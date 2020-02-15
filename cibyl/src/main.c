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
    int doom_argc = 3;
    for(int i = 0; doom_args[i]; i++)
        if(doom_args[i] == ' ')
            doom_argc++;
    int real_doom_argc = doom_argc - 2;
    char** doom_argv = malloc(sizeof(char*)*(doom_argc+1));
    *doom_argv++ = doom_args;
    for(int i = 0; doom_args[i]; i++)
        if(doom_args[i] == ' ')
        {
            *doom_argv++ = doom_args + i + 1;
            doom_args[i] = 0;
        }
    for(int i = 0; i < 2; i++)
        *doom_argv++ = 0;
    *doom_argv = 0;
    doom_argv -= doom_argc;
    char q_buf[3];
    if(!strcmp(doom_argv[real_doom_argc - 1], "-launcher"))
    {
        printf("Select a mode you want to play:\n");
        printf("* X = singleplayer\n");
        printf("* [] = LAN multiplayer (UDP broadcast)\n");
        int key = 0;
        do
            key = NOPH_MyXlet_pollInput();
        while(key != 10 && key != 461);
        if(key == 10)
            doom_argv[--real_doom_argc] = 0;
        else
        {
            doom_argv[real_doom_argc-1] = "-net";
            printf("Select number of players:\n");
            printf("* LEFT = 2 players\n");
            printf("* DOWN = 3 players\n");
            printf("* RIGHT = 4 players\n");
            do
                key = NOPH_MyXlet_pollInput();
            while(key != 37 && key != 40 && key != 39);
            q_buf[0] = '?';
            q_buf[2] = 0;
            if(key == 37)
                q_buf[1] = '2';
            else if(key == 40)
                q_buf[1] = '3';
            else if(key == 39)
                q_buf[1] = '4';
            doom_argv[real_doom_argc++] = q_buf;
            printf("Select gamemode:\n");
            printf("* X = cooperative\n");
            printf("* [] = deathmatch\n");
            do
                key = NOPH_MyXlet_pollInput();
            while(key != 10 && key != 461);
            if(key == 461)
                doom_argv[real_doom_argc++] = "-deathmatch";
        }
    }
    printf("cmdline:");
    for(int i = 0; i < real_doom_argc; i++)
        printf(" \"%s\"", doom_argv[i]);
    printf("\n");
    Helper_main(real_doom_argc, doom_argv);
    return 0;
}
