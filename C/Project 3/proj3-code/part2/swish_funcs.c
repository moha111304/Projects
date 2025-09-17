#include "swish_funcs.h"

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "string_vector.h"

#define MAX_ARGS 10
/*
 * Helper function to compare two strings to see if they are the same
 * withough including a new library.
 */
int my_strcmp(const char *a, const char *b) {
    while (*a && *b && *a == *b) {
        a++;
        b++;
    }
    return (unsigned char) *a - (unsigned char) *b;
}

/*
 * Helper function to run a single command within a pipeline. You should make
 * make use of the provided 'run_command' function here.
 * tokens: String vector containing the tokens representing the command to be
 * executed, possible redirection, and the command's arguments.
 * pipes: An array of pipe file descriptors.
 * n_pipes: Length of the 'pipes' array
 * in_idx: Index of the file descriptor in the array from which the program
 *         should read its input, or -1 if input should not be read from a pipe.
 * out_idx: Index of the file descriptor in the array to which the program
 *          should write its output, or -1 if output should not be written to
 *          a pipe.
 * Returns 0 on success or -1 on error.
 */
int run_piped_command(strvec_t *tokens, int *pipes, int n_pipes, int in_idx, int out_idx) {
    // TODO Complete this function's implementation
    pid_t pid = fork();
    if (pid == -1) {
        perror("fork");
        return -1;
    } else if (pid == 0) {
        // Child process
        if (in_idx != -1) {
            dup2(pipes[in_idx], STDIN_FILENO);
        }
        if (out_idx != -1) {
            dup2(pipes[out_idx], STDOUT_FILENO);
        }

        // Close all pipe fds in the child
        for (int i = 0; i < n_pipes; i++) {
            close(pipes[i]);
        }

        run_command(tokens);
        exit(1);
    }

    return 0;
}

int run_pipelined_commands(strvec_t *tokens) {
    // TODO Complete this function's implementation

    // Split tokens into separate command groups
    int capacity = 4;
    int cmd_count = 0;
    strvec_t *commands = malloc(capacity * sizeof(strvec_t));
    if (!commands) {
        perror("malloc");
        return -1;
    }

    strvec_init(&commands[cmd_count]);

    for (int i = 0; i < tokens->length; i++) {
        const char *tok = strvec_get(tokens, i);
        if (my_strcmp(tok, "|") == 0) {
            cmd_count++;
            if (cmd_count >= capacity) {
                capacity *= 2;
                strvec_t *new_commands = realloc(commands, capacity * sizeof(strvec_t));
                if (!new_commands) {
                    perror("realloc");
                    // Clean up whats already been allocated
                    for (int j = 0; j < cmd_count; j++) {
                        strvec_clear(&commands[j]);
                    }
                    free(commands);
                    return -1;
                }
                commands = new_commands;
            }
            strvec_init(&commands[cmd_count]);
        } else {
            strvec_add(&commands[cmd_count], tok);
        }
    }
    cmd_count++;    // total number of commands

    // Set up pipes
    int num_pipes = 2 * (cmd_count - 1);
    int pipes[num_pipes];
    for (int i = 0; i < cmd_count - 1; i++) {
        if (pipe(&pipes[2 * i]) == -1) {
            perror("pipe");
            // Clean up
            for (int j = 0; j < cmd_count; j++) {
                strvec_clear(&commands[j]);
            }
            free(commands);
            return -1;
        }
    }

    // Launch each command
    for (int i = 0; i < cmd_count; i++) {
        int in_idx = (i == 0) ? -1 : 2 * (i - 1);               // read end of previous pipe
        int out_idx = (i == cmd_count - 1) ? -1 : 2 * i + 1;    // write end of current pipe
        if (run_piped_command(&commands[i], pipes, num_pipes, in_idx, out_idx) == -1) {
            // Clean up
            for (int j = 0; j < cmd_count; j++) {
                strvec_clear(&commands[j]);
            }
            free(commands);
            return -1;
        }
    }

    // Close all pipe fds in parent
    for (int i = 0; i < num_pipes; i++) {
        close(pipes[i]);
    }

    //  Wait for all children
    for (int i = 0; i < cmd_count; i++) {
        int status;
        wait(&status);
    }

    // Clean up strvecs
    for (int i = 0; i < cmd_count; i++) {
        strvec_clear(&commands[i]);
    }
    free(commands);

    return 0;
}
