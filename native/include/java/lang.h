#include <sys/time.h>
#include <unistd.h>

static long long NOPH_System_currentTimeMillis()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000ll + tv.tv_usec / 1000ll;
}

static void NOPH_Thread_sleep(long long millis)
{
    usleep(millis * 1000);
}
