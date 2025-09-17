#include "minitar.h"

#include <fcntl.h>
#include <grp.h>
#include <math.h>
#include <pwd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/sysmacros.h>
#include <sys/types.h>
#include <unistd.h>

#define NUM_TRAILING_BLOCKS 2
#define MAX_MSG_LEN 128
#define BLOCK_SIZE 512

// Constants for tar compatibility information
#define MAGIC "ustar"

// Constants to represent different file types
// We'll only use regular files in this project
#define REGTYPE '0'
#define DIRTYPE '5'

/*
 * Helper function to compute the checksum of a tar header block
 * Performs a simple sum over all bytes in the header in accordance with POSIX
 * standard for tar file structure.
 */
void compute_checksum(tar_header *header) {
    // Have to initially set header's checksum to "all blanks"
    memset(header->chksum, ' ', 8);
    unsigned sum = 0;
    char *bytes = (char *) header;
    for (int i = 0; i < sizeof(tar_header); i++) {
        sum += bytes[i];
    }
    snprintf(header->chksum, 8, "%07o", sum);
}

/*
 * Populates a tar header block pointed to by 'header' with metadata about
 * the file identified by 'file_name'.
 * Returns 0 on success or -1 if an error occurs
 */
int fill_tar_header(tar_header *header, const char *file_name) {
    memset(header, 0, sizeof(tar_header));
    char err_msg[MAX_MSG_LEN];
    struct stat stat_buf;
    // stat is a system call to inspect file metadata
    if (stat(file_name, &stat_buf) != 0) {
        snprintf(err_msg, MAX_MSG_LEN, "Failed to stat file %s", file_name);
        perror(err_msg);
        return -1;
    }

    strncpy(header->name, file_name, 100);    // Name of the file, null-terminated string
    snprintf(header->mode, 8, "%07o",
             stat_buf.st_mode & 07777);    // Permissions for file, 0-padded octal

    snprintf(header->uid, 8, "%07o", stat_buf.st_uid);    // Owner ID of the file, 0-padded octal
    struct passwd *pwd = getpwuid(stat_buf.st_uid);       // Look up name corresponding to owner ID
    if (pwd == NULL) {
        snprintf(err_msg, MAX_MSG_LEN, "Failed to look up owner name of file %s", file_name);
        perror(err_msg);
        return -1;
    }
    strncpy(header->uname, pwd->pw_name, 32);    // Owner name of the file, null-terminated string

    snprintf(header->gid, 8, "%07o", stat_buf.st_gid);    // Group ID of the file, 0-padded octal
    struct group *grp = getgrgid(stat_buf.st_gid);        // Look up name corresponding to group ID
    if (grp == NULL) {
        snprintf(err_msg, MAX_MSG_LEN, "Failed to look up group name of file %s", file_name);
        perror(err_msg);
        return -1;
    }
    strncpy(header->gname, grp->gr_name, 32);    // Group name of the file, null-terminated string

    snprintf(header->size, 12, "%011o",
             (unsigned) stat_buf.st_size);    // File size, 0-padded octal
    snprintf(header->mtime, 12, "%011o",
             (unsigned) stat_buf.st_mtime);    // Modification time, 0-padded octal
    header->typeflag = REGTYPE;                // File type, always regular file in this project
    strncpy(header->magic, MAGIC, 6);          // Special, standardized sequence of bytes
    memcpy(header->version, "00", 2);          // A bit weird, sidesteps null termination
    snprintf(header->devmajor, 8, "%07o",
             major(stat_buf.st_dev));    // Major device number, 0-padded octal
    snprintf(header->devminor, 8, "%07o",
             minor(stat_buf.st_dev));    // Minor device number, 0-padded octal

    compute_checksum(header);
    return 0;
}

/*
 * Removes 'nbytes' bytes from the file identified by 'file_name'
 * Returns 0 upon success, -1 upon error
 * Note: This function uses lower-level I/O syscalls (not stdio), which we'll learn about later
 */
