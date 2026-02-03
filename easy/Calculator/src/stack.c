#include "stack.h"

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