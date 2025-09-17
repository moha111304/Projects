#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define ALPHABET_LEN 26

/*
 * Counts the number of occurrences of each letter (case insensitive) in a text
 * file and stores the results in an array.
 * file_name: The name of the text file in which to count letter occurrences
 * counts: An array of integers storing the number of occurrences of each letter.
 *     counts[0] is the number of 'a' or 'A' characters, counts [1] is the number
 *     of 'b' or 'B' characters, and so on.
 * Returns 0 on success or -1 on error.
 */
int count_letters(const char *file_name, int *counts) {
    FILE *fp = fopen(file_name, "r");
    if (!fp) {
        perror("fopen");
        return -1;
    }

    int character;
    while ((character = fgetc(fp)) != EOF) {
        if (isalpha(character)) {
            character = tolower(character);
            counts[character - 'a']++;
        }
    }

    fclose(fp);
    return 0;
}

/*
 * Processes a particular file(counting occurrences of each letter)
 *     and writes the results to a file descriptor.
 * This function should be called in child processes.
 * file_name: The name of the file to analyze.
 * out_fd: The file descriptor to which results are written
 * Returns 0 on success or -1 on error
 */
int process_file(const char *file_name, int out_fd) {
    int counts[ALPHABET_LEN] = {0};

    if (count_letters(file_name, counts) == -1) {
        return -1;
    }

    if (write(out_fd, counts, sizeof(counts)) != sizeof(counts)) {
        perror("write");
        return -1;
    }

    return 0;
}

int main(int argc, char **argv) {
    if (argc == 1) {
        return 0;
    }

    // TODO Create a pipe for child processes to write their results
    int pipefd[2];
    if (pipe(pipefd) == -1) {
        perror("pipe");
        return 1;
    }
    // TODO Fork a child to analyze each specified file (names are argv[1], argv[2], ...)
    for (int i = 1; i < argc; i++) {
        pid_t pid = fork();
        if (pid == -1) {
            perror("fork");
            return 1;
        } else if (pid == 0) {
            // Child process
            close(pipefd[0]);    // Close read end
            if (process_file(argv[i], pipefd[1]) == -1) {
                exit(1);
            }
            close(pipefd[1]);
            exit(0);
        }
    }

    // Parent process
    close(pipefd[1]);    // Close write end

    // TODO Aggregate all the results together by reading from the pipe in the parent
    int total_counts[ALPHABET_LEN] = {0};
    for (int i = 1; i < argc; i++) {
        int buf[ALPHABET_LEN];
        ssize_t bytes_read = read(pipefd[0], buf, sizeof(buf));
        if (bytes_read == -1) {
            perror("read");
            continue;
        } else if (bytes_read != sizeof(buf)) {    // Optionally I could add error print statement
            continue;
        }

        for (int j = 0; j < ALPHABET_LEN; j++) {
            total_counts[j] += buf[j];
        }
    }

    close(pipefd[0]);

    // Wait for all children
    for (int i = 1; i < argc; i++) {
        int status;
        wait(&status);
    }

    // TODO Change this code to print out the total count of each letter (case insensitive)
    for (int i = 0; i < ALPHABET_LEN; i++) {
        printf("%c Count: %d\n", 'a' + i, total_counts[i]);
    }
    return 0;
}
