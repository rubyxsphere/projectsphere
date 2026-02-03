#include "calculator.h"
#include "stack.h"
#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdlib.h>
#include <math.h>

static CalcStack stack = {0};

void initStack() {
    stack.op_top = -1;
    stack.num_top = -1;
}

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
                    pushNumber(&stack, 0);
                    pushOperator(&stack, '-');
                    token_start++;
                    continue;
                }
                else
                {
                    strcpy(display, "Error");
                    return false;
                }
            }

            pushNumber(&stack, num);
            token_start = end;
            expecting_number = false;
        }
        else {
            char op = *token_start;

            if (op == '+' || op == '-' || op == '*' || op == '/') {
                while (stack.op_top >= 0 && ((isLeftAssociative(op) && precedence(op) <= precedence(peekOperator(&stack))) ||
                        (!isLeftAssociative(op) && precedence(op) < precedence(peekOperator(&stack))))) {

                    char stack_op = popOperator(&stack);
                    if (stack.num_top < 1) {
                        strcpy(display, "Error");
                        return false;
                    }

                    double b = popNumber(&stack);
                    double a = popNumber(&stack);
                    double result = applyOperator(stack_op, a, b);

                    if (isnan(result)) {
                        strcpy(display, "Div by 0");
                        return false;
                    }

                    pushNumber(&stack, result);
                }

                pushOperator(&stack, op);
                token_start++;
                expecting_number = true;
            }
            else {
                strcpy(display, "Error");
                return false;
            }
        }
    }

    while (stack.op_top >= 0) {
        if (stack.num_top < 1) {
            strcpy(display, "Error");
            return false;
        }

        char op = popOperator(&stack);
        double b = popNumber(&stack);
        double a = popNumber(&stack);
        double result = applyOperator(op, a, b);

        if (isnan(result)) {
            strcpy(display, "Div by 0");
            return false;
        }

        pushNumber(&stack, result);
    }

    if (stack.num_top != 0) {
        strcpy(display, "Error");
        return false;
    }

    double final_result = popNumber(&stack);

    if (fabs(final_result - floor(final_result)) < 1e-10) {
        snprintf(display, 256, "%.0f", final_result);
    }
    else {
        snprintf(display, 256, "%.10g", final_result);
    }

    return true;
}