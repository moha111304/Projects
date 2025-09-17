#define _GNU_SOURCE

#include "swish_funcs.h"

#include <assert.h>
#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "job_list.h"
#include "string_vector.h"

#define MAX_ARGS 10

int tokenize(char *s, strvec_t *tokens) {
    // Task 0: Tokenize string s
    // Assume each token is separated by a single space (" ")
    // Use the strtok() function to accomplish this
    // Add each token to the 'tokens' parameter (a string vector)
    // Return 0 on success, -1 on error
    // Tokenize input string based on spaces
    char *token = strtok(s, " ");
    while (token != NULL) {
        strvec_add(tokens, token);    // Add each token to the vector
        token = strtok(NULL, " ");
    }
    return 0;
}

int run_command(strvec_t *tokens) {
    // Task 2: Execute the specified program (token 0) with the
    // specified command-line arguments
    // THIS FUNCTION SHOULD BE CALLED FROM A CHILD OF THE MAIN SHELL PROCESS
    // Hint: Build a string array from the 'tokens' vector and pass this into execvp()
    // Another Hint: You have a guarantee of the longest possible needed array, so you
    // won't have to use malloc.

    // Task 3: Extend this function to perform output redirection before exec()'ing
    // Check for '<' (redirect input), '>' (redirect output), '>>' (redirect and append output)
    // entries inside of 'tokens' (the strvec_find() function will do this for you)
    // Open the necessary file for reading (<), writing (>), or appending (>>)
    // Use dup2() to redirect stdin (<), stdout (> or >>)
    // DO NOT pass redirection operators and file names to exec()'d program
    // E.g., "ls -l > out.txt" should be exec()'d with strings "ls", "-l", NULL

    // Task 4: You need to do two items of setup before exec()'ing
    // 1. Restore the signal handlers for SIGTTOU and SIGTTIN to their defaults.
    // The code in main() within swish.c sets these handlers to the SIG_IGN value.
    // Adapt this code to use sigaction() to set the handlers to the SIG_DFL value.
    // 2. Change the process group of this process (a child of the main shell).
    // Call getpid() to get its process ID then call setpgid() and use this process
    // ID as the value for the new process group ID

    if (tokens->length == 0) {
        return -1;
    }

    // Restore default signal handlers
    struct sigaction sa;
    sa.sa_handler = SIG_DFL;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGTTOU, &sa, NULL);
    sigaction(SIGTTIN, &sa, NULL);

    // Create a new process group for this command
    setpgid(0, 0);

    int in_fd = -1, out_fd = -1;

    for (int i = 0; i < tokens->length; i++) {
        char *current = strvec_get(tokens, i);

        // Check for redirection operators
        if (strcmp(current, "<") == 0 || strcmp(current, ">") == 0 || strcmp(current, ">>") == 0) {
            if (i + 1 >= tokens->length) {    // No filename provided
                fprintf(stderr, "Error: No file specified for redirection\n");
                return -1;
            }

            char *filename = strvec_get(tokens, i + 1);
            if (strcmp(current, "<") == 0) {
                in_fd = open(filename, O_RDONLY);
            } else if (strcmp(current, ">") == 0) {
                out_fd = open(filename, O_WRONLY | O_CREAT | O_TRUNC, 0644);
            } else {    // ">>"
                out_fd = open(filename, O_WRONLY | O_CREAT | O_APPEND, 0644);
            }

            if ((in_fd == -1 && strcmp(current, "<") == 0) ||
                (out_fd == -1 && (strcmp(current, ">") == 0 || strcmp(current, ">>") == 0))) {
                perror("Failed to open input file");
                return -1;
            }

            // Shift tokens to remove redirection symbols and filename
            for (int j = i; j < tokens->length - 2; j++) {
                tokens->data[j] = tokens->data[j + 2];
            }
            tokens->length -= 2;
            i--;    // Adjust index after removal
        }
    }

    // Apply I/O redirection if necessary
    if (in_fd != -1) {
        if (dup2(in_fd, STDIN_FILENO) == -1) {
            perror("dup2 input failed");
            return -1;
        }
        close(in_fd);
    }
    if (out_fd != -1) {
        if (dup2(out_fd, STDOUT_FILENO) == -1) {
            perror("dup2 output failed");
            return -1;
        }
        close(out_fd);
    }

    // Convert strvec_t to char* array for execvp()
    char *args[MAX_ARGS];
    int i;
    for (i = 0; i < tokens->length && i < MAX_ARGS - 1; i++) {
        args[i] = strvec_get(tokens, i);
    }
    args[i] = NULL;    // Ensure null termination

    // Execute the command
    execvp(args[0], args);
    perror("exec");    // If execvp fails
    exit(1);
}

