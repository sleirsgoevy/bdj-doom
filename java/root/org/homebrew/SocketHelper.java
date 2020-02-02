package org.homebrew;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.ArrayList;

public class SocketHelper
{
    private static class NonBlockingSocket extends Thread
    {
        private MyXlet.EventQueue eq;
        private DatagramSocket sock;
        private InetAddress last;
        private int my_port;
        public NonBlockingSocket(int port, boolean broadcast) throws java.io.IOException
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
        public int sendto(int addr, int cnt, int peer) throws java.net.UnknownHostException
        {
            peer--;
            InetAddress inet_addr;
            int port = this.my_port + 1;
            if(peers == null || peer < 0 || peer >= peers.size())
                inet_addr = InetAddress.getByName("255.255.255.255");
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
        public void regLastPeer(int port) throws java.net.UnknownHostException
        {
            registerPeer(last.getHostAddress(), port);
        }
    }
    private static ArrayList peers;
    private static ArrayList ports;
    public static int create(int port, int broadcast)
    {
        try
        {
            return CRunTime.registerObject(new NonBlockingSocket(port, broadcast != 0));
        }
        catch(IOException e)
        {
            e.printStackTrace(MyXlet.getStdout());
            return -1;
        }
    }
    public static void registerPeer(String addr, int port) throws java.net.UnknownHostException
    {
        if(peers == null)
            peers = new ArrayList();
        peers.add(InetAddress.getByName(addr));
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
        byte[] ip0 = inet_aton(getIPAddress(false));
        int ans = 0;
        for(int i = 0; i < peers.size(); i++)
        {
            byte[] ip1 = inet_aton(((InetAddress)peers.get(i)).getHostAddress());
            int diff = 0;
            for(int j = 0; j < 4 && diff == 0; j++)
                diff = ip0[j] - ip1[j];
            if(diff > 0)
                ans++;
        }
        return ans;
    }
}
