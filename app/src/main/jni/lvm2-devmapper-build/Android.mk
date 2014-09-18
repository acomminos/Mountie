include $(CLEAR_VARS)

# we only build libdevmapper

LOCAL_MODULE := devmapper 
LOCAL_PATH := $(ROOT)/lvm2/libdm
LOCAL_SRC_FILES := \
    datastruct/bitset.c \
    datastruct/hash.c \
    datastruct/list.c \
    libdm-common.c \
    libdm-file.c \
    libdm-deptree.c \
    libdm-string.c \
    libdm-report.c \
    libdm-config.c \
    mm/dbg_malloc.c \
    mm/pool.c \
    regex/matcher.c \
    regex/parse_rx.c \
    regex/ttree.c \
    ioctl/libdm-iface.c
    
LOCAL_C_INCLUDES := \
    $(LOCAL_PATH) \
    $(LOCAL_PATH)/ioctl \
    $(LOCAL_PATH)/misc \
    $(LOCAL_PATH)/../lib/misc \
    $(LOCAL_PATH)/../lib/log

LOCAL_CFLAGS := -DDM_DEVICE_UID=0 -DDM_DEVICE_GID=0 -DDM_DEVICE_MODE='0600' \
		-include $(ROOT)/lvm2-devmapper-build/android-fixes.h
LOCAL_SHARED_LIBRARIES := uuid
#LOCAL_LDLIBS := -lselinux -ludev -lpthread

include $(BUILD_SHARED_LIBRARY)
