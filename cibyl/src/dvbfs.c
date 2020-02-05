#include <cibyl-fileops.h>
#include <org/homebrew.h>
#include <cibyl.h>

static FILE* dvb_open(const char* path, cibyl_fops_open_mode_t mode)
{
    NOPH_InputStream_t ans;
    int error = 0;
    NOPH_try(NOPH_setter_exception_handler, (void*)&error)
    {
        ans = NOPH_DVBCachedFile_open(path);
    }
    NOPH_catch_exception(java/io/IOException);
    if(error || ans == 0)
        return NULL;
    return NOPH_InputStream_createFILE(ans);
}

static cibyl_fops_t dvbfs_fops = {
    .priv_data_size = 0,
    .open = dvb_open,
    .close = NULL,
    .read = NULL,
    .write = NULL,
    .seek = NULL
};

static __attribute__((constructor)) void dvbfs_setup()
{
    cibyl_fops_register("dvbfs://", &dvbfs_fops, 1);
}
