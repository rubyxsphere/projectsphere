#include "display.h"
#include <string.h>
#include <ctype.h>
#include <stdbool.h>

bool updateDisplay(char *display, char input) {
    size_t len = strlen(display);

    if (strcmp(display, "0") == 0 && isdigit(input)) {
        display[0] = input;
        display[1] = '\0';
        return true;
    }

    if (len == 0) {
        if (input == '-' || isdigit(input)) {
            display[0] = input;
            display[1] = '\0';
            return true;
        }
        return false;
    }

    if (len >= 255)
        return false;

    display[len] = input;
    display[len + 1] = '\0';
    return true;
}

void clearDisplay(char *display) {
    strcpy(display, "0");
}