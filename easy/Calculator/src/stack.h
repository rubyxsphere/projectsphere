#ifndef STACK_H
#define STACK_H

#include <stdbool.h>

#define MAX_STACK 100

typedef struct {
    char operators[MAX_STACK];
    double numbers[MAX_STACK];
    int op_top;
    int num_top;
} CalcStack;

void initStack(CalcStack* stack);
bool pushOperator(CalcStack* stack, char op);
char popOperator(CalcStack* stack);
char peekOperator(CalcStack* stack);
bool pushNumber(CalcStack* stack, double num);
double popNumber(CalcStack* stack);

#endif