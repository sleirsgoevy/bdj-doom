#pragma once

char* Helper_vfsRoot(int which);

static char* getenv(const char* name)
{
    if(!strcmp(name, "DOOMWADDIR"))
        return Helper_vfsRoot(1);//"resource://";
    if(!strcmp(name, "HOME"))
        return "/home/fake";
    if(!strcmp(name, "VFS_ROOT"))
        return Helper_vfsRoot(0);
    return NULL;
}

