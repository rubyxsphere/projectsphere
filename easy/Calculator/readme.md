### Program info:

GCC: Compiler for building the executable.<br>
Make: Build automation tool for compiling and linking.<br>
Raylib & Raygui: Cross-platform library for graphics, window management and input handling.

Shunting Yard Algorithm: expression parsing (handles operator precedence: * / before + -) and standard stack operations for expression eval. <https://en.wikipedia.org/wiki/Shunting_yard_algorithm><br>
Flow: click buttons -> button action handlers -> modify display string -> algorithm parses string -> calculates result -> updates display.<br>
Structures: `CalcStack` holds two stacks (numbers and operators) for algorithm, and `ButtonDef` struct stores button label/type/value for button handling.

> Clicking the display copies the current number to clipboard.