int resume_job(strvec_t *tokens, job_list_t *jobs, int is_foreground) {
    // Task 5: Implement the ability to resume stopped jobs in the foreground
    // 1. Look up the relevant job information (in a job_t) from the jobs list
    //    using the index supplied by the user (in tokens index 1)
    //    Feel free to use sscanf() or atoi() to convert this string to an int
    // 2. Call tcsetpgrp(STDIN_FILENO, <job_pid>) where job_pid is the job's process ID
    // 3. Send the process the SIGCONT signal with the kill() system call
    // 4. Use the same waitpid() logic as in main -- don't forget WUNTRACED
    // 5. If the job has terminated (not stopped), remove it from the 'jobs' list
    // 6. Call tcsetpgrp(STDIN_FILENO, <shell_pid>). shell_pid is the *current*
    //    process's pid, since we call this function from the main shell process

    // Task 6: Implement the ability to resume stopped jobs in the background.
    // This really just means omitting some of the steps used to resume a job in the foreground:
    // 1. DO NOT call tcsetpgrp() to manipulate foreground/background terminal process group
    // 2. DO NOT call waitpid() to wait on the job
    // 3. Make sure to modify the 'status' field of the relevant job list entry to BACKGROUND
    //    (as it was STOPPED before this)

    if (tokens->length < 2) {
        printf("Usage: %s <job_index>\n", strvec_get(tokens, 0));
        return -1;
    }

    int job_index = atoi(strvec_get(tokens, 1));
    job_t *job = job_list_get(jobs, job_index);
    if (job == NULL) {
        printf("Job index out of bounds\n");
        return -1;
    }

    if (is_foreground) {
        tcsetpgrp(STDIN_FILENO, job->pid);
        kill(job->pid, SIGCONT);

        int status;
        waitpid(job->pid, &status, WUNTRACED);

        if (WIFSTOPPED(status)) {
            job->status = STOPPED;
        } else {
            job_list_remove(jobs, job_index);
        }

        tcsetpgrp(STDIN_FILENO, getpid());    // Restore shell control
    } else {
        // Send SIGCONT to resume the stopped job in the background
        kill(job->pid, SIGCONT);
        // Update the job's status to BACKGROUND
        job->status = BACKGROUND;
    }

    return 0;
}

int await_background_job(strvec_t *tokens, job_list_t *jobs) {
    // Task 6: Wait for a specific job to stop or terminate
    // 1. Look up the relevant job information (in a job_t) from the jobs list
    //    using the index supplied by the user (in tokens index 1)
    // 2. Make sure the job's status is BACKGROUND (no sense waiting for a stopped job)
    // 3. Use waitpid() to wait for the job to terminate, as you have in resume_job() and main().
    // 4. If the process terminates (is not stopped by a signal) remove it from the jobs list

    // Ensure the correct number of arguments is provided
    if (tokens->length < 2) {
        printf("Usage: wait-for <job_index>\n");
        return -1;
    }

    // Extract job index from tokens
    int job_index = atoi(strvec_get(tokens, 1));
    job_t *job = job_list_get(jobs, job_index);

    // Validate that the job exists and is a background job
    if (job == NULL || job->status != BACKGROUND) {
        printf("Job index is for stopped process not background process\n");
        return -1;
    }

    // Wait for the job to stop or terminate
    int status;
    waitpid(job->pid, &status, WUNTRACED);
    if (!WIFSTOPPED(status)) {    // If the job has terminated, remove it from the job list
        job_list_remove(jobs, job_index);
    }
    return 0;
}

int await_all_background_jobs(job_list_t *jobs) {
    // Task 6: Wait for all background jobs to stop or terminate
    // 1. Iterate through the jobs list, ignoring any stopped jobs
    // 2. For a background job, call waitpid() with WUNTRACED.
    // 3. If the job has stopped (check with WIFSTOPPED), change its
    //    status to STOPPED. If the job has terminated, do nothing until the
    //    next step (don't attempt to remove it while iterating through the list).
    // 4. Remove all background jobs (which have all just terminated) from jobs list.
    //    Use the job_list_remove_by_status() function.

    job_t *current = jobs->head;

    // First, wait for background jobs to stop or terminate
    while (current != NULL) {
        if (current->status == BACKGROUND) {
            int status;
            waitpid(current->pid, &status, WUNTRACED);

            if (WIFSTOPPED(status)) {
                current->status = STOPPED;    // Mark stopped jobs
            }
        }
        current = current->next;
    }

    // Second, remove all terminated background jobs in one step
    job_list_remove_by_status(jobs, BACKGROUND);

    return 0;
}
