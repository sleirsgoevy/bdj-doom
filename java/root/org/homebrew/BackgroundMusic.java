package org.homebrew;

public class BackgroundMusic
{
    private static java.util.HashMap by_handle;
    private static int handle_alloc;
    static
    {
        by_handle = new java.util.HashMap();
    }
    public static int registerSong(int ptr, int len)
    {
        byte[] b = new byte[len];
        CRunTime.memcpy(b, 0, ptr, len);
        int handle = ++handle_alloc;
        while(true)
        {
            try
            {
                by_handle.put(new Integer(handle), new MUSFile(b));
                break;
            }
            catch(OutOfMemoryError e)
            {
                PCMPlayer.flushCache();
            }
        }
        return handle;
    }
    public static void unregisterSong(int handle)
    {
        by_handle.remove(new Integer(handle));
    }
    public static void playSong(int handle, int looping)
    {
        if(looping != 0)
            ((MUSFile)by_handle.get(new Integer(handle))).loop();
        else
            ((MUSFile)by_handle.get(new Integer(handle))).play();
    }
    public static void pauseSong(int handle)
    {
        ((MUSFile)by_handle.get(new Integer(handle))).pause();
    }
    public static void resumeSong(int handle)
    {
        ((MUSFile)by_handle.get(new Integer(handle))).resume();
    }
    public static void stopSong(int handle)
    {
        ((MUSFile)by_handle.get(new Integer(handle))).stop();
    }
    public static void shutdown()
    {
        java.util.Iterator it = by_handle.values().iterator();
        while(it.hasNext())
            ((MUSFile)it.next()).stop();
    }
}
