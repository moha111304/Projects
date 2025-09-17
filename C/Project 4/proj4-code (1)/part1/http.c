#include "http.h"

#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#define BUFSIZE 512

const char *get_mime_type(const char *file_extension) {
    if (strcmp(".txt", file_extension) == 0) {
        return "text/plain";
    } else if (strcmp(".html", file_extension) == 0) {
        return "text/html";
    } else if (strcmp(".jpg", file_extension) == 0) {
        return "image/jpeg";
    } else if (strcmp(".png", file_extension) == 0) {
        return "image/png";
    } else if (strcmp(".pdf", file_extension) == 0) {
        return "application/pdf";
    } else if (strcmp(".mp3", file_extension) == 0) {
        return "audio/mpeg";
    }

    return NULL;
}

int read_http_request(int fd, char *resource_name) {
    // TODO Not yet implemented
    char buf[BUFSIZE];
    int bytes_read = 0;
    int total_read = 0;

    // Read until we hit \r\n\r\n or fill the buffer
    while ((bytes_read = read(fd, buf + total_read, BUFSIZE - total_read - 1)) > 0) {
        total_read += bytes_read;
        buf[total_read] = '\0';
        // Check for end of HTTP headers
        if (strstr(buf, "\r\n\r\n")) {
            break;
        }
        if (total_read >= BUFSIZE - 1) {
            break;
        }
    }

    if (bytes_read < 0) {
        perror("read");
        return -1;
    }

    // Parse the first line
    char method[8], path[256];
    // Parse HTTP method and resource path from the request line
    if (sscanf(buf, "%s %s", method, path) != 2) {
        fprintf(stderr, "Invalid request format\n");
        return -1;
    }

    if (strcmp(method, "GET") != 0) {
        fprintf(stderr, "Unsupported HTTP method: %s\n", method);
        return -1;
    }

    // Remove leading slash
    if (path[0] == '/') {
        strncpy(resource_name, path + 1, 255);
        resource_name[255] = '\0';
    } else {
        strncpy(resource_name, path, 255);
        resource_name[255] = '\0';
    }

    return 0;
}

int write_http_response(int fd, const char *resource_path) {
    // TODO Not yet implemented
    int file_fd = open(resource_path, O_RDONLY);
    if (file_fd < 0) {
        // File not found
        const char *response =
            "HTTP/1.0 404 Not Found\r\n"
            "Content-Length: 0\r\n\r\n";
        write(fd, response, strlen(response));
        return -1;
    }

    struct stat st;
    if (fstat(file_fd, &st) < 0) {
        perror("fstat");
        close(file_fd);
        return -1;
    }

    const char *ext = strrchr(resource_path, '.');
    const char *mime_type = get_mime_type(ext ? ext : "");

    if (!mime_type) {
        fprintf(stderr, "Unknown MIME type for %s\n", resource_path);
        close(file_fd);
        return -1;
    }

    // Write HTTP 200 OK response header
    dprintf(fd,
            "HTTP/1.0 200 OK\r\n"
            "Content-Length: %ld\r\n"
            "Content-Type: %s\r\n"
            "\r\n",
            st.st_size, mime_type);

    // Send file in chunks
    char buf[BUFSIZE];
    ssize_t n;
    while ((n = read(file_fd, buf, BUFSIZE)) > 0) {
        if (write(fd, buf, n) != n) {
            perror("write");
            close(file_fd);
            return -1;
        }
    }

    if (n < 0) {
        perror("read file");
    }

    close(file_fd);
    return 0;
}
