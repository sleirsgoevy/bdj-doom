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

#include <cibyl_getenv.h>

struct fd
{
    FILE* f;
    char* path;
    int mode;
    int offset;
};

static int open(const char* path, int mode, ...)
{
    FILE* f = fopen(path, mode?"w":"r");
    if(f == 0)
        return -1;
    struct fd* ans = malloc(sizeof(struct fd));
    ans->f = f;
    ans->path = strdup(path);
    ans->mode = mode;
    ans->offset = 0;
    return (int)ans;
}
#define O_RDONLY 0
#define O_BINARY 0
#define O_CREAT 0
#define O_TRUNC 0
#define O_WRONLY 1

static long read(int fd, void* buf, size_t count)
{
    long ans = fread(buf, 1, count, ((struct fd*)fd)->f);
    if(ans >= 0)
        ((struct fd*)fd)->offset += ans;
    return ans;
}

static long write(int fd, const void* buf, size_t count)
{
    long ans = fwrite(buf, 1, count, ((struct fd*)fd)->f);
    if(ans >= 0)
        ((struct fd*)fd)->offset += ans;
    return ans;
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
    if(offset < 0)
        return -1;
    struct fd* f = (struct fd*)fd;
    if(f->offset > offset)
    {
        fclose(f->f);
        f->f = fopen(f->path, f->mode?"w":"r");
        f->offset = 0;
    }
    char buf[256];
    while(f->offset != offset)
    {
        int left = offset - f->offset;
        if(left > 256)
            left = 256;
        int chunk_sz = read(fd, buf, left);
        if(chunk_sz < 0)
            return -1;
    }
    return offset;
}
