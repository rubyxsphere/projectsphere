#ifndef CALCULATOR_H
#define CALCULATOR_H

#include <stdbool.h>

bool evalCalc(char *display);
double applyOperator(char op, double a, double b);
int precedence(char op);
bool isLeftAssociative(char op);

#endif