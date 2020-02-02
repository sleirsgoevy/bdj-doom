static void NOPH_MyXlet_blitFramebuffer(int x, int y, int w, int h, int* ptr, int scansize){}

static void NOPH_MyXlet_repaint(){}

int NOPH_MyXlet_pollInput();

int NOPH_PCMPlayer_register(char* name, void* ptr, int sz);
void NOPH_PCMPlayer_play(int idx);

int NOPH_SocketHelper_create(int port, int is_broadcast);
void NOPH_SocketHelper_registerPeer(char* ip, int port);
int NOPH_SocketHelper_sendto(int sock, void* buf, int sz, int peer);
int NOPH_SocketHelper_recvfrom(int sock, void* buf, int sz, int* peer);
void NOPH_SocketHelper_registerLastPeer(int sock, int port);
int NOPH_SocketHelper_getConsolePlayer();