int remove_trailing_bytes(const char *file_name, size_t nbytes) {
    char err_msg[MAX_MSG_LEN];

    struct stat stat_buf;
    if (stat(file_name, &stat_buf) != 0) {
        snprintf(err_msg, MAX_MSG_LEN, "Failed to stat file %s", file_name);
        perror(err_msg);
        return -1;
    }

    off_t file_size = stat_buf.st_size;
    if (nbytes > file_size) {
        file_size = 0;
    } else {
        file_size -= nbytes;
    }

    if (truncate(file_name, file_size) != 0) {
        snprintf(err_msg, MAX_MSG_LEN, "Failed to truncate file %s", file_name);
        perror(err_msg);
        return -1;
    }
    return 0;
}

int create_archive(const char *archive_name, const file_list_t *files) {
    int archive_fd = open(archive_name, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (archive_fd < 0) {
        perror("Failed to open archive for writing");
        return -1;
    }

    node_t *current = files->head;
    while (current) {
        tar_header header;
        struct stat stat_buf;

        // get file metadata
        if (stat(current->name, &stat_buf) != 0) {
            perror("Failed to stat file");
            close(archive_fd);
            return -1;
        }

        // fill the tar header
        if (fill_tar_header(&header, current->name) != 0) {
            close(archive_fd);
            return -1;
        }

        // write header
        if (write(archive_fd, &header, sizeof(tar_header)) != sizeof(tar_header)) {
            perror("Error writing file header");
            close(archive_fd);
            return -1;
        }

        int file_fd = open(current->name, O_RDONLY);
        if (file_fd < 0) {
            perror("Failed to open file for reading");
            close(archive_fd);
            return -1;
        }

        char buffer[BLOCK_SIZE] = {0};
        ssize_t bytes_read;

        // write file content
        while ((bytes_read = read(file_fd, buffer, BLOCK_SIZE)) > 0) {
            if (write(archive_fd, buffer, bytes_read) != bytes_read) {
                perror("Error writing file contents to archive");
                close(file_fd);
                close(archive_fd);
                return -1;
            }
        }

        // compute padding to align to 512 bytes IMPORTANT
        size_t padding = (BLOCK_SIZE - (stat_buf.st_size % BLOCK_SIZE)) % BLOCK_SIZE;
        if (padding > 0) {
            memset(buffer, 0, BLOCK_SIZE);
            if (write(archive_fd, buffer, padding) != padding) {
                perror("Error writing padding to archive");
                close(file_fd);
                close(archive_fd);
                return -1;
            }
        }

        close(file_fd);
        current = current->next;
    }

    // write the required two empty blocks as the footer
    char footer[BLOCK_SIZE * 2] = {0};
    if (write(archive_fd, footer, BLOCK_SIZE * 2) != BLOCK_SIZE * 2) {
        perror("Failed to write archive footer");
        close(archive_fd);
        return -1;
    }

    close(archive_fd);
    return 0;
}

int append_files_to_archive(const char *archive_name, const file_list_t *files) {
    // remove existing footer (last 2 blocks)
    if (remove_trailing_bytes(archive_name, BLOCK_SIZE * NUM_TRAILING_BLOCKS) != 0) {
        perror("Failed to remove archive footer");
        return -1;
    }

    int archive_fd = open(archive_name, O_WRONLY | O_APPEND);
    if (archive_fd < 0) {
        perror("Failed to open archive for appending");
        return -1;
    }

    node_t *current = files->head;
    while (current) {
        tar_header header;
        struct stat stat_buf;

        // get file metadata
        if (stat(current->name, &stat_buf) != 0) {
            perror("Failed to stat file");
            close(archive_fd);
            return -1;
        }

        // fill the tar header
        if (fill_tar_header(&header, current->name) != 0) {
            close(archive_fd);
            return -1;
        }

        // write header block
        if (write(archive_fd, &header, sizeof(tar_header)) != sizeof(tar_header)) {
            perror("Error writing file header");
            close(archive_fd);
            return -1;
        }

        int file_fd = open(current->name, O_RDONLY);
        if (file_fd < 0) {
            perror("Failed to open file for reading");
            close(archive_fd);
            return -1;
        }

        char buffer[BLOCK_SIZE] = {0};
        ssize_t bytes_read;

        // write file content
        while ((bytes_read = read(file_fd, buffer, BLOCK_SIZE)) > 0) {
            if (write(archive_fd, buffer, bytes_read) != bytes_read) {
                perror("Error writing file contents to archive");
                close(file_fd);
                close(archive_fd);
                return -1;
            }
        }

        // compute padding to align to 512 bytes IMPORTANT
        size_t padding = (BLOCK_SIZE - (stat_buf.st_size % BLOCK_SIZE)) % BLOCK_SIZE;
        if (padding > 0) {
            memset(buffer, 0, BLOCK_SIZE);
            if (write(archive_fd, buffer, padding) != padding) {
                perror("Error writing padding to archive");
                close(file_fd);
                close(archive_fd);
                return -1;
            }
        }

        close(file_fd);
        current = current->next;
    }

    // add the two-block footer at the end of the archive once more
    char footer[BLOCK_SIZE * 2] = {0};
    if (write(archive_fd, footer, BLOCK_SIZE * 2) != BLOCK_SIZE * 2) {
        perror("Failed to write archive footer");
        close(archive_fd);
        return -1;
    }

    close(archive_fd);
    return 0;
}

int get_archive_file_list(const char *archive_name, file_list_t *files) {
    int archive_fd = open(archive_name, O_RDONLY);
    if (archive_fd < 0) {
        perror("Failed to open archive for listing");
        return -1;
    }

    tar_header header;
    while (read(archive_fd, &header, sizeof(tar_header)) == sizeof(tar_header)) {
        if (header.name[0] == '\0') {
            break;    // reached the end of the archive (footer detected)
        }

        // Add file name to the list
        if (file_list_add(files, header.name) != 0) {
            close(archive_fd);
            return -1;
        }

        // move file pointer past the file content (skip file data)
        char *size_str = strtok(header.size, " ");
        long file_size = strtol(size_str, NULL, 8);    // convert octal to decimal
        size_t padding = (BLOCK_SIZE - (file_size % BLOCK_SIZE)) % BLOCK_SIZE;
        lseek(archive_fd, file_size + padding, SEEK_CUR);
    }

    close(archive_fd);
    return 0;
}

int extract_files_from_archive(const char *archive_name) {
    int archive_fd = open(archive_name, O_RDONLY);
    if (archive_fd < 0) {
        perror("Failed to open archive for extraction");
        return -1;
    }

    tar_header header;
    while (read(archive_fd, &header, sizeof(tar_header)) == sizeof(tar_header)) {
        if (header.name[0] == '\0') {    // stop at the two consecutive zero blocks (end of archive)
            break;
        }

        int file_fd = open(header.name, O_WRONLY | O_CREAT | O_TRUNC, 0644);
        if (file_fd < 0) {
            perror("Failed to open file for extraction");
            close(archive_fd);
            return -1;
        }

        char *size_str = strtok(header.size, " ");
        long file_size = strtol(size_str, NULL, 8);    // convert octal to decimal

        char buffer[BLOCK_SIZE];
        size_t bytes_left = file_size;
        while (bytes_left > 0) {
            size_t bytes_to_read = (bytes_left < BLOCK_SIZE) ? bytes_left : BLOCK_SIZE;
            ssize_t bytes_read = read(archive_fd, buffer, bytes_to_read);

            if (bytes_read < 0) {
                perror("Error reading file contents from archive");
                close(file_fd);
                close(archive_fd);
                return -1;
            }

            if (write(file_fd, buffer, bytes_read) != bytes_read) {
                perror("Error writing file contents");
                close(file_fd);
                close(archive_fd);
                return -1;
            }
            bytes_left -= bytes_read;
        }

        close(file_fd);

        // move to the next 512-byte boundary
        size_t padding = (file_size % BLOCK_SIZE) ? (BLOCK_SIZE - (file_size % BLOCK_SIZE)) : 0;
        if (padding > 0) {
            lseek(archive_fd, padding, SEEK_CUR);
        }
    }

    close(archive_fd);
    return 0;
}
