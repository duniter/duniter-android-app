LOCAL_PATH := $(call my-dir)
#I don't know why the first call to my-dir does not return the dir this file is in
CURRENT_DIR := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := sodium
LOCAL_SRC_FILES = $(CURRENT_DIR)/libsodium/libsodium-android-$(TARGET_ARCH)/lib/libsodium.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := kaliumjni
LOCAL_SRC_FILES := $(CURRENT_DIR)/sodium_wrap.c

LOCAL_CFLAGS   += -Wall -g -pedantic -std=c99

LOCAL_C_INCLUDES += $(CURRENT_DIR)/libsodium/libsodium-android-$(TARGET_ARCH)/include
LOCAL_C_INCLUDES += $(CURRENT_DIR)/libsodium/libsodium-android-$(TARGET_ARCH)/include/sodium
#LOCAL_C_INCLUDES += $(CURRENT_DIR)/libsodium/libsodium-android-$(TARGET_ARCH)/include/python2.7
LOCAL_STATIC_LIBRARIES += android_native_app_glue sodium

include $(BUILD_SHARED_LIBRARY)

