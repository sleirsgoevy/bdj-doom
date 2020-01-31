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
        public NonBlockingSocket(int port) throws java.io.IOException
        {
            sock = new DatagramSocket(port);
            eq = new MyXlet.EventQueue();
            start();
        }
        public int sendto(int addr, int cnt, int peer) throws java.net.UnknownHostException
        {
            peer--;
            InetAddress inet_addr;
            int port = 5029;
            if(peer < 0 || peer >= peers.size())
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
            InetAddress inet_addr = pkt.getAddress();
            int i;
            for(i = 0; i < peers.size(); i++)
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
    }
    private static ArrayList peers;
    private static ArrayList ports;
    public static int create(int port)
    {
        try
        {
            return CRunTime.registerObject(new NonBlockingSocket(port));
        }
        catch(IOException e)
        {
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
}
