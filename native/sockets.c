#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <net/if.h>
#include <linux/sockios.h>
#include <string.h>
#include <sys/ioctl.h>

static in_addr_t getOwnIP(int is_broadcast)
{
    if(is_broadcast)
        return -1;
    static in_addr_t ans;
    if(ans)
        return ans;
    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    DIR* sys_class_net = opendir("/sys/class/net");
    struct dirent* dent;
    while((dent = readdir(sys_class_net)))
    {
        struct ifreq ifr;
        strncpy(ifr.ifr_name, dent->d_name, IFNAMSIZ-1);
        ioctl(sock, SIOCGIFADDR, &ifr);
        in_addr_t addr = ((struct sockaddr_in*)&ifr.ifr_addr)->sin_addr.s_addr;
        char* a = (char*)&addr;
        if((a[0] == (char)192 && a[1] == (char)168) || (a[0] == (char)172 && a[1] == (char)16) || a[0] == (char)10)
        {
            closedir(sys_class_net);
            close(sock);
            return ans = addr;
        }
    }
    exit(1);
}

int NOPH_SocketHelper_create(int port, int is_broadcast)
{
    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    fcntl(sock, F_SETFL, O_NONBLOCK);
    if(sock < 0)
        return -1;
    struct sockaddr_in addr = {.sin_family = AF_INET, .sin_addr = {.s_addr = is_broadcast?0:getOwnIP(is_broadcast)}, .sin_port = htons(port)};
    if(bind(sock, (struct sockaddr*)&addr, sizeof(addr)))
    {
        close(sock);
        return -1;
    }
    int val = 1;
    setsockopt(sock, SOL_SOCKET, SO_BROADCAST, &val, sizeof(int));
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
    socklen_t l = sizeof(addr);
    getsockname(sock, &addr, &l);
    peer--;
    if(peer >= 0 && peer < peern)
    { 
        addr.sin_addr.s_addr = peers[peer];
        addr.sin_port = htons(ports[peer]);
    }
    else
    {
        addr.sin_addr.s_addr = -1;
        addr.sin_port = htons(htons(addr.sin_port) + 1);
    }
    return sendto(sock, buf, len, 0, (struct sockaddr*)&addr, sizeof(addr));
}

static struct sockaddr_in last_addr;

int NOPH_SocketHelper_recvfrom(int sock, void* buf, int len, int* peer)
{
    struct sockaddr_in addr;
    socklen_t addrlen = sizeof(addr);
    int ans = recvfrom(sock, buf, len, 0, (struct sockaddr*)&addr, &addrlen);
    if(ans < 0)
        return ans;
    if(addr.sin_addr.s_addr == getOwnIP(0))
        return -1;
    last_addr = addr;
    int i = 0;
    addr.sin_port = htons(addr.sin_port);
    for(i = 0; i < peern; i++)
        if(peers[i] == addr.sin_addr.s_addr && ports[i] == addr.sin_port)
            break;
    i++;
    *peer = i;
    return ans;
}

void NOPH_SocketHelper_registerLastPeer(int sock, int port)
{
    NOPH_SocketHelper_registerPeer("0.0.0.0", 0);
    peers[peern-1] = last_addr.sin_addr.s_addr;
    ports[peern-1] = port;
}

int NOPH_SocketHelper_getConsolePlayer()
{
    int ans = 0;
    in_addr_t own = getOwnIP(0);
    for(int i = 0; i < peern; i++)
    {
        signed char* a = (signed char*)&own;
        signed char* b = (signed char*)(peers+i);
        int diff = 0;
        for(int i = 0; i < 4 && diff == 0; i++)
            diff = a[i] - b[i];
        if(diff > 0)
            ans++;
    }
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
