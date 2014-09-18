include $(CLEAR_VARS)

# cryptsetup is built with the openssl backend and the luks1 

LOCAL_MODULE := cryptsetup
LOCAL_PATH := $(ROOT)/cryptsetup/lib
LOCAL_SRC_FILES := \
    setup.c \
    utils.c \
    utils_benchmark.c \
    utils_crypt.c \
    utils_loop.c \
    utils_devpath.c \
    utils_wipe.c \
    utils_fips.c \
    utils_device.c \
    libdevmapper.c \
    volumekey.c \
    random.c \
    crypt_plain.c \
    luks1/af.c \
    luks1/keymanage.c \
    loopaes/loopaes.c \
    tcrypt/tcrypt.c \
    crypto_backend/crypto_cipher_kernel.c \
    crypto_backend/crypto_storage.c \
    crypto_backend/pbkdf_check.c \
    crypto_backend/crc32.c \
    crypto_backend/crypto_openssl.c
    
LOCAL_C_INCLUDES := \
    $(ROOT) \
    $(LOCAL_PATH) \
    $(LOCAL_PATH)/crypto_backend \
    $(LOCAL_PATH)/luks1 \
    $(LOCAL_PATH)/loopaes \
    $(LOCAL_PATH)/verity \
    $(LOCAL_PATH)/tcrypt \
    $(ROOT)/lvm2/libdm

LOCAL_CFLAGS := -include $(ROOT)/cryptsetup-build/endian-fix.h \
                -include $(ROOT)/cryptsetup-build/config.h
LOCAL_SHARED_LIBRARIES := uuid devmapper

include $(BUILD_SHARED_LIBRARY)
