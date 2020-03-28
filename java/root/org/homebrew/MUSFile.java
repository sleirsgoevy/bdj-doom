package org.homebrew;

public class MUSFile
{
    private class PlayingThread extends Thread
    {
        public void run()
        {
            try
            {
            while(playing)
            {
                int[] durations = new int[128];
                for(int i = 0; i < 128; i++)
                    durations[i] = -1;
                int[] depths = new int[128];
                int[] starts = new int[128];
                //MyXlet.getStdout().println("precalc durations...");
                for(int i = 0; i < events.size(); i++)
                {
                    Event e = (Event)events.get(i);
                    if(e.is_stop)
                    {
                        if(--depths[e.t.note] == 0)
                        {
                            int duration = e.t.stop - starts[e.t.note];
                            if(duration > durations[e.t.note])
                                durations[e.t.note] = duration;
                        }
                    }
                    else
                    {
                        if(depths[e.t.note]++ == 0)
                            starts[e.t.note] = e.t.start;
                    }
                }
                //MyXlet.getStdout().println("precalc durations... done");
                //MyXlet.getStdout().println("allocate HSounds... ");
                org.havi.ui.HSound[] notes = new org.havi.ui.HSound[128];
                int total_duration = 0;
                for(int i = 0; i < 128; i++)
                    if(durations[i] >= 0)
                    {
                        int d = durations[i];
                        if(d > 2000)
                            d = 2000;
                        total_duration += d;
                        while(true)
                        {
                            try
                            {
                                notes[i] = PCMTone.tone(note_freq(i), 48 * d);
                                break;
                            }
                            catch(OutOfMemoryError e)
                            {
                                PCMPlayer.flushCache();
                            }
                        }
                    }
                //MyXlet.getStdout().println("allocate HSounds... done");
                for(int i = 0; i < 128; i++)
                    depths[i] = 0;
                starts = null;
                long shift = System.currentTimeMillis() - cur_time;
                //MyXlet.getStdout().println("playing... ");
                for(int i = 0; i < events.size() && playing; i++)
                {
                    boolean noop = false;
                    Event e = (Event)events.get(i);
                    int event_time;
                    if(e.is_stop)
                        event_time = e.t.stop;
                    else
                    {
                        event_time = e.t.start;
                        if(System.currentTimeMillis() >= e.t.stop + shift)
                            noop = true;
                    }
                    while(playing && System.currentTimeMillis() < event_time + shift)
                        cur_time = System.currentTimeMillis() - shift;
                    if(!playing)
                        break;
                    if(e.is_stop)
                    {
                        if(--depths[e.t.note] == 0)
                            notes[e.t.note].stop();
                    }
                    else
                    {
                        if(depths[e.t.note]++ == 0 && !noop)
                        {
                            if(durations[e.t.note] > 2000)
                                notes[e.t.note].loop();
                            else
                                notes[e.t.note].play();
                        }
                    }
                    //MyXlet.getStdout().println("playing... "+i+"/"+events.size());
                }
                if(!playing)
                {
                    for(int i = 0; i < 128; i++)
                        if(notes[i] != null)
                            notes[i].stop();
                }
                if(!looping)
                    break;
                cur_time = 0;
            }
            }
            catch(Throwable e)
            {
                e.printStackTrace(MyXlet.getStdout());
            }
        }
    }
    private PlayingThread pl_t;
    private long cur_time;
    private boolean playing;
    private boolean looping;
    private class Tone
    {
        public int start = 0;
        public int stop = 0;
        public int note = 0;
    }
    private class Event
    {
        public Tone t;
        public boolean is_stop;
        public Event(Tone t, boolean is_stop)
        {
            this.t = t;
            this.is_stop = is_stop;
        }
    }
    java.util.ArrayList events;
    private int note_freq(int i)
    {
        return (int)(440 * Math.pow(2, (i - 69) / 12.0));
    }
    private int ticks_to_us(int t)
    {
        return t * 7440;
    }
    public MUSFile(byte[] data)
    {
        events = new java.util.ArrayList();
        int length = (((int)data[5])&255)<<8|(((int)data[4])&255);
        int start = (((int)data[7])&255)<<8|(((int)data[6])&255);
        int cur = start;
        int end = start + length;
        java.util.ArrayList[] notes = new java.util.ArrayList[128];
        int cur_time = 0;
        for(int i = cur; i < end; i++)
        {
            int e = (((int)data[i])&255);
            int ch = e & 15;
            int k = (e >> 4) & 7;
            int note;
            switch(k)
            {
            case 0:
                note = (((int)data[++i])&255);
                if(notes[note] != null && notes[note].size() != 0)
                {
                    Tone t = (Tone)notes[note].remove(notes[note].size()-1);
                    t.stop = ticks_to_us(cur_time) / 1000;
                    events.add(new Event(t, true));
                }
                break;
            case 1:
                note = (((int)data[++i])&255);
                if((note & 128) != 0)
                {
                    note &= 127;
                    i++; // volume;
                }
                Tone t = new Tone();
                t.start = ticks_to_us(cur_time) / 1000;
                t.note = note;
                events.add(new Event(t, false));
                if(notes[note] == null)
                    notes[note] = new java.util.ArrayList();
                notes[note].add(t);
                break;
            case 3:
            case 4:
                i++;
            case 2:
                i++;
                break;
            case 6:
                if(data[i + 1 - end] != 0); // assert i + 1 == end
                break;
            case 5:
            case 7:
                if(data[i - end] != 0); // assert False
            }
            if((e & 128) != 0)
            {
                int delta = 0;
                for(;;)
                {
                    int c = ((int)data[++i])&255;
                    delta = delta << 7 | c & 127;
                    if((c & 128) == 0)
                        break;
                }
                cur_time += delta;
            }
        }
    }
    public synchronized void play()
    {
        playing = true;
        looping = false;
        cur_time = 0;
        pl_t = new PlayingThread();
        pl_t.start();
    }
    public synchronized void loop()
    {
        playing = true;
        looping = true;
        cur_time = 0;
        pl_t = new PlayingThread();
        pl_t.start();
    }
    public synchronized void pause()
    {
        playing = false;
        pl_t = null;
    }
    public synchronized void resume()
    {
        playing = true;
        pl_t = new PlayingThread();
        pl_t.start();
    }
    public synchronized void stop()
    {
        playing = false;
        pl_t = null;
    }
}
