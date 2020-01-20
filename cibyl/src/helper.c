#include <org/homebrew.h>
#include <stdlib.h>

void Helper_blitScreen(char* screen, char* palette)
{
    static int screen2[320*200];
    for(int i = 0; i < 320*200; i++)
    {
        int r = 0xff&palette[3*(0xff&screen[i])];
        int g = 0xff&palette[3*(0xff&screen[i])+1];
        int b = 0xff&palette[3*(0xff&screen[i])+2];
        screen2[i] = 0xff000000|(r<<16)|(g<<8)|b;
    }
    NOPH_MyXlet_blitFramebuffer(0, 0, 320, 200, screen2, 320);
    NOPH_MyXlet_repaint();
}

char* Helper_vfsRoot()
{
    static char* ans;
    if(!ans)
    {
        int sz = NOPH_MyXlet_strlenVFSRoot();
        ans = malloc(sz + 1);
        NOPH_MyXlet_getVFSRoot(ans);
    }
    return ans;
}
