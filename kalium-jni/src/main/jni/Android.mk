LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := sodium
LOCAL_SRC_FILES := /installs/libsodium/libsodium-android-$(TARGET_ARCH)/lib/libsodium.a #/installs/libsodium/libsodium-android-(x86|arm|mips)/lib/libsodium.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := kaliumjni
LOCAL_SRC_FILES := /home/eis/git/ucoin-android-app/kalium-jni/src/main/jni/sodium_wrap.c

LOCAL_CFLAGS   += -Wall -g -pedantic -std=c99

LOCAL_C_INCLUDES += /installs/libsodium/libsodium-android-$(TARGET_ARCH)/include /installs/libsodium/libsodium-android-$(TARGET_ARCH)/include/sodium
LOCAL_STATIC_LIBRARIES += android_native_app_glue sodium
#LOCAL_LDLIBS += -llog -lsodium


include $(BUILD_SHARED_LIBRARY)

