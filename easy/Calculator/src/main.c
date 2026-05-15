// Currently there is not many features, nor do I plan to add them
// This is the foundation of a simple calculator app

// Gui libraries
#include <raylib.h>
#define RAYGUI_IMPLEMENTATION
#include <raygui.h>
#include <string.h>
#include "calculator.h"
#include <ctype.h>
#include <stdbool.h>

typedef enum {
    ACTION_NUMBER,
    ACTION_OPERATOR,
    ACTION_CLEAR,
    ACTION_EQUALS,
    ACTION_DECIMAL,
    ACTION_SIGN,
    ACTION_BACKSPACE
} ButtonActionType;

typedef bool (*ButtonActionHandler)(char *display, const char *value);

typedef struct {
    const char *label;
    ButtonActionType type;
    const char *value;
} ButtonDef;

void setupGUI(void);
void clearDisplay(char *display);
bool updateDisplay(char *display, char input);

bool handleNumber(char *display, const char *value);
bool handleDecimalAction(char *display, const char *value);
bool handleSignAction(char *display, const char *value);
bool handleBackspaceAction(char *display, const char *value);
bool handleOperatorAction(char *display, const char *value);
bool handleClearAction(char *display, const char *value);
bool handleEqualsAction(char *display, const char *value);

// UI
const int WINDOW_WIDTH = 360;
const int WINDOW_HEIGHT = 400;
const int TEXTBOX_PADDING = 10;
const int BUTTON_SIZE = 60;
const int GRID_COLS = 5;
const int GRID_SPACING = 5;
const int GRID_START_X = 20;
const int GRID_START_Y = 100;
const int WINDOW_COLOR = 0xE0E0E0FF;

static const ButtonDef buttonDefs[] = {
    {"7", ACTION_NUMBER, "7"},
    {"8", ACTION_NUMBER, "8"},
    {"9", ACTION_NUMBER, "9"},
    {"/", ACTION_OPERATOR, "/"},
    {"CLR", ACTION_CLEAR, ""},

    {"4", ACTION_NUMBER, "4"},
    {"5", ACTION_NUMBER, "5"},
    {"6", ACTION_NUMBER, "6"},
    {"*", ACTION_OPERATOR, "*"},
    {"+/-", ACTION_SIGN, ""},

    {"1", ACTION_NUMBER, "1"},
    {"2", ACTION_NUMBER, "2"},
    {"3", ACTION_NUMBER, "3"},
    {"-", ACTION_OPERATOR, "-"},
    {".", ACTION_DECIMAL, "."},

    {"0", ACTION_NUMBER, "0"},
    {"<-", ACTION_BACKSPACE, ""},
    {"=", ACTION_EQUALS, ""},
    {"+", ACTION_OPERATOR, "+"}};

const int TOTAL_BUTTONS = sizeof(buttonDefs) / sizeof(buttonDefs[0]);

static const ButtonActionHandler actionHandlers[] = {
    [ACTION_NUMBER] = handleNumber,
    [ACTION_OPERATOR] = handleOperatorAction,
    [ACTION_CLEAR] = handleClearAction,
    [ACTION_EQUALS] = handleEqualsAction,
    [ACTION_DECIMAL] = handleDecimalAction,
    [ACTION_SIGN] = handleSignAction,
    [ACTION_BACKSPACE] = handleBackspaceAction};

void setupGUI()
{
    SetConfigFlags(FLAG_WINDOW_HIDDEN);
    InitWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Calculator");
    SetWindowPosition((GetMonitorWidth(0) - WINDOW_WIDTH) / 2, (GetMonitorHeight(0) - WINDOW_HEIGHT) / 2);
    ClearWindowState(FLAG_WINDOW_HIDDEN);
    SetTargetFPS(60);

    GuiSetFont(GetFontDefault());
    GuiSetStyle(DEFAULT, BASE_COLOR_NORMAL, WINDOW_COLOR);
    GuiSetStyle(DEFAULT, TEXT_SIZE, 20);
}

void clearDisplay(char *display)
{
    display[0] = '0';
    display[1] = '\0';
}

bool updateDisplay(char *display, char input)
{
    size_t len = strlen(display);

    if (input == '.')
    {
        if (len == 0)
        {
            if (DISPLAY_CAPACITY < 3) return false;
            display[0] = '0';
            display[1] = '.';
            display[2] = '\0';
            return true;
        }

        char last = display[len - 1];
        if (last == '+' || last == '-' || last == '*' || last == '/')
        {
            if (len >= DISPLAY_CAPACITY - 2)
                return false;
            display[len] = '0';
            display[len + 1] = '.';
            display[len + 2] = '\0';
            return true;
        }

        int i = len - 1;
        while (i >= 0 && (isdigit(display[i]) || display[i] == '.'))
            i--;

        if (display[i + 1] == '.')
            return false;

        if (len >= DISPLAY_CAPACITY - 1)
            return false;

        display[len] = '.';
        display[len + 1] = '\0';
        return true;
    }

    if (strcmp(display, "0") == 0 && isdigit(input))
    {
        display[0] = input;
        display[1] = '\0';
        return true;
    }

    if (len == 0)
    {
        if (input == '-' || isdigit(input))
        {
            display[0] = input;
            display[1] = '\0';
            return true;
        }
        return false;
    }

    if (len >= DISPLAY_CAPACITY - 1)
        return false;

    display[len] = input;
    display[len + 1] = '\0';
    return true;
}

