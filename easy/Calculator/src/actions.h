#ifndef ACTIONS_H
#define ACTIONS_H

#include <stdbool.h>

bool handleNumber(char* display, const char* value);
bool handleOperatorAction(char* display, const char* value);
bool handleClearAction(char* display, const char* value);
bool handleEqualsAction(char* display, const char* value);
bool handleOperator(char* display, const char* op);

#endif