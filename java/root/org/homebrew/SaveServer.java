package org.homebrew;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SaveServer extends Thread
{
    private PrintStream console;
    public SaveServer(PrintStream c)
    {
        console = c;
    }
    public void run()
    {
        try
        {
            ServerSocket ssock = new ServerSocket(4444);
            while(true)
            {
                Socket sock = ssock.accept();
                InputStream is = sock.getInputStream();
                OutputStream os = sock.getOutputStream();
                String header = "";
                do
                    header += (char)is.read();
                while(header.charAt(header.length()-1) != '\n');
                int q = header.indexOf(" ");
                String path = header.substring(q + 1);
                q = path.indexOf(" ");
                path = path.substring(0, q);
                path = (MyXlet.getVFSRoot()+"/"+path);
                while((q = path.indexOf("//")) >= 0)
                    path = path.substring(0, q+1)+path.substring(q+2);
                InputStream infile = null;
                try
                {
                    URL url = new URL(path);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    infile = conn.getInputStream();
                }
                catch(IOException e){}
                if(infile == null)
                {
                    os.write("HTTP/1.0 404 Not Found\r\nContent-Type: text/plain\r\nConnection: close\r\n\r\n404 Not Found".getBytes("UTF8"));
                    os.flush();
                    sock.close();
                    continue;
                }
                os.write("HTTP/1.0 200 OK\r\nContent-Type: application/octet-stream\r\nConnection: close\r\n\r\n".getBytes("UTF8"));
                byte[] buf = new byte[4096];
                int len;
                while((len = infile.read(buf)) > 0)
                    os.write(buf, 0, len);
                os.flush();
                sock.close();
            }
        }
        catch(Throwable e)
        {
            e.printStackTrace(console);
        }
    }
    public static void testConnection(PrintStream out)
    {
        try
        {
            URL url = new URL("http://ip-address.ru/show");
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            String s = "";
            int c;
            while((c = is.read()) >= 0)
                s += (char)c;
            out.println(s);
        }
        catch(Exception e)
        {
            e.printStackTrace(out);
        }
    }
}
