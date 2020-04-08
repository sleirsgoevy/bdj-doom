package org.homebrew;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PCMTone
{
    public static byte[] writePCM(int freq, int dur, int volume)
    {
        int sz = dur * 2;
        byte[] buf = new byte[dur * 2 + 56];
        for(int i = 0; i < 8; i++)
            buf[i] = (byte)"BCLK0200".charAt(i);
        buf[11] = (byte)56;
        buf[43] = (byte)12;
        buf[45] = (byte)1;
        buf[46] = (byte)0x11;
        buf[47] = (byte)0x40;
        int q = sz;
        for(int i = 55; i >= 52; i--)
        {
            buf[i] = (byte)q;
            q >>>= 8;
        }
        for(int i = 0; i < dur; i++)
        {
            if(((i*2*freq)/48000)%2!=0)
            {
                buf[2*i+56] = (byte)((-volume) >> 8);
                buf[2*i+57] = (byte)(-volume);
            }
            else
            {
                buf[2*i+56] = (byte)(volume >> 8);
                buf[2*i+57] = (byte)volume;
            }
        }
        return buf;
    }
    public static org.havi.ui.HSound tone(int freq, int dur, int volume)
    {
        org.havi.ui.HSound hs = new org.havi.ui.HSound();
        hs.set(writePCM(freq, dur, volume));
        return hs;
    }
}
