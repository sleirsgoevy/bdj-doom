#include <cibyl-memoryfs.h>
#include <cibyl-fileops.h>
#include <stdio.h>
#include <stdlib.h>

static FILE* memres_open(const char* path, cibyl_fops_open_mode_t mode)
{
    return NOPH_MemoryFile_openIndirect(path, "r");
}

static cibyl_fops_t memres_fops =
{
    .priv_data_size = 0,
    .open = memres_open,
    .close = NULL,
    .read = NULL,
    .write = NULL,
    .seek = NULL,
};

static void __attribute__((constructor)) memres_register_fs()
{
    cibyl_fops_register("mem:", &memres_fops, 0);
}
