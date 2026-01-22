// Currently there is not many features, nor do I plan to add them
// This is the foundation of a simple calculator app

// Gui libraries
#include <raylib.h>
#define RAYGUI_IMPLEMENTATION
#include <raygui.h>
#include <string.h>
#include "core.h"

const int WINDOW_WIDTH = 300;
const int WINDOW_HEIGHT = 400;
const int TEXTBOX_PADDING = 10;
const int BUTTON_SIZE = 60;
const int GRID_COLS = 4;
const int GRID_ROWS = 4;
const int GRID_SPACING = 5;
const int GRID_START_X = 20;
const int GRID_START_Y = 100;

ButtonDef buttonDefs[] = {
    // 1st row
    {"0", ACTION_NUMBER, "0"},
    {"1", ACTION_NUMBER, "1"},
    {"2", ACTION_NUMBER, "2"},
    {"/", ACTION_OPERATOR, "/"},

    // 2nd row
    {"3", ACTION_NUMBER, "3"},
    {"4", ACTION_NUMBER, "4"},
    {"5", ACTION_NUMBER, "5"},
    {"*", ACTION_OPERATOR, "*"},

    // 3rd row
    {"6", ACTION_NUMBER, "6"},
    {"7", ACTION_NUMBER, "7"},
    {"8", ACTION_NUMBER, "8"},
    {"-", ACTION_OPERATOR, "-"},

    // 4th row
    {"9", ACTION_NUMBER, "9"},
    {"CLR", ACTION_CLEAR, ""},
    {"=", ACTION_EQUALS, ""},
    {"+", ACTION_OPERATOR, "+"}
};

const int TOTAL_BUTTONS = sizeof(buttonDefs) / sizeof(buttonDefs[0]);

ButtonActionHandler actionHandlers[] = {
    [ACTION_NUMBER] = handleNumber,
    [ACTION_OPERATOR] = handleOperatorAction,
    [ACTION_CLEAR] = handleClearAction,
    [ACTION_EQUALS] = handleEqualsAction};

void setupGUI() {
    InitWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Calculator");
    SetTargetFPS(30);

    GuiSetStyle(DEFAULT, BASE_COLOR_NORMAL, 0xE0E0E0FF);
    GuiSetStyle(BUTTON, BORDER_WIDTH, 1);
    GuiSetStyle(DEFAULT, TEXT_SIZE, 20);
}

int main() {
    setupGUI();

    char display[256] = "0";
    char notificationText[256] = "";
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
            CheckCollisionPointRec(GetMousePosition(), textBoxRect)) {
            SetClipboardText(display);
            snprintf(notificationText, sizeof(notificationText), "Copied %s to the clipboard.", display);
            notificationTimer = 90; // 3 seconds, 30 FPS
        }

        GuiTextBox(textBoxRect, display, 256, false);

        if (notificationTimer > 0) {
            DrawText(notificationText, TEXTBOX_PADDING, 70, 10, DARKGRAY);
            notificationTimer--;
        }

        for (int i = 0; i < TOTAL_BUTTONS; i++) {
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;

            int x = GRID_START_X + col * (BUTTON_SIZE + GRID_SPACING);
            int y = GRID_START_Y + row * (BUTTON_SIZE + GRID_SPACING);

            if (GuiButton((Rectangle){x, y, BUTTON_SIZE, BUTTON_SIZE}, buttonDefs[i].label)) {
                ButtonActionHandler handler = actionHandlers[buttonDefs[i].type];
                if (handler) {
                    handler(display, buttonDefs[i].value);
                }
            }
        }

        EndDrawing();
    }

    CloseWindow();
    return 0;
}