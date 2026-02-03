#include "actions.h"
#include "display.h"
#include "calculator.h"
#include <string.h>
#include <stdbool.h>

bool handleNumber(char *display, const char *value) {
    return updateDisplay(display, value[0]);
}

bool handleOperatorAction(char *display, const char *value) {
    size_t len = strlen(display);
    if (len == 0 && strcmp(value, "-") != 0) {
        return false;
    }

    if (len > 0) {
        char last = display[len - 1];
        if (last == '+' || last == '-' || last == '*' || last == '/') {
            display[len - 1] = value[0];
            return true;
        }
    }

    return updateDisplay(display, value[0]);
}

bool handleClearAction(char *display, const char *value) {
    clearDisplay(display);
    return true;
}

bool handleEqualsAction(char *display, const char *value) {
    return evalCalc(display);
}

bool handleOperator(char *display, const char *op) {
    return handleOperatorAction(display, op);
}