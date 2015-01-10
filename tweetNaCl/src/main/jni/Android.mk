LOCAL_PATH := $(call my-dir)

# tweetnacl

include $(CLEAR_VARS)

LOCAL_MODULE    := tweetnacl
LOCAL_SRC_FILES := tweetnacl-jni.c tweetnacl.c devurandom.c
LOCAL_LDLIBS    := -llog 

include $(BUILD_SHARED_LIBRARY)
