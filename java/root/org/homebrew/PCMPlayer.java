package org.homebrew;

import java.util.ArrayList;
import java.util.HashMap;
import javax.media.*;
import org.havi.ui.*;

public class PCMPlayer
{
    private static ArrayList names;
    private static ArrayList ptrs;
    private static ArrayList szs;
    private static HashMap cache;
    static
    {
        cache = new HashMap();
    }
    public static int register(String s, int ptr, int sz)
    {
        if(names == null)
            names = new ArrayList();
        if(ptrs == null)
            ptrs = new ArrayList();
        if(szs == null)
            szs = new ArrayList();
        int ans = names.size();
        names.add(s);
        ptrs.add(new Integer(ptr));
        szs.add(new Integer(sz));
        return ans;
    }
    public static void play(int idx) throws Exception
    {
        HSound pl;
        synchronized(cache)
        {
            Integer iidx = new Integer(idx);
            if(!cache.containsKey(iidx))
            {
                PCMWriter.writePCM("sound"+idx, ((Integer)ptrs.get(idx)).intValue(), ((Integer)szs.get(idx)).intValue());
                pl = new HSound();
                pl.load(new java.net.URL(MyXlet.getVFSRoot()+"/sound"+idx+".pcm"));
                cache.put(iidx, pl);
            }
            else
                pl = (HSound)cache.get(iidx);
        }
        pl.stop();
        pl.play();
    }
}
