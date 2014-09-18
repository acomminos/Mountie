# Copyright (C) 2014 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
ROOT := $(call my-dir)

#$(call import-module, cryptsetup-android)
include $(ROOT)/uuid/Android.mk
include $(ROOT)/lvm2-devmapper-build/Android.mk
include $(ROOT)/cryptsetup-build/Android.mk

include $(CLEAR_VARS)

LOCAL_MODULE := mountie
LOCAL_SHARED_LIBRARIES := devmapper cryptsetup

include $(BUILD_SHARED_LIBRARY)