bool handleNumber(char *display, const char *value)
{
    return updateDisplay(display, value[0]);
}

bool handleDecimalAction(char *display, const char *value)
{
    return updateDisplay(display, '.');
}

bool handleSignAction(char *display, const char *value)
{
    size_t len = strlen(display);
    if (len == 0)
        return false;

    int i = (int)len - 1;
    while (i >= 0 && (isdigit(display[i]) || display[i] == '.'))
        i--;

    int numberStart = i + 1;
    if ((size_t)numberStart == len)
        return false;

    if (numberStart == 0)
    {
        if (display[0] == '-')
        {
            memmove(display, display + 1, len);
            display[len - 1] = '\0';
            if (display[0] == '\0')
                clearDisplay(display);
            return true;
        }

        if (len >= DISPLAY_CAPACITY - 1)
            return false;

        memmove(display + 1, display, len + 1);
        display[0] = '-';
        return true;
    }

    char prev = display[numberStart - 1];
    if (prev == '-' && (numberStart == 1 || display[numberStart - 2] == '+' || display[numberStart - 2] == '-' || display[numberStart - 2] == '*' || display[numberStart - 2] == '/'))
    {
        memmove(display + numberStart - 1, display + numberStart, len - numberStart + 1);
        return true;
    }

    if (prev == '+' || prev == '*' || prev == '/')
    {
        if (len >= DISPLAY_CAPACITY - 1)
            return false;

        memmove(display + numberStart + 1, display + numberStart, len - numberStart + 1);
        display[numberStart] = '-';
        return true;
    }

    return false;
}

bool handleBackspaceAction(char *display, const char *value)
{
    size_t len = strlen(display);
    if (len <= 1)
    {
        clearDisplay(display);
        return true;
    }

    display[len - 1] = '\0';
    if (display[0] == '\0' || strcmp(display, "-") == 0)
        clearDisplay(display);
    return true;
}

bool handleOperatorAction(char *display, const char *value)
{
    size_t len = strlen(display);
    if (len == 0 && strcmp(value, "-") != 0)
    {
        return false;
    }

    if (len > 0)
    {
        char last = display[len - 1];
        if (last == '+' || last == '-' || last == '*' || last == '/')
        {
            display[len - 1] = value[0];
            return true;
        }
    }

    return updateDisplay(display, value[0]);
}

bool handleClearAction(char *display, const char *value)
{
    clearDisplay(display);
    return true;
}

bool handleEqualsAction(char *display, const char *value)
{
    return evalCalc(display);
}

int main()
{
    setupGUI();

    char display[DISPLAY_CAPACITY] = "0";
    char notificationText[DISPLAY_CAPACITY] = "";
    int notificationTimer = 0;

    while (!WindowShouldClose())
    {
        BeginDrawing();

        ClearBackground(RAYWHITE);

        // First draw a white rectangle for the background
        // Then draw the text box on the top
        // Clicking on the textbox puts the number on the clipboard
        DrawRectangle(TEXTBOX_PADDING, 20, WINDOW_WIDTH - 2 * TEXTBOX_PADDING, 40, WHITE);
        Rectangle textBoxRect = {TEXTBOX_PADDING, 20, WINDOW_WIDTH - 2 * TEXTBOX_PADDING, 40};

        if (IsMouseButtonPressed(MOUSE_LEFT_BUTTON) &&
            CheckCollisionPointRec(GetMousePosition(), textBoxRect))
        {
            SetClipboardText(display);
            snprintf(notificationText, DISPLAY_CAPACITY, "Copied %.38s to the clipboard.", display);
            notificationTimer = 90;
        }

        int contentWidth = (int)textBoxRect.width - 12;
        const char *displayText = display;
        int displayedWidth = MeasureText(displayText, 20);
        if (displayedWidth > contentWidth)
        {
            int displayLen = strlen(display);
            for (int idx = 1; idx < displayLen; idx++)
            {
                int width = MeasureText(display + idx, 20);
                if (width <= contentWidth)
                {
                    displayText = display + idx;
                    displayedWidth = width;
                    break;
                }
            }
        }

        int drawX = TEXTBOX_PADDING + 6 + (contentWidth - displayedWidth);
        DrawText(displayText, drawX, 28, 20, BLACK);

        if (notificationTimer > 0)
        {
            DrawText(notificationText, TEXTBOX_PADDING, 70, 10, DARKGRAY);
            notificationTimer--;
        }

        for (int i = 0; i < TOTAL_BUTTONS; i++)
        {
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;

            int x = GRID_START_X + col * (BUTTON_SIZE + GRID_SPACING);
            int y = GRID_START_Y + row * (BUTTON_SIZE + GRID_SPACING);

            if (GuiButton((Rectangle){x, y, BUTTON_SIZE, BUTTON_SIZE}, buttonDefs[i].label))
            {
                ButtonActionHandler handler = actionHandlers[buttonDefs[i].type];
                if (handler)
                {
                    handler(display, buttonDefs[i].value);
                }
            }
        }

        EndDrawing();
    }

    CloseWindow();
    return 0;
}