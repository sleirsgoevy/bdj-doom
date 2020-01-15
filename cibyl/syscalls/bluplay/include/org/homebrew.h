#pragma once
#include <cibyl-syscall_defs.h>

void NOPH_MyXlet_blitFramebuffer(int x, int y, int w, int h, int* ptr, int scansize);
void NOPH_MyXlet_repaint(void);
