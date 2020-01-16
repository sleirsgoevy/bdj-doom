#pragma once
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>

static int access(const char* fn, int mode)
{
    FILE* f = fopen(fn, "r");
    if(!f)
        return -1;
    fclose(f);
    return 0;
}
#define R_OK 0

static char* getenv(const char* name)
{
    if(!strcmp(name, "DOOMWADDIR"))
        return "resource://";
    if(!strcmp(name, "HOME"))
        return "/home/fake";
    return NULL;
}

struct fd
{
    FILE* f;
    char* path;
};

static int open(const char* path, int mode, ...)
{
    FILE* f = fopen(path, mode?"w":"r");
    if(f == 0)
        return -1;
    struct fd* ans = malloc(sizeof(struct fd));
    ans->f = f;
    ans->path = strdup(path);
    return (int)ans;
}
#define O_RDONLY 0
#define O_BINARY 0
#define O_CREAT 0
#define O_TRUNC 0
#define O_WRONLY 1

static long read(int fd, void* buf, size_t count)
{
    return fread(buf, 1, count, ((struct fd*)fd)->f);
}

static long write(int fd, const void* buf, size_t count)
{
    return fwrite(buf, 1, count, ((struct fd*)fd)->f);
}

static int close(int fd)
{
    struct fd* data = (struct fd*)fd;
    int ans = fclose(data->f);
    free(data->path);
    free(data);
    return ans;
}

struct stat
{
    int st_size;
};

static int fstat(int fd, struct stat* ans)
{
    const char* path = ((struct fd*)fd)->path;
    FILE* f = fopen(path, "r");
    if(!f)
        return -1;
    char buf[256];
    int sz = 0;
    int chunk;
    while((chunk = fread(buf, 1, 256, f)) > 0)
        sz += chunk;
    fclose(f);
    if(chunk < 0)
        return -1;
    ans->st_size = sz;
    return 0;
}

typedef int off_t;

static off_t lseek(int fd, off_t offset, int whence)
{
    int ans = fseek(((struct fd*)fd)->f, offset, whence);
    if(ans == 0)
        return ftell(((struct fd*)fd)->f);
    return ans;
}
