#include <stdio.h>
#include <string.h>

#include "file_list.h"
#include "minitar.h"

int main(int argc, char **argv) {
    if (argc < 4) {
        fprintf(stderr, "Usage: %s -c|a|t|u|x -f ARCHIVE [FILE...]\n", argv[0]);
        return 1;
    }

    file_list_t files;
    file_list_init(&files);

    char *archive_name = NULL;
    int i = 1;
    char operation = '/';

    // get operation flag (-c, -a, -t, -u, -x)
    if (argv[i][0] == '-' && strlen(argv[i]) == 2) {
        operation = argv[i][1];
        i++;
    } else {
        fprintf(stderr, "Invalid operation. Use -c, -a, -t, -u, or -x.\n");
        file_list_clear(&files);
        return 1;
    }

    // get -f argument
    if (i < argc && strcmp(argv[i], "-f") == 0 && i + 1 < argc) {
        archive_name = argv[i + 1];
        i += 2;
    } else {
        fprintf(stderr, "Missing -f ARCHIVE argument.\n");
        file_list_clear(&files);
        return 1;
    }

    // collect file names for -c, -a, and -u operations
    if (operation == 'c' || operation == 'a' || operation == 'u') {
        for (; i < argc; i++) {
            if (file_list_add(&files, argv[i]) != 0) {
                fprintf(stderr, "Failed to add file: %s\n", argv[i]);
                file_list_clear(&files);
                return 1;
            }
        }
    }

    int result = 0;
    switch (operation) {
        case 'c':    // create a new archive
            if (files.head == NULL) {
                fprintf(stderr, "No files specified for creation.\n");
                result = 1;
                break;
            }
            remove(archive_name);    // maje sure archive is overwritten if it exists
            result = create_archive(archive_name, &files);
            if (result != 0)
                fprintf(stderr, "Failed to create archive.\n");
            break;

        case 'a':    // append files to archive
            if (files.head == NULL) {
                fprintf(stderr, "No files specified for appending.\n");
                result = 1;
                break;
            }
            result = append_files_to_archive(archive_name, &files);
            if (result != 0)
                fprintf(stderr, "Failed to append files to archive.\n");
            break;

        case 't':    // list files in archive
            result = get_archive_file_list(archive_name, &files);
            if (result == 0) {
                node_t *current = files.head;
                while (current) {
                    printf("%s\n", current->name);
                    current = current->next;
                }
            } else {
                fprintf(stderr, "Failed to list files in archive.\n");
            }
            break;

        case 'u':    // update files in archive
            file_list_t archive_files;
            file_list_init(&archive_files);

            // get the list of files currently in the archive
            if (get_archive_file_list(archive_name, &archive_files) != 0) {
                fprintf(stderr, "Failed to read archive file list.\n");
                file_list_clear(&archive_files);
                file_list_clear(&files);
                return 1;
            }

            // check if all update files are in the archive
            if (!file_list_is_subset(&files, &archive_files)) {
                fprintf(stderr,
                        "Error: One or more of the specified files is not already present in "
                        "archive\n");
                file_list_clear(&archive_files);
                file_list_clear(&files);
                return 1;
            }

            file_list_clear(&archive_files);

            result = append_files_to_archive(archive_name, &files);
            break;

        case 'x':    // extract from archive
            result = extract_files_from_archive(archive_name);
            if (result != 0)
                fprintf(stderr, "Failed to extract files from archive\n");
            break;
    }

    file_list_clear(&files);
    return result;
}
