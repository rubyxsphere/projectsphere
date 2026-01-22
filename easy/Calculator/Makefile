# Trying to make this work on both windows and linux
CC = gcc
CFLAGS = -Iinclude -Wall -Wextra -O2

# Cross-platform settings
ifeq ($(OS),Windows_NT)
    LDFLAGS = -lraylib -lopengl32 -lgdi32 -lwinmm -s -mwindows
    CLEAN_CMD = cmd /c "if exist $(BUILD_DIR) rd /s /q $(BUILD_DIR)"
    MKDIR_CMD = cmd /c if not exist $(BUILD_DIR) mkdir $(BUILD_DIR)
else
    LDFLAGS = -lraylib -lGL -lm -lpthread -ldl -lrt -lX11 -s
    CLEAN_CMD = rm -rf $(BUILD_DIR)
    MKDIR_CMD = mkdir -p $(BUILD_DIR)
endif

# General settings
SRC_DIR = src
BUILD_DIR = build
SRCS = $(SRC_DIR)/main.c $(SRC_DIR)/core.c
OBJS = $(BUILD_DIR)/main.o $(BUILD_DIR)/core.o
TARGET = $(BUILD_DIR)/calculator

all: clean $(TARGET)

$(BUILD_DIR):
	$(MKDIR_CMD)

$(TARGET): $(BUILD_DIR) $(OBJS)
	$(CC) $(OBJS) -o $(TARGET) $(LDFLAGS)

$(BUILD_DIR)/main.o: $(SRC_DIR)/main.c
	$(CC) $(CFLAGS) -c $< -o $@

$(BUILD_DIR)/core.o: $(SRC_DIR)/core.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	-$(CLEAN_CMD)

.PHONY: all clean