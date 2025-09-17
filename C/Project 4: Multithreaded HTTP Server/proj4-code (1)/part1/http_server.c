#define _GNU_SOURCE

#include <errno.h>
#include <netdb.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include "http.h"

#define BUFSIZE 512
#define LISTEN_QUEUE_LEN 5

int keep_going = 1;

void handle_sigint(int signo) {
    keep_going = 0;
}

int main(int argc, char **argv) {
    // First argument is directory to serve, second is port
    if (argc != 3) {
        printf("Usage: %s <directory> <port>\n", argv[0]);
        return 1;
    }
    // Uncomment the lines below to use these definitions:
    const char *serve_dir = argv[1];
    const char *port = argv[2];

    // TODO Complete the rest of this function
    // Change to the serving directory
    if (chdir(serve_dir) < 0) {
        perror("chdir");
        return 1;
    }

    // Setup signal handling with sigaction
    struct sigaction sa;
    sa.sa_handler = handle_sigint;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    if (sigaction(SIGINT, &sa, NULL) < 0) {
        perror("sigaction");
        return 1;
    }

    // Setup socket
    struct addrinfo hints = {0}, *res;
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    int ret = getaddrinfo(NULL, port, &hints, &res);
    if (ret != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(ret));
        return 1;
    }

    int listen_fd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (listen_fd < 0) {
        perror("socket");
        freeaddrinfo(res);
        return 1;
    }

    int yes = 1;
    if (setsockopt(listen_fd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes)) < 0) {
        perror("setsockopt");
    }

    if (bind(listen_fd, res->ai_addr, res->ai_addrlen) < 0) {
        perror("bind");
        close(listen_fd);
        freeaddrinfo(res);
        return 1;
    }

    if (listen(listen_fd, LISTEN_QUEUE_LEN) < 0) {
        perror("listen");
        close(listen_fd);
        freeaddrinfo(res);
        return 1;
    }

    freeaddrinfo(res);

    printf("HTTP server running on port %s, serving %s\n", port, serve_dir);

    // Accept and serve connections in a loop
    while (keep_going) {
        int conn_fd = accept(listen_fd, NULL, NULL);
        // Read and handle incoming HTTP request
        if (conn_fd < 0) {
            if (errno == EINTR)
                continue;    // Interrupted by signal
            perror("accept");
            break;
        }

        char resource[256];
        if (read_http_request(conn_fd, resource) == 0) {
            write_http_response(conn_fd, resource);
        }

        close(conn_fd);
    }

    close(listen_fd);
    printf("Server shutting down.\n");
    return 0;
}
