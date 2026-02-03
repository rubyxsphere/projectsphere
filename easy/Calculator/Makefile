# Trying to make this work on both windows and linux
CC = gcc
CFLAGS = -Iinclude -Isrc -Wall -Wextra -O2 -Wno-unused-parameter

# Cross-platform settings
ifeq ($(OS),Windows_NT)
    LDFLAGS = -lraylib -lopengl32 -lgdi32 -lwinmm -s -mwindows
    CLEAN_CMD = cmd /c "if exist $(BUILD_DIR) rd /s /q $(BUILD_DIR)"
    MKDIR_CMD = cmd /c if not exist $(BUILD_DIR) mkdir $(BUILD_DIR)
    EXE_EXT = .exe
else
    LDFLAGS = -lraylib -lGL -lm -lpthread -ldl -lrt -lX11 -s
    CLEAN_CMD = rm -rf $(BUILD_DIR)
    MKDIR_CMD = mkdir -p $(BUILD_DIR)
    EXE_EXT =
endif

# General settings
SRC_DIR = src
BUILD_DIR = build
TARGET = $(BUILD_DIR)/calculator$(EXE_EXT)

SRCS = $(wildcard $(SRC_DIR)/*.c)
OBJS = $(patsubst $(SRC_DIR)/%.c,$(BUILD_DIR)/%.o,$(SRCS))

all: clean $(TARGET)

$(BUILD_DIR):
	$(MKDIR_CMD)

$(TARGET): $(BUILD_DIR) $(OBJS)
	$(CC) $(OBJS) -o $(TARGET) $(LDFLAGS)

# Pattern rule for building object files
$(BUILD_DIR)/%.o: $(SRC_DIR)/%.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	-$(CLEAN_CMD)

.PHONY: all clean