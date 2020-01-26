#include <SDL2/SDL.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>

void test_memory(){}

int Helper_main(int argc, char** argv);

static SDL_Window* win;
static SDL_Renderer* renderer;
static SDL_Texture* tex;

void Helper_blitScreen(char* fb, char* palette)
{
    static int fb2[320*200];
    for(int i = 0; i < 320*200; i++)
    {
        int r = 0xff&palette[3*(0xff&fb[i])];
        int g = 0xff&palette[3*(0xff&fb[i])+1];
        int b = 0xff&palette[3*(0xff&fb[i])+2];
        fb2[i] = 0xff000000|(r<<16)|(g<<8)|b;
    }
    SDL_UpdateTexture(tex, NULL, fb2, 1280);
    SDL_RenderClear(renderer);
    SDL_RenderCopy(renderer, tex, NULL, NULL);
    SDL_RenderPresent(renderer);
}

int NOPH_MyXlet_pollInput()
{
    SDL_Event e;
    while(SDL_PollEvent(&e))
    {
        int coef = 0;
        if(e.type == SDL_KEYDOWN && !e.key.repeat)
            coef = 1;
        else if(e.type == SDL_KEYUP)
            coef = -1;
        else
            continue;
        switch(e.key.keysym.sym)
        {
        case SDLK_LEFT:
            return coef*37;
        case SDLK_UP:
            return coef*38;
        case SDLK_RIGHT:
            return coef*39;
        case SDLK_DOWN:
            return coef*40;
        case SDLK_RETURN:
            return coef*10;
        case SDLK_LCTRL:
            return coef*417;
        case SDLK_ESCAPE:
            return coef*19;
        case SDLK_SPACE:
            return coef*461;
        case SDLK_1:
            return coef*424;
        case SDLK_9:
            return coef*412;
        }
    }
    return 0;
}

struct sound
{
    void* ptr_start;
    int len;
};

static struct sound* sound_vec;
static int sound_vec_cnt;
static int sound_vec_cap;

int NOPH_PCMPlayer_register(char* name, void* ptr, int sz)
{
    if(sound_vec_cnt == sound_vec_cap)
    {
        sound_vec_cap = sound_vec_cap * 2 + 1;
        sound_vec = realloc(sound_vec, sizeof(struct sound)*sound_vec_cap);
    }
    int ans = sound_vec_cnt++;
    sound_vec[ans] = (struct sound){ptr, sz};
    return ans;
}

void NOPH_PCMPlayer_play(int idx)
{
    struct sound snd = sound_vec[idx];
    if(fork())
        return;
    char* ptr = snd.ptr_start;
    int cnt = snd.len;
    int the_pipe[2];
    if(pipe(the_pipe))
        exit(1);
    if(!fork())
    {
        close(the_pipe[1]);
        dup2(the_pipe[0], 0);
        execlp("aplay", "aplay", "-r", "11025", "-f", "U8", "-c", "1", NULL);
        exit(1);
    }
    close(the_pipe[0]);
    while(cnt)
    {
        int chunk_sz = write(the_pipe[1], ptr, cnt);
        if(chunk_sz <= 0)
            exit(1);
        ptr += chunk_sz;
        cnt -= chunk_sz;
    }
    exit(0);
}

int main(int argc, char** argv)
{
    if(SDL_Init(SDL_INIT_EVERYTHING))
    {
        fprintf(stderr, "Failed to initialize SDL: %s\n", SDL_GetError());
        return 1;
    }
    win = SDL_CreateWindow("native", SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, 320, 200, 0);
    if(!win)
    {
        fprintf(stderr, "Failed to create window: %s\n", SDL_GetError());
        return 1;
    }
    renderer = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);
    tex = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_BGRA32, SDL_TEXTUREACCESS_STREAMING, 320, 200);
    signal(SIGINT, SIG_DFL);
    return Helper_main(argc, argv);
}
