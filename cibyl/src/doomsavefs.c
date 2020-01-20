#include <cibyl-fileops.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <cibyl_getenv.h>
#include <java/lang.h>

static FILE* doomsavefs_open(const char* path0, cibyl_fops_open_mode_t mode)
{
    const char* mode_s = NULL;
    switch(mode)
    {
    case READ: mode_s = "r"; break;
    case READ_WRITE: mode_s = "r+"; break;
    case WRITE: mode_s = "w"; break;
    case APPEND: mode_s = "a"; break;
    case READ_APPEND: mode_s = "a+"; break;
    case READ_TRUNCATE: mode_s = "w+"; break;
    }
    const char* vfs_root = getenv("VFS_ROOT");
    char* path = malloc(strlen(vfs_root)+1+strlen(path0)+1);
    strcpy(path, vfs_root);
    strcat(path, "/");
    strcat(path, path0);
    FILE* ans = fopen(path, mode_s);
    if(!ans && mode != READ)
        NOPH_throw(NOPH_Exception_new_string(path));
    free(path);
    return ans;
}

static cibyl_fops_t doomsave_fops =
{
    .priv_data_size = 0,
    .keep_uri = 1,
    .open = doomsavefs_open,
    .close = NULL,
    .read = NULL,
    .write = NULL,
    .seek = NULL
};

static void __attribute__((constructor)) doomsave_register_fs()
{
    cibyl_fops_register("doomsav", &doomsave_fops, 0);
}
