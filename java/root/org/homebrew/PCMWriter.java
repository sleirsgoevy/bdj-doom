package org.homebrew;

import java.io.FileOutputStream;
import java.io.IOException;

public class PCMWriter
{
    public static void writePCM(String name, int ptr, int cnt) throws IOException
    {
        int sz = PCMConvert.estimateSize(cnt);
        byte[] header = new byte[56];
        for(int i = 0; i < 8; i++)
            header[i] = (byte)"BCLK0200".charAt(i);
        header[11] = (byte)56;
        header[43] = (byte)12;
        header[45] = (byte)1;
        header[46] = (byte)0x11;
        header[47] = (byte)0x40;
        int q = sz;
        for(int i = 55; i >= 52; i--)
        {
            header[i] = (byte)q;
            q >>>= 8;
        }
        FileOutputStream out = new FileOutputStream(MyXlet.getVFSRoot().substring(7)+"/"+name+".pcm");
        out.write(header);
        byte[] buf1 = new byte[11025];
        byte[] buf2 = new byte[96000];
        int rd_left = cnt;
        int wr_left = sz;
        while(rd_left != 0 && wr_left != 0)
        {
            int chunk_sz = (rd_left<11025?rd_left:11025);
            CRunTime.memcpy(buf1, 0, ptr, chunk_sz);
            rd_left -= chunk_sz;
            ptr += chunk_sz;
            PCMConvert.convert(buf1, buf2);
            chunk_sz = (wr_left<96000?wr_left:96000);
            out.write(buf2, 0, chunk_sz);
            wr_left -= chunk_sz;
        }
        if(rd_left != 0 || wr_left != 0)
            throw new RuntimeException("wtf?");
    }
}
