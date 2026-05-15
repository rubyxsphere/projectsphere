#include "calculator.h"
#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdlib.h>
#include <math.h>

#define MAX_STACK 32
#define ERROR_EXPECTED_NUMBER "EXPECTED NUMBER"
#define ERROR_INVALID_EXPRESSION "INVALID EXPRESSION"
#define ERROR_DIVISION_BY_ZERO "DIVISION BY ZERO"
#define ERROR_INVALID_OPERATOR "INVALID OPERATOR"
#define ERROR_UNBALANCED_EXPRESSION "UNBALANCED EXPRESSION"

typedef struct {
    char operators[MAX_STACK];
    double numbers[MAX_STACK];
    int op_top;
    int num_top;
} CalcStack;

void initStack(CalcStack* stack) {
    stack->op_top = -1;
    stack->num_top = -1;
}

bool pushOperator(CalcStack* stack, char op) {
    if (stack->op_top >= MAX_STACK - 1)
        return false;
    stack->operators[++stack->op_top] = op;
    return true;
}

char popOperator(CalcStack* stack) {
    if (stack->op_top < 0)
        return '\0';
    return stack->operators[stack->op_top--];
}

char peekOperator(CalcStack* stack) {
    if (stack->op_top < 0)
        return '\0';
    return stack->operators[stack->op_top];
}

bool pushNumber(CalcStack* stack, double num) {
    if (stack->num_top >= MAX_STACK - 1)
        return false;
    stack->numbers[++stack->num_top] = num;
    return true;
}

double popNumber(CalcStack* stack) {
    if (stack->num_top < 0)
        return 0;
    return stack->numbers[stack->num_top--];
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
    CalcStack stack;
    initStack(&stack);

    const char *expr = display;
    const char *token_start = expr;
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
                    if (!pushNumber(&stack, 0) || !pushOperator(&stack, '-')) {
                        snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_EXPRESSION);
                        return false;
                    }
                    token_start++;
                    continue;
                }
                else
                {
                    snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_EXPECTED_NUMBER);
                    return false;
                }
            }

            if (!pushNumber(&stack, num)) {
                snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_EXPRESSION);
                return false;
            }
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
                        snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_EXPRESSION);
                        return false;
                    }

                    double b = popNumber(&stack);
                    double a = popNumber(&stack);
                    double result = applyOperator(stack_op, a, b);

                    if (isnan(result)) {
                        snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_DIVISION_BY_ZERO);
                        return false;
                    }

                    if (!pushNumber(&stack, result)) {
                        snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_EXPRESSION);
                        return false;
                    }
                }

                if (!pushOperator(&stack, op)) {
                    snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_EXPRESSION);
                    return false;
                }
                token_start++;
                expecting_number = true;
            } else {
                snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_OPERATOR);
                return false;
            }
        }
    }

    while (stack.op_top >= 0) {
        if (stack.num_top < 1) {
            snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_INVALID_EXPRESSION);
            return false;
        }

        char op = popOperator(&stack);
        double b = popNumber(&stack);
        double a = popNumber(&stack);
        double result = applyOperator(op, a, b);

        if (isnan(result)) {
            snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_DIVISION_BY_ZERO);
            return false;
        }

        pushNumber(&stack, result);
    }

    if (stack.num_top != 0) {
        snprintf(display, DISPLAY_CAPACITY, "%s", ERROR_UNBALANCED_EXPRESSION);
        return false;
    }

    double final_result = popNumber(&stack);

    if (fabs(final_result - floor(final_result)) < 1e-10) {
        snprintf(display, DISPLAY_CAPACITY, "%.0f", final_result);
    }
    else if (fabs(final_result) >= 1e10 || (fabs(final_result) > 0 && fabs(final_result) < 1e-9)) {
        snprintf(display, DISPLAY_CAPACITY, "%.6e", final_result);
    }
    else {
        snprintf(display, DISPLAY_CAPACITY, "%.10g", final_result);
    }

    return true;
}