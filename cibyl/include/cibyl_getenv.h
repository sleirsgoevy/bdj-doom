#pragma once

char* Helper_vfsRoot();

static char* getenv(const char* name)
{
    if(!strcmp(name, "DOOMWADDIR"))
        return "resource://";
    if(!strcmp(name, "HOME"))
        return "/home/fake";
    if(!strcmp(name, "VFS_ROOT"))
        return Helper_vfsRoot();
    return NULL;
}

