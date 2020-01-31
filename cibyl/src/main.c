#include <org/homebrew.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <assert.h>

int data[576*324];

int Helper_main(int argc, char** argv);

void test_memory()
{
    printf("test memory... ");
    int l = 0;
    int h = 16777216;
    while(h - l > 1)
    {
        int m = (l+h)/2;
        void* buf = malloc(m);
        if(buf)
        {
            free(buf);
            l = m;
        }
        else
            h = m;
    }
    printf("%d bytes available\n", l);
}

int main()
{
    assert(sizeof(long) == sizeof(int));
    test_memory();
    test_memory();
    fprintf(stdout, "test stdout\n");
    fprintf(stderr, "test stderr\n");
    printf("test printf\n");
    char doom_args[256];
    NOPH_MyXlet_getDoomCommandLine(doom_args);
    int l = strlen(doom_args);
    if(doom_args[l-1] == '\n')
        doom_args[--l] = 0;
    if(doom_args[l-1] == '\r')
        doom_args[--l] = 0;
    int doom_argc = 1;
    for(int i = 0; doom_args[i]; i++)
        if(doom_args[i] == ' ')
            doom_argc++;
    char** doom_argv = malloc(sizeof(char*)*(doom_argc+1));
    *doom_argv++ = doom_args;
    for(int i = 0; doom_args[i]; i++)
        if(doom_args[i] == ' ')
        {
            *doom_argv++ = doom_args + i + 1;
            doom_args[i] = 0;
        }
    *doom_argv = 0;
    doom_argv -= doom_argc;
    printf("cmdline:");
    for(int i = 0; i < doom_argc; i++)
        printf(" \"%s\"", doom_argv[i]);
    printf("\n");
    Helper_main(doom_argc, doom_argv);
    return 0;
}
