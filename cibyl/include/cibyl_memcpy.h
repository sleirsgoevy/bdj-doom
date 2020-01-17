#include <string.h>
#include <stddef.h>
#include <ctype.h>
#define memcpy memcpy8
#define strcpy strcpy8
#define strncpy strncpy8
#define strcmp strcmp8
#define strncmp strncmp8
#define toupper toupper8

static void* memcpy(void* dst, void* src, size_t n)
{
    char* p1 = dst;
    char* p2 = src;
    for(int i = 0; i < n; i++)
        p1[i] = p2[i];
    return dst;
}

static char* strcpy(char* dst, const char* src)
{
    char* ans = dst;
    while(*dst++ = *src++);
    return ans;
}

static char* strncpy(char* dst, const char* src, size_t n)
{
    int i;
    for(i = 0; i < n && src[i]; i++)
        dst[i] = src[i];
    for(; i < n; i++)
        dst[i] = 0;
    return dst;
}

static int strcmp(const char* a, const char* b)
{
    while(*a && *a++ == *b++);
    return *a - *b;
}

static int strncmp(const char* a, const char* b, size_t n)
{
    int i;
    for(i = 0; i < n && a[i] && a[i] == b[i]; i++);
    if(i == n)
        return 0;
    return a[i] - b[i];
}

static int toupper(int c)
{
    if(c >= 'a' && c <= 'z')
        c += 'A' - 'a';
    return c;
}
