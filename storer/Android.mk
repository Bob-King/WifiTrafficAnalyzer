LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES = $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../common

LOCAL_ARM_MODE := arm
LOCAL_MODULE := storer
LOCAL_SRC_FILES := main.c
#LOCAL_CFLAGS += -pie -fPIE
#LOCAL_LDFLAGS += -pie -fPIE
#LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
include $(BUILD_EXECUTABLE)
