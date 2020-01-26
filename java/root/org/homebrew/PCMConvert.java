package org.homebrew;

public class PCMConvert
{
    public static void convert(byte[] in_data, byte[] out_data)
    {
        for(int i = 0; i < 48000; i++)
        {
            int q = ((int)(in_data[(i * 11025) / 48000])) & 0xff;
            q -= 128;
            out_data[2*i] = (byte)q;
            out_data[2*i+1] = 0;
        }
    }
    public static int estimateSize(int in_sz)
    {
        return 2 * (int)((in_sz * 48000l + 11024l) / 11025l);
    }
}
