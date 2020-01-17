package org.homebrew;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

import org.dvb.ui.DVBBufferedImage;

public class Screen extends Container
{
    private static final long serialVersionUID = 4761178503523947426L;
    private ArrayList messages;
    private Font font;
    private DVBBufferedImage fb;
    public int top;
    public boolean gui = false;
    public Screen(ArrayList messages)
    {
        this.messages = messages;
        font = new Font(null, Font.PLAIN, 36);
        fb = new DVBBufferedImage(576, 324);
    }
    public void paint(Graphics g)
    {
        /*Graphics offG = fb.getGraphics();
        offG.setFont(font); */
        if(gui)
        {
            g.drawImage(fb, 0, 0, 1920, 1080, 0, 0, 576, 324, null);
            return;
        }
        g.setColor(new Color(100, 110, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(font);
        g.setColor(new Color(255, 255, 255));
        for(int i = 0; i < messages.size(); i++)
        {
            String message = (String)messages.get(i);
            int message_width = g.getFontMetrics().stringWidth(message);
            g.drawString(message, 0, top + (i*40));
        }
    }
    public DVBBufferedImage getFramebuffer()
    {
        return fb;
    }
}
