package org.homebrew;

import java.io.OutputStream;
import java.util.ArrayList;
import org.havi.ui.HScene;

public class MessagesOutputStream extends OutputStream
{
    ArrayList messages;
    HScene scene;
    String cur;
    public MessagesOutputStream(ArrayList msgs, HScene sc)
    {
        messages = msgs;
        scene = sc;
        cur = "";
        messages.add(cur);
    }
    public void write(int c)
    {
        if(c == 10)
        {
            scene.repaint();
            cur = "";
            messages.add(cur);
        }
        else if(c != 179)
        {
            cur += (char)c;
            messages.set(messages.size()-1, cur);
        }
    }
}
