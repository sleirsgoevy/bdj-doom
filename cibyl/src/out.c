#include <java/io.h>
#include <org/homebrew.h>
#include <stdio.h>

FILE* open_console()
{
    return NOPH_OutputStream_createFILE(NOPH_MyXlet_getStdout());
}
