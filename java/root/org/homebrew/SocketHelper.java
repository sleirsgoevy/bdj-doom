package org.homebrew;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.ArrayList;

public class SocketHelper
{
    private static interface NonBlockingSocket
    {
        public int sendto(int addr, int cnt, int peer);
        public int recvfrom(int addr, int cnt, int peer);
        public void regLastPeer(int port);
    }
    private static class NonBlockingUDPSocket extends Thread implements NonBlockingSocket
    {
        private MyXlet.EventQueue eq;
        private DatagramSocket sock;
        private InetAddress last;
        private int my_port;
        public NonBlockingUDPSocket(int port, boolean broadcast) throws IOException
        {
            this.my_port = port;
            if(broadcast)
                sock = new DatagramSocket(port);
            else
                sock = new DatagramSocket(port, InetAddress.getByName(getIPAddress(false)));
            sock.setBroadcast(true);
            eq = new MyXlet.EventQueue();
            start();
        }
        public int sendto(int addr, int cnt, int peer)
        {
            peer--;
            InetAddress inet_addr = null;
            int port = this.my_port + 1;
            if(peers == null || peer < 0 || peer >= peers.size())
            {
                try
                {
                    inet_addr = InetAddress.getByName("255.255.255.255");
                }
		catch(java.net.UnknownHostException e){} // will never happen
            }
            else
            {
                inet_addr = (InetAddress)peers.get(peer);
                port = ((Integer)ports.get(peer)).intValue();
            }
            byte[] buf = new byte[cnt];
            CRunTime.memcpy(buf, 0, addr, cnt);
            DatagramPacket pkt = new DatagramPacket(buf, buf.length);
            pkt.setAddress(inet_addr);
            pkt.setPort(port);
            try
            {
                sock.send(pkt);
                return cnt;
            }
            catch(IOException e)
            {
                return -1;
            }
        }
        public int recvfrom(int addr, int cnt, int peer)
        {
            Object o = eq.get();
            if(o == null)
                return -1;
            DatagramPacket pkt = (DatagramPacket)o;
            InetAddress inet_addr = last = pkt.getAddress();
            if(inet_addr.getHostAddress().equals(getIPAddress(false)))
                return -1;
            int i;
            for(i = 0; peers != null && i < peers.size(); i++)
                if(peers.get(i).equals(inet_addr))
                    break;
            CRunTime.memory[peer/4] = i + 1;
            byte[] buf = pkt.getData();
            int len = pkt.getLength();
            if(cnt > len)
                cnt = len;
            CRunTime.memcpy(addr, buf, 0, cnt);
            return cnt;
        }
        public void run()
        {
            byte[] buf = null;
            for(;;)
            {
                if(buf == null)
                    buf = new byte[4096];
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                try
                {
                    sock.receive(pkt);
                }
                catch(IOException e)
                {
                    e.printStackTrace(MyXlet.getStdout());
                    continue;
                }
                eq.put(pkt);
                buf = null;
            }
        }
        public void regLastPeer(int port)
        {
            registerPeer(last.getHostAddress(), port);
        }
    }
    private static class IPXSocket extends Thread
    {
        public String ipx_ntoa(byte[] addr)
        {
            String addr_s = "";
            String alphabet = "0123456789abcdef";
            for(int i = 0; i < 10; i++)
            {
                addr_s += ':';
                addr_s += alphabet.charAt((addr[i]>>4)&15);
                addr_s += alphabet.charAt(addr[i]&15);
            }
            return addr_s.substring(1);
        }
        private class IPXPacket
        {
            public byte[] data;
            public byte[] src_addr;
            public int src_port;
            public IPXPacket(byte[] data, byte[] src_addr, int src_port)
            {
                this.data = data;
                this.src_addr = src_addr;
                this.src_port = src_port;
            }
        }
        private class IPXPortSocket implements NonBlockingSocket
        {
            private IPXSocket master;
            private int local_port;
            public MyXlet.EventQueue eq;
            private byte[] last_addr;
            private int last_port;
            public IPXPortSocket(IPXSocket m, int p)
            {
                master = m; 
                local_port = p;
                eq = new MyXlet.EventQueue();
            }
            public int sendto(int ptr, int len, int peer)
            {
                peer--;
                byte[] remote_addr;
                int remote_port;
                if(peers == null || peer < 0 || peer >= peers.size())
                {
                    remote_addr = new byte[]{(byte)0, (byte)0, (byte)0, (byte)0, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1};
                    remote_port = local_port;
                }
                else
                {
                    remote_addr = (byte[])peers.get(peer);
                    remote_port = ((Integer)ports.get(peer)).intValue();
                }
                byte[] data = new byte[len+4]; // 4 garbage bytes on IPX
                CRunTime.memcpy(data, 0, ptr, len);
                try
                {
                    master.ipx_send(0, remote_addr, remote_port, master.my_addr, local_port, data);
                }
                catch(IOException e)
                {
                    return -1;
                }
                return len;
            }
            public int recvfrom(int ptr, int len, int peer_p)
            {
                Object o = eq.get();
                if(o == null)
                    return -1;
                IPXPacket pkt = (IPXPacket)o;
                boolean not_own = false;
                for(int i = 0; i < 10 && !not_own; i++)
                    if(pkt.src_addr[i] != master.my_addr[i])
                        not_own = true;
                if(!not_own)
                    return -1;
                last_addr = pkt.src_addr;
                last_port = pkt.src_port;
                int peer;
                for(peer = 0; peers != null && peer < peers.size() && !ipx_ntoa((byte[])peers.get(peer)).equals(ipx_ntoa(last_addr)); peer++);
                CRunTime.memory[peer_p / 4] = peer + 1;
                if(len > pkt.data.length-4)
                    len = pkt.data.length-4;
                CRunTime.memcpy(ptr, pkt.data, 0, len);
                return len;
            }
            public void regLastPeer(int port)
            {
                registerPeer(ipx_ntoa(last_addr), last_port);
            }
        }
        private DatagramSocket sock;
        private java.util.HashMap bindings;
        public byte[] my_addr;
        public void ipx_send(int kind, byte[] dst_addr, int dst_port, byte[] src_addr, int src_port, byte[] data) throws IOException
        {
            byte[] buf = new byte[30 + data.length];
            buf[0] = buf[1] = (byte)-1;
            buf[2] = (byte)(buf.length >> 8);
            buf[3] = (byte)buf.length;
            buf[4] = 0;
            buf[5] = (byte)kind;
            for(int i = 0; i < 10; i++)
            {
                buf[i + 6] = dst_addr[i];
                buf[i + 18] = src_addr[i];
            }
            buf[16] = (byte)(dst_port >> 8);
            buf[17] = (byte)dst_port;
            buf[28] = (byte)(src_port >> 8);
            buf[29] = (byte)src_port;
            for(int i = 0; i < data.length; i++)
                buf[i + 30] = data[i];
            DatagramPacket pkt = new DatagramPacket(buf, buf.length);
            sock.send(pkt);
        }
        private int ipx_recv(byte[] dst, byte[] src, int[] ports, byte[] buf) throws IOException
        {
            DatagramPacket pkt = new DatagramPacket(buf, buf.length);
            sock.receive(pkt);
            int l = pkt.getLength();
            int l2 = (255&(int)buf[2])<<8|(255&(int)buf[3]);
            if(l != l2)
                throw new RuntimeException("wtf?");
            ports[2] = 255&(int)buf[5];
            for(int i = 0; i < 10; i++)
            {
                dst[i] = buf[i + 6];
                src[i] = buf[i + 18];
            }
            ports[0] = (255&(int)buf[16])<<8|(255&(int)buf[17]);
            ports[1] = (255&(int)buf[28])<<8|(255&(int)buf[29]);
            l -= 30;
            for(int i = 0; i < l; i++)
                buf[i] = buf[i + 30];
            return l;
        }
        public IPXSocket(String server_ip, int server_port) throws IOException
        {
            sock = new DatagramSocket();
            sock.connect(InetAddress.getByName(server_ip), server_port);
            byte[] dst_addr = new byte[10];
            byte[] src_addr = new byte[10];
            byte[] body = new byte[0];
            sock.setSoTimeout(1000);
            int[] ports = new int[3];
            byte[] recvbuf = new byte[30];
            int len = -1;
            while(true)
            {
                ipx_send(0, dst_addr, 2, src_addr, 2, body);
                try
                {
                    len = ipx_recv(dst_addr, src_addr, ports, recvbuf);
                    break;
                }
                catch(java.net.SocketTimeoutException e){}
            }
            sock.setSoTimeout(0);
            my_addr = dst_addr;
            bindings = new java.util.HashMap();
            start();
        }
        public void run()
        {
            byte[] src_addr = new byte[10];
            byte[] dst_addr = new byte[10];
            int[] ports = new int[3];
            byte[] buf = new byte[4096];
            while(true)
            {
                int l;
                try
                {
                    l = ipx_recv(dst_addr, src_addr, ports, buf);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    continue;
                }
                boolean is_me = true;
                for(int i = 0; i < 10 && is_me; i++)
                    if(dst_addr[i] != my_addr[i])
                        is_me = false;
                boolean is_broadcast = true;
                for(int i = 4; i < 10 && is_broadcast; i++)
                    if(dst_addr[i] != (byte)-1)
                        is_broadcast = false;
                if(is_me && is_broadcast)
                    throw new RuntimeException("wtf?");
                if(!is_me && !is_broadcast)
                    continue;
                Object sock = bindings.get(new Integer(ports[0]));
                if(sock == null)
                    continue;
                byte[] pkt = new byte[l];
                for(int i = 0; i < l; i++)
                    pkt[i] = buf[i];
                ((IPXPortSocket)sock).eq.put(new IPXPacket(pkt, src_addr, ports[1]));
            }
        }
        public synchronized NonBlockingSocket bind(int port)
        {
            Object sock = bindings.get(new Integer(port));
            if(sock != null)
                //throw new RuntimeException("cannot bind to already bound port");
                return (IPXPortSocket)sock;
            IPXPortSocket ps = new IPXPortSocket(this, port);
            bindings.put(new Integer(port), ps);
            return ps;
        }
    }
    private static ArrayList peers;
    private static ArrayList ports;
    private static IPXSocket ipx;
    public static int initIPX(String host, int port)
    {
        try
        {
            ipx = new IPXSocket(host, port);
        }
        catch(IOException e)
        {
            e.printStackTrace(MyXlet.getStdout());
            return -1;
        }
        return 0;
    }
    private static NonBlockingSocket _create(int port, int broadcast) throws IOException
    {
        if(ipx != null)
            return ipx.bind(port);
        else
            return new NonBlockingUDPSocket(port, broadcast != 0);
    }
    public static int create(int port, int broadcast)
    {
        try
        {
            return CRunTime.registerObject(_create(port, broadcast));
        }
        catch(IOException e)
        {
            e.printStackTrace(MyXlet.getStdout());
            return -1;
        }
    }
    public static void registerPeer(String addr, int port)
    {
        if(peers == null)
            peers = new ArrayList();
        if(split(addr, '.').length == 4) // IP address
        {
            try
            {
                peers.add(InetAddress.getByName(addr));
            }
            catch(java.net.UnknownHostException e){} // will never happen
        }
        else if(split(addr, ':').length == 10) // IPX address
        {
            byte[] n_addr = new byte[10];
            String[] sp = split(addr, ':');
            for(int i = 0; i < 10; i++)
                n_addr[i] = (byte)Integer.parseInt(sp[i], 16);
            peers.add(n_addr);
        }
        else
            throw new RuntimeException("unknown address type: "+addr);
        if(ports == null)
            ports = new ArrayList();
        ports.add(new Integer(port));
    }
    public static int sendto(int sock, int addr, int cnt, int peer) throws java.net.UnknownHostException
    {
        return ((NonBlockingSocket)CRunTime.getRegisteredObject(sock)).sendto(addr, cnt, peer);
    }
    public static int recvfrom(int sock, int addr, int cnt, int peer)
    {
        return ((NonBlockingSocket)CRunTime.getRegisteredObject(sock)).recvfrom(addr, cnt, peer);
    }
    public static void registerLastPeer(int sock, int port) throws java.net.UnknownHostException
    {
        ((NonBlockingSocket)CRunTime.getRegisteredObject(sock)).regLastPeer(port);
    }
    public static String getIPAddress(boolean broadcast)
    {
        if(broadcast)
            return "255.255.255.255";
        try
        {
            java.util.Enumeration x = java.net.NetworkInterface.getNetworkInterfaces();
            while(x.hasMoreElements())
            {
                java.net.NetworkInterface ni = (java.net.NetworkInterface)x.nextElement();
                java.util.Enumeration y = ni.getInetAddresses();
                while(y.hasMoreElements())
                {
                    String addr = ((InetAddress)y.nextElement()).getHostAddress();
                    if(addr.startsWith("192.168.") || addr.startsWith("172.16.") || addr.startsWith("10."))
                        return addr;
                }
            }
        }
        catch(Exception e){}
        return null;
    }
    private static String[] split(String s, char c) //JavaME sucks
    {
        int cnt = 1;
        for(int i = 0; i < s.length(); i++)
            if(s.charAt(i) == c)
                cnt++;
        String[] ans = new String[cnt];
        ans[0] = "";
        int i = 0;
        for(int j = 0; j < s.length(); j++)
            if(s.charAt(j) == c)
                ans[++i] = "";
            else
                ans[i] += s.charAt(j);
        return ans;
    }
    private static byte[] inet_aton(String s)
    {
        String[] arr = split(s, '.');
        byte[] ans = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            ans[i] = (byte)(new Integer(arr[i])).intValue();
        return ans;
    }
    public static int getConsolePlayer()
    {
        byte[] ip0 = (ipx!=null?ipx.my_addr:inet_aton(getIPAddress(false)));
        int ans = 0;
        for(int i = 0; i < peers.size(); i++)
        {
            
            byte[] ip1 = (ipx!=null?(byte[])peers.get(i):inet_aton(((InetAddress)peers.get(i)).getHostAddress()));
            int diff = 0;
            for(int j = 0; j < ip1.length && diff == 0; j++)
                diff = (255&(int)ip0[j]) - (255&(int)ip1[j]);
            if(diff > 0)
                ans++;
        }
        return ans;
    }
}
