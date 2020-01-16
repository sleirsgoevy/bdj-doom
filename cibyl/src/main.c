#include <org/homebrew.h>
#include <math.h>

int data[576*324];

int main()
{
    //double h0 = sqrt(576*576+324*324);
    int q = 0;
    int color_change_speed = 0;
    for(;;)
    {
        int key;
        while((key = NOPH_MyXlet_pollInput()))
            switch(key)
            {
            case 37: // left arrow
            case -39:
                color_change_speed = (color_change_speed + 767) % 768;
                break;
            case 39: // right arrow
            case -37:
                color_change_speed = (color_change_speed + 1) % 768;
                break;
            }
        q = (q + color_change_speed) % 768;
        int qr, qg, qb;
        if(q < 256)
        {
            qr = 256 - q;
            qg = q;
            qb = 0;
        }
        else if(q < 512)
        {
            qr = 0;
            qg = 512 - q;
            qb = q - 256;
        }
        else
        {
            qr = q - 512;
            qg = 0;
            qb = 768 - q;
        }
        for(int y = 0; y < 324; y++)
            for(int x = 0; x < 576; x++)
            {
                //double qd = sqrt(y*y+x*x);
                data[y*576+x] = 0xff000000
                              | (qr*(x+y))/900 << 16
                              | (qg*(x+y))/900 << 8
                              | (qb*(x+y))/900;
            }
        NOPH_MyXlet_blitFramebuffer(0, 0, 576, 324, data, 576);
        NOPH_MyXlet_repaint();
    }
    return 0;
}
