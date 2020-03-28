package org.homebrew;

import java.io.*;
import org.dvb.ui.DVBBufferedImage;

public class DVBCachedFile
{
    private int length;
    private DVBBufferedImage img;
    private static java.util.HashMap cache;
    static 
    {
        cache = new java.util.HashMap();
    }
    private DVBCachedFile(String fname) throws IOException
    {
        length = (int)(new File(fname)).length();
        img = new DVBBufferedImage(1000, length / 3000 + 1 /* fix 1000x0 issue */);
        FileInputStream fin = new FileInputStream(fname);
        for(int offset = 0; offset < length; offset += 3000)
        {
            int expected_sz = length - offset;
            if(expected_sz > 3000)
                expected_sz = 3000;
            byte[] buf = new byte[3000];
            int[] ibuf = new int[1000];
            int buf_offset = 0;
            while(expected_sz > 0)
            {
                int real_sz = fin.read(buf, buf_offset, expected_sz);
                if(real_sz <= 0)
                    throw new EOFException();
                buf_offset += real_sz;
                expected_sz -= real_sz;
            }
            for(int i = 0; i < 1000; i++)
                ibuf[i] = 0xff000000
                         |(255&(int)buf[3*i])<<16
                         |(255&(int)buf[3*i+1])<<8
                         |(255&(int)buf[3*i+2]);
            img.setRGB(0, offset / 3000, 1000, 1, ibuf, 0, 1000);
        }
    }
    private InputStream getInputStream()
    {
        return new InputStream()
        {
            private int pos = 0;
            private int mark = 0;
            private int[] ibuf = new int[1000];
            private int cur_line = -1;
            public synchronized int read(byte[] buf, int off, int len)
            {
                if(pos == length)
                    return -1;
                for(int i = off; i < off + len; i++)
                {
                    if(pos == length)
                        return i - off;
                    int line = pos / 3000;
                    if(line != cur_line)
                    {
                        cur_line = line;
                        img.getRGB(0, line, 1000, 1, ibuf, 0, 1000);
                    }
                    buf[i] = (byte)(ibuf[(pos%3000)/3]>>((2-pos%3)<<3));
                    pos++;
                }
                return len;
            }
            public synchronized int read(byte[] buf)
            {
                return read(buf, 0, buf.length);
            }
            public synchronized int read()
            {
                byte[] b = new byte[1];
                if(read(b) <= 0)
                    return -1;
                return 255&(int)b[0];
            }
            public boolean markSupported()
            {
                return true; 
            }
            public synchronized void mark(int limit)
            {
                mark = pos;
            }
            public synchronized void reset()
            {
                pos = mark;
            }
            public synchronized long skip(long n)
            {
                int old_pos = pos;
                pos += (int)n;
                if(pos > length)
                    pos = length;
                return pos - old_pos;
            }
        };
    }
    public static InputStream open(String fname) throws IOException
    {
        DVBCachedFile ans = (DVBCachedFile)cache.get(fname);
        if(ans == null)
        {
            ans = new DVBCachedFile(fname);
            cache.put(fname, ans);
        }
        return ans.getInputStream();
    }
}
