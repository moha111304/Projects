#define _GNU_SOURCE

#include <errno.h>
#include <netdb.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include "connection_queue.h"
#include "http.h"

#define BUFSIZE 512
#define LISTEN_QUEUE_LEN 5
#define N_THREADS 5

int keep_going = 1;
const char *serve_dir;

connection_queue_t conn_queue;
pthread_t workers[N_THREADS];

void handle_sigint(int signo) {
    (void) signo;
    keep_going = 0;
    connection_queue_shutdown(&conn_queue);
}

// Worker thread function fot help
void *worker_thread(void *arg) {
    (void) arg;
    char resource_name[512];    // Buffer to hold the resource name

    while (1) {
        int client_fd = connection_queue_dequeue(&conn_queue);
        if (client_fd < 0) {
            break;    // Queue is shutting down
        }

        // Read the HTTP request and extract the resource name
        if (read_http_request(client_fd, resource_name) == 0) {
            // Generate the file path from the resource name
            // Assuming resources are directly mapped to file paths
            if (write_http_response(client_fd, resource_name) != 0) {
                fprintf(stderr, "Failed to send response for %s\n", resource_name);
            }
        } else {
            fprintf(stderr, "Failed to read HTTP request\n");
        }

        // Close the connection after sending the response
        close(client_fd);
    }

    return NULL;
}

int main(int argc, char **argv) {
    // First argument is directory to serve, second is port
    if (argc != 3) {
        printf("Usage: %s <directory> <port>\n", argv[0]);
        return 1;
    }
    // Uncomment the lines below to use these definitions:
    serve_dir = argv[1];
    const char *port = argv[2];

    // TODO Implement the rest of this function
    // Block all signals before creating threads
    sigset_t full_mask, prev_mask;
    sigfillset(&full_mask);
    pthread_sigmask(SIG_BLOCK, &full_mask, &prev_mask);

    // Set up signal handler
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = handle_sigint;
    sigaction(SIGINT, &sa, NULL);

    // Change to serve directory
    if (chdir(serve_dir) != 0) {
        perror("chdir");
        return 1;
    }

    // Initialize thread-safe connection queue
    connection_queue_init(&conn_queue);

    // Create worker threads
    for (int i = 0; i < N_THREADS; i++) {
        pthread_create(&workers[i], NULL, worker_thread, NULL);
    }

    // Restore signal mask to allow SIGINT in main thread
    pthread_sigmask(SIG_SETMASK, &prev_mask, NULL);

    // Set up listening socket
    struct addrinfo hints, *res;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    int err = getaddrinfo(NULL, port, &hints, &res);
    if (err != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(err));
        return 1;
    }

    int listen_fd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (listen_fd < 0) {
        perror("socket");
        freeaddrinfo(res);
        return 1;
    }

    int optval = 1;
    setsockopt(listen_fd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval));

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

    // Accept loop
    while (keep_going) {
        int client_fd = accept(listen_fd, NULL, NULL);
        if (client_fd < 0) {
            if (errno == EINTR)
                continue;    // Interrupted by signal
            perror("accept");
            break;
        }

        if (connection_queue_enqueue(&conn_queue, client_fd) < 0) {
            close(client_fd);    // Queue shutting down
        }
    }

    // Clean up
    close(listen_fd);

    for (int i = 0; i < N_THREADS; i++) {
        pthread_join(workers[i], NULL);
    }

    connection_queue_free(&conn_queue);
    return 0;
}
