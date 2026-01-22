#ifndef CORE_H
#define CORE_H

#include <stdbool.h>

typedef enum {
    ACTION_NUMBER,
    ACTION_OPERATOR,
    ACTION_CLEAR,
    ACTION_EQUALS,
    ACTION_FUNCTION
} ButtonActionType;

typedef bool (*ButtonActionHandler)(char* display, const char* value);

typedef struct {
    const char* label;
    ButtonActionType type;
    const char* value;
} ButtonDef;

bool updateDisplay(char* display, char input);
void clearDisplay(char* display);
bool evalCalc(char* display);
bool handleOperator(char* display, const char* op);

bool handleNumber(char* display, const char* value);
bool handleOperatorAction(char* display, const char* value);
bool handleClearAction(char* display, const char* value);
bool handleEqualsAction(char* display, const char* value);

#endif