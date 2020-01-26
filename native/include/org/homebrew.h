static void NOPH_MyXlet_blitFramebuffer(int x, int y, int w, int h, int* ptr, int scansize){}

static void NOPH_MyXlet_repaint(){}

int NOPH_MyXlet_pollInput();

int NOPH_PCMPlayer_register(char* name, void* ptr, int sz);
void NOPH_PCMPlayer_play(int idx);
