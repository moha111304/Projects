#include "connection_queue.h"

#include <stdio.h>
#include <string.h>

// Initialize the connection queue and its synchronization primitives
int connection_queue_init(connection_queue_t *queue) {
    // TODO Not yet implemented
    if (!queue) {
        return -1;
    }

    // Zero out the queue structure
    memset(queue, 0, sizeof(connection_queue_t));
    queue->length = 0;
    queue->read_idx = 0;
    queue->write_idx = 0;
    queue->shutdown = 0;

    // Initialize mutex and condition variables
    if (pthread_mutex_init(&queue->mutex, NULL) != 0) {
        perror("pthread_mutex_init");
        return -1;
    }
    if (pthread_cond_init(&queue->not_empty, NULL) != 0) {
        perror("pthread_cond_init");
        return -1;
    }
    if (pthread_cond_init(&queue->not_full, NULL) != 0) {
        perror("pthread_cond_init");
        return -1;
    }

    return 0;
}

// Add a connection file descriptor to the queue (blocks if full)
int connection_queue_enqueue(connection_queue_t *queue, int connection_fd) {
    // TODO Not yet implemented
    if (!queue) {
        return -1;
    }

    pthread_mutex_lock(&queue->mutex);

    while (queue->length == CAPACITY && !queue->shutdown) {
        // Wait until there is space in the queue
        pthread_cond_wait(&queue->not_full, &queue->mutex);
    }

    if (queue->shutdown) {
        pthread_mutex_unlock(&queue->mutex);
        return -1;    // Queue is shutting down
    }

    // Add the connection_fd to the queue
    queue->client_fds[queue->write_idx] = connection_fd;
    queue->write_idx = (queue->write_idx + 1) % CAPACITY;
    queue->length++;

    // Signal that the queue is not empty
    pthread_cond_signal(&queue->not_empty);

    pthread_mutex_unlock(&queue->mutex);
    return 0;
}

// Remove and return a connection file descriptor from the queue (blocks if empty)
int connection_queue_dequeue(connection_queue_t *queue) {
    // TODO Not yet implemented
    if (!queue) {
        return -1;
    }

    pthread_mutex_lock(&queue->mutex);

    while (queue->length == 0 && !queue->shutdown) {
        // Wait until there is an item to dequeue
        pthread_cond_wait(&queue->not_empty, &queue->mutex);
    }

    if (queue->shutdown) {
        pthread_mutex_unlock(&queue->mutex);
        return -1;    // Queue is shutting down
    }

    // Remove the connection_fd from the queue
    int client_fd = queue->client_fds[queue->read_idx];
    queue->read_idx = (queue->read_idx + 1) % CAPACITY;
    queue->length--;

    // Signal that the queue is not full
    pthread_cond_signal(&queue->not_full);

    pthread_mutex_unlock(&queue->mutex);
    return client_fd;
}

// Shut down the queue and wake up all waiting threads
int connection_queue_shutdown(connection_queue_t *queue) {
    // TODO Not yet implemented
    if (!queue) {
        return -1;
    }

    pthread_mutex_lock(&queue->mutex);
    queue->shutdown = 1;

    // Signal to unblock any waiting threads
    pthread_cond_broadcast(&queue->not_empty);
    pthread_cond_broadcast(&queue->not_full);

    pthread_mutex_unlock(&queue->mutex);
    return 0;
}

// Clean up resources associated with the queue
int connection_queue_free(connection_queue_t *queue) {
    // TODO Not yet implemented
    if (!queue) {
        return -1;
    }

    pthread_mutex_destroy(&queue->mutex);
    pthread_cond_destroy(&queue->not_empty);
    pthread_cond_destroy(&queue->not_full);
    return 0;
}
