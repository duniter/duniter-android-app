LOCAL_PATH := $(call my-dir)

# scrypt

include $(CLEAR_VARS)

LOCAL_MODULE    := scrypt
LOCAL_SRC_FILES := scrypt_jni.c crypto_scrypt-nosse.c sha256.c
LOCAL_CFLAGS += -std=c99 -Wall -O2
LOCAL_CFLAGS += -DHAVE_CONFIG_H
LOCAL_LDFLAGS += -lc -shared

LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)