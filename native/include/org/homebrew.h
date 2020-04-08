static void NOPH_MyXlet_blitFramebuffer(int x, int y, int w, int h, int* ptr, int scansize){}

static void NOPH_MyXlet_repaint(){}

int NOPH_MyXlet_pollInput();

int NOPH_PCMPlayer_register(char* name, void* ptr, int sz);
void NOPH_PCMPlayer_play(int idx);

int NOPH_SocketHelper_initIPX(char* ip, int port);
int NOPH_SocketHelper_create(int port, int is_broadcast);
void NOPH_SocketHelper_registerPeer(char* ip, int port);
int NOPH_SocketHelper_sendto(int sock, void* buf, int sz, int peer);
int NOPH_SocketHelper_recvfrom(int sock, void* buf, int sz, int* peer);
void NOPH_SocketHelper_registerLastPeer(int sock, int port);
int NOPH_SocketHelper_getConsolePlayer();

static int NOPH_BackgroundMusic_registerSong(void* ptr, int sz)
{
    return 1;
}
static void NOPH_BackgroundMusic_unregisterSong(int handle){}
static void NOPH_BackgroundMusic_playSong(int handle, int looping){}
static void NOPH_BackgroundMusic_pauseSong(int handle){}
static void NOPH_BackgroundMusic_resumeSong(int handle){}
static void NOPH_BackgroundMusic_stopSong(int handle){}
static void NOPH_MUSFile_setVolume(int volume){}
