#pragma once
#include <cibyl-syscall_defs.h>
#include <java/lang.h>
#include <java/io.h>

/* GUI */

// forwards to DVBBufferedImage::setRGB(int, int, int, int, int[], int, int)
void NOPH_MyXlet_blitFramebuffer(int x, int y, int w, int h, int* ptr, int scansize);

// calls scene.repaint()
void NOPH_MyXlet_repaint(void);

/* Check for key events
   Returns:
     -keyCode on KEY_RELEASED
     +keyCode on KEY_PRESSED
     0 if there are no pending events
*/
int NOPH_MyXlet_pollInput(void);

/* stdio */

// returns MyXlet.class
NOPH_Class_t NOPH_MyXlet_getclass(void);

// returns an OutputStream for printing to the screen
NOPH_OutputStream_t NOPH_MyXlet_getStdout(void);

// get the VFS root, prefixed with 'file://'
int NOPH_MyXlet_strlenVFSRoot(void); /* Throws */
void NOPH_MyXlet_getVFSRoot(void* buf); /* Throws */

// write `sound.bdmv`-compatible file with this sound effect
//int NOPH_PCMWriter_writePCM(char* name, void* ptr, int sz);

/* sound */

// add name to the list of registered sounds
int NOPH_PCMPlayer_register(char* name, void* ptr, int sz);

// play sfx #idx
void NOPH_PCMPlayer_play(int idx); /* Throws */

/* network */

// create socket & bind on port
int NOPH_SocketHelper_create(int port);

// sendto
int NOPH_SocketHelper_sendto(int sock, const void* buf, int len, int peer); /* Throws */

// recvfrom
int NOPH_SocketHelper_recvfrom(int sock, void* buf, int len, int* peer);

// register peer
void NOPH_SocketHelper_registerPeer(char* ip, int port); /* Throws */

// get DOOM command line
void NOPH_MyXlet_getDoomCommandLine(void* buf);
