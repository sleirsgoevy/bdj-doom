#pragma once
#include <cibyl-syscall_defs.h>
#include <java/lang.h>
#include <java/io.h>

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

// returns MyXlet.class
NOPH_Class_t NOPH_MyXlet_getclass(void);

// returns an OutputStream for printing to the screen
NOPH_OutputStream_t NOPH_MyXlet_getStdout(void);

// get the VFS root, prefixed with 'file://'
int NOPH_MyXlet_strlenVFSRoot(void); /* Throws */
void NOPH_MyXlet_getVFSRoot(void* buf); /* Throws */

// write `sound.bdmv`-compatible file with this sound effect
//int NOPH_PCMWriter_writePCM(char* name, void* ptr, int sz);

// add name to the list of registered sounds
int NOPH_PCMPlayer_register(char* name, void* ptr, int sz);

// play sfx #idx
void NOPH_PCMPlayer_play(int idx); /* Throws */
