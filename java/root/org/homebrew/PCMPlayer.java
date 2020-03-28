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
                //PCMWriter.writePCM("sound", ((Integer)ptrs.get(idx)).intValue(), ((Integer)szs.get(idx)).intValue());
                while(true)
                {
                    try
                    {
                        pl = new HSound();
                        pl.set(/*new java.net.URL(MyXlet.getVFSRoot()+"/sound.pcm")*/PCMWriter.writePCM(((Integer)ptrs.get(idx)).intValue(), ((Integer)szs.get(idx)).intValue()));
                        cache.put(iidx, pl);
                        break;
                    }
                    catch(OutOfMemoryError e)
                    {
                        cache.clear();
                    }
                }
            }
            else
                pl = (HSound)cache.get(iidx);
        }
        pl.stop();
        pl.play();
    }
    public static void flushCache()
    {
        synchronized(cache)
        { 
            cache.clear();
        }
    }
}
