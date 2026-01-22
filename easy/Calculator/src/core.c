#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdlib.h>
#include <math.h>
#include "core.h"

#define MAX_STACK 100

typedef struct {
    char operators[MAX_STACK];
    double numbers[MAX_STACK];
    int op_top;
    int num_top;
} CalcStack;

static CalcStack stack = {0};

void initStack() {
    stack.op_top = -1;
    stack.num_top = -1;
}

bool pushOperator(char op) {
    if (stack.op_top >= MAX_STACK - 1)
        return false;
    stack.operators[++stack.op_top] = op;
    return true;
}

char popOperator() {
    if (stack.op_top < 0)
        return '\0';
    return stack.operators[stack.op_top--];
}

char peekOperator() {
    if (stack.op_top < 0)
        return '\0';
    return stack.operators[stack.op_top];
}

bool pushNumber(double num) {
    if (stack.num_top >= MAX_STACK - 1)
        return false;
    stack.numbers[++stack.num_top] = num;
    return true;
}

double popNumber() {
    if (stack.num_top < 0)
        return 0;
    return stack.numbers[stack.num_top--];
}

// Operator precedence + associativity
int precedence(char op) {
    switch (op) {
    case '*':
    case '/':
        return 2;
    case '+':
    case '-':
        return 1;
    default:
        return 0;
    }
}

bool isLeftAssociative(char op) {
    return (op == '+' || op == '-' || op == '*' || op == '/');
}

double applyOperator(char op, double a, double b) {
    switch (op) {
    case '+':
        return a + b;
    case '-':
        return a - b;
    case '*':
        return a * b;
    case '/':
        if (b == 0) {
            return NAN;
        }
        return a / b;
    default:
        return 0;
    }
}

// Shunting Yard algorithm
bool evalCalc(char *display) {
    initStack();

    char *expr = display;
    char *token_start = expr;
    bool expecting_number = true;

    while (*token_start) {
        while (*token_start == ' ')
            token_start++;

        if (!*token_start)
            break;

        if (expecting_number) {
            char *end;
            double num = strtod(token_start, &end);

            if (end == token_start) {
                // Not a number - check for unary minus
                if (*token_start == '-' && expecting_number) {
                    pushNumber(0);
                    pushOperator('-');
                    token_start++;
                    continue;
                }
                else
                {
                    strcpy(display, "Error");
                    return false;
                }
            }

            pushNumber(num);
            token_start = end;
            expecting_number = false;
        }
        else {
            // Parse operator
            char op = *token_start;

            if (op == '+' || op == '-' || op == '*' || op == '/') {
                // Process operators with higher OR equal precdence
                while (stack.op_top >= 0 && ((isLeftAssociative(op) && precedence(op) <= precedence(peekOperator())) ||
                        (!isLeftAssociative(op) && precedence(op) < precedence(peekOperator())))) {

                    char stack_op = popOperator();
                    if (stack.num_top < 1) {
                        strcpy(display, "Error");
                        return false;
                    }

                    double b = popNumber();
                    double a = popNumber();
                    double result = applyOperator(stack_op, a, b);

                    if (isnan(result)) {
                        strcpy(display, "Div by 0");
                        return false;
                    }

                    pushNumber(result);
                }

                pushOperator(op);
                token_start++;
                expecting_number = true;
            }
            else {
                // Invalid character
                strcpy(display, "Error");
                return false;
            }
        }
    }

    // Apply remaining operators
    while (stack.op_top >= 0) {
        if (stack.num_top < 1) {
            strcpy(display, "Error");
            return false;
        }

        char op = popOperator();
        double b = popNumber();
        double a = popNumber();
        double result = applyOperator(op, a, b);

        if (isnan(result)) {
            strcpy(display, "Div by 0");
            return false;
        }

        pushNumber(result);
    }

    // Final result should be on top of number stack
    if (stack.num_top != 0) {
        strcpy(display, "Error");
        return false;
    }

    double final_result = popNumber();

    if (fabs(final_result - floor(final_result)) < 1e-10) {
        snprintf(display, 256, "%.0f", final_result);
    }
    else {
        snprintf(display, 256, "%.10g", final_result);
    }

    return true;
}

bool handleNumber(char *display, const char *value) {
    return updateDisplay(display, value[0]);
}

// Don't allow operator at start unless its minus
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

    // Buffer overflow
    if (len >= 255)
        return false;

    display[len] = input;
    display[len + 1] = '\0';
    return true;
}

void clearDisplay(char *display) {
    strcpy(display, "0");
}