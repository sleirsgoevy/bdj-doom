#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>

int NOPH_SocketHelper_create(int port)
{
    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    fcntl(sock, F_SETFL, O_NONBLOCK);
    if(sock < 0)
        return -1;
    struct sockaddr_in addr = {.sin_family = AF_INET, .sin_addr = {.s_addr = 0}, .sin_port = htons(port)};
    if(bind(sock, (struct sockaddr*)&addr, sizeof(addr)))
    {
        close(sock);
        return -1;
    }
    return sock;
}

static in_addr_t* peers;
static short* ports;
static int peern;
static int peercap;

void NOPH_SocketHelper_registerPeer(const char* peer, int port)
{
    if(peern == peercap)
    {
        peercap = 2 * peercap + 1;
        peers = realloc(peers, peercap * sizeof(in_addr_t));
        peers[peern] = inet_addr(peer);
        ports = realloc(ports, peercap * sizeof(short));
        ports[peern++] = port;
    }
}

int NOPH_SocketHelper_sendto(int sock, const void* buf, int len, int peer)
{
    struct sockaddr_in addr = {.sin_family = AF_INET};
    peer--;
    if(peer >= 0 && peer < peern)
    { 
        addr.sin_addr.s_addr = peers[peer];
        addr.sin_port = ports[peer];
    }
    else
    {
        addr.sin_addr.s_addr = -1;
        addr.sin_port = 5029;
    }
    addr.sin_port = htons(addr.sin_port);
    return sendto(sock, buf, len, 0, (struct sockaddr*)&addr, sizeof(addr));
}

int NOPH_SocketHelper_recvfrom(int sock, void* buf, int len, int* peer)
{
    struct sockaddr_in addr;
    socklen_t addrlen = sizeof(addr);
    int ans = recvfrom(sock, buf, len, 0, (struct sockaddr*)&addr, &addrlen);
    if(ans < 0)
        return ans;
    int i = 0;
    addr.sin_port = htons(addr.sin_port);
    for(i = 0; i < peern; i++)
        if(peers[i] == addr.sin_addr.s_addr && ports[i] == addr.sin_port)
            break;
    i++;
    *peer = i;
    return ans;
}

// stubs (for debugging)
/*
int NOPH_SocketHelper_create(int port)
{
    return 0;
}

void NOPH_SocketHelper_registerPeer(char* addr, int port){}

int NOPH_SocketHelper_sendto(int sock, void* addr, int sz, int peer)
{
    return sz;
}

int NOPH_SocketHelper_recvfrom(int sock, void* addr, int sz, int* peer)
{
    return -1;
}*/
