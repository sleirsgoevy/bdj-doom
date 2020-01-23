package org.homebrew;

import java.io.*;

import java.util.*;

import java.awt.*;
import java.net.*;

import javax.media.*;

import javax.tv.xlet.*;

import org.bluray.ui.event.HRcEvent;
import org.bluray.net.BDLocator;
import org.bluray.ti.DiscManager;
import org.davic.net.Locator;

import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;
import org.dvb.event.UserEventListener;
import org.dvb.event.UserEventRepository;

import org.havi.ui.*;
import org.dvb.ui.DVBBufferedImage;

public class MyXlet implements Xlet, UserEventListener
{
    private static MyXlet instance;
    private class EventQueue
    {
        private LinkedList l;
        int cnt = 0;
        EventQueue()
        {
            l = new LinkedList();
        }
        public synchronized void put(Object obj)
        {
            l.addLast(obj);
            cnt++;
        }
        public synchronized Object get()
        {
            if(cnt == 0)
                return null;
            Object o = l.getFirst();
            l.removeFirst();
            cnt--;
            return o;
        }
    }
    private EventQueue eq;
    private HScene scene;
    private Screen gui;
    private XletContext context;
    private PrintStream console;
    private PrintStream sock;
    private final ArrayList messages = new ArrayList();
    public void initXlet(XletContext context)
    {
        instance = this;
        this.context = context;
        this.eq = new EventQueue();
        // START: Code required for text output.
        scene = HSceneFactory.getInstance().getDefaultHScene();
        try
        {
            gui = new Screen(messages);
            gui.setSize(1920, 1080); // BD screen size
            scene.add(gui, BorderLayout.CENTER);
            // END: Code required for text output.
            UserEventRepository repo = new UserEventRepository("input");
            repo.addAllArrowKeys();
            repo.addAllColourKeys();
            repo.addAllNumericKeys();
            repo.addKey(HRcEvent.VK_ENTER);
            repo.addKey(HRcEvent.VK_POPUP_MENU);
            repo.addKey(19);
            repo.addKey(424);
            repo.addKey(425);
            repo.addKey(412);
            repo.addKey(417);
            EventManager.getInstance().addUserEventListener(this, repo);
            (new Thread()
            {
                public void run()
                {
                    try
                    {
                        scene.repaint();
                        console = new PrintStream(new MessagesOutputStream(messages, scene));
                        //SaveServer.testConnection(console);
                        (new SaveServer(console)).start();
                        InputStream is = getClass().getResourceAsStream("/program.data.bin");
                        int start = CibylCallTable.getAddressByName("__start");
                        CRunTime.init(is);
                        int sp = (CRunTime.memory.length * 4) - 8;
                        CRunTime.publishCallback("Cibyl.atexit");
                        CibylCallTable.call(start, sp, 0, 0, 0, 0);
                        messages.add("something went wrong");
                        scene.repaint();
                    }
                    catch(Throwable e)
                    {
                        printStackTrace(e);
                        gui.gui = false;
                        scene.repaint();
                    }
                }
            }).start();
        }
        catch(Throwable e)
        {
            printStackTrace(e);
            gui.gui = false;
        }
        scene.validate();
    }
    // Don't touch any of the code from here on.
    public void startXlet()
    {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();
    }
    public void pauseXlet()
    {
        gui.setVisible(false);
    }
    public void destroyXlet(boolean unconditional)
    {
        scene.remove(gui);
        scene = null;
    }
    private void printStackTrace(Throwable e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        if(trace.charAt(trace.length()-1) != '\n')
            trace += '\n';
        String line = "";
        for(int i = 0; i < trace.length(); i++)
        {
            char x = trace.charAt(i);
            if(x == '\n')
            {
                messages.add(line);
                line = "";
            }
            else
                line += x;
        }
    }
    public void userEventReceived(UserEvent evt)
    {
        if(!this.gui.gui)
        {
            if(evt.getType() == HRcEvent.KEY_PRESSED)
            {
                if(evt.getCode() == 38)
                    gui.top -= 800;
                else if(evt.getCode() == 40)
                    gui.top += 800; 
                scene.repaint();
            }
            return;
        }
        if(evt.getType() == HRcEvent.KEY_PRESSED)
        {
            eq.put(new Integer((int)evt.getCode()));
        }
        else if(evt.getType() == HRcEvent.KEY_RELEASED)
            eq.put(new Integer(-(int)evt.getCode()));
        // kept as an API reference
        /*messages.add("some event");
        if(evt.getType() == HRcEvent.KEY_PRESSED)
        {
            messages.add("keydown "+evt.getCode());
            if(evt.getCode() == 10 || evt.getCode() == 461)
            {
                try
                {
                    MediaLocator ml = (evt.getCode()==10?new MediaLocator(getClass().getResource("/00000.m2ts")):new MediaLocator("bd://PLAYLIST:00000"));
                    Player pl = Manager.createPlayer(ml);
                    pl.start();
                }
                catch(Throwable e)
                {
                    printStackTrace(e);
                }
            }
            else if(evt.getCode() == 19)
            {
                messages.clear();
                messages.add("screen cleared");
            }
        }
        else if(evt.getType() == HRcEvent.KEY_RELEASED)
            messages.add("keyup "+evt.getCode());
        scene.repaint();*/
    }
    public static void blitFramebuffer(int x, int y, int w, int h, int ptr, int scansz)
    {
        if(ptr % 4 != 0)
            throw new RuntimeException("blitFramebuffer: unaligned pointer passed");
        instance.gui.gui = true;
        instance.gui.getFramebuffer().setRGB(x, y, w, h, CRunTime.memory, ptr / 4, scansz);
    }
    public static void repaint()
    {
        instance.scene.repaint();
    }
    public static int pollInput()
    {
        Object ans = instance.eq.get();
        if(ans == null)
            return 0;
        return ((Integer)ans).intValue();
    }
    public static Class getclass()
    {
        return MyXlet.class;
    }
    public static PrintStream getStdout()
    {
        return instance.console;
    }
    public static PrintStream getNetStdout()
    {
        if(instance.sock == null)
        {
            try
            {
                instance.sock = new PrintStream((new Socket("192.168.172.108", 12345)).getOutputStream());
            }
            catch(Exception e)
            {
                instance.printStackTrace(e);
                throw new RuntimeException("failed to connect socket");
            }
        }
        return instance.sock;
    }
    public static String getVFSRoot()
    {
        return "file://"+System.getProperty("dvb.persistent.root")
              +"/"+(String)instance.context.getXletProperty("dvb.org.id")
              +"/"+(String)instance.context.getXletProperty("dvb.app.id");
    }
    public static int strlenVFSRoot() throws Exception
    {
        return getVFSRoot().getBytes("UTF8").length;
    }
    public static void getVFSRoot(int buffer) throws Exception
    {
        byte[] data = (getVFSRoot()+"\u0000").getBytes("UTF8");
        CRunTime.memcpy(buffer, data, 0, data.length);
    }
}
