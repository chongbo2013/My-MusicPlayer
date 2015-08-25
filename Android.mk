LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
  src/com/lewa/player/IMediaPlaybackService.aidl

LOCAL_STATIC_JAVA_LIBRARIES := entaged \
    android-support-v13 \
    android-support-v4 \
    baiduTing \
    lewa-support-v7-appcompat \
    com.lewa.themes
LOCAL_JAVA_LIBRARIES += lewa-framework
LOCAL_PACKAGE_NAME := LewaPlayer
LOCAL_CERTIFICATE := shared
LOCAL_OVERRIDES_PACKAGES := Music

#LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
#LOCAL_PROGUARD_ENABLED := full

LOCAL_JNI_SHARED_LIBRARIES += libaudiocore libaudiofp libBDmfemusic_V1
	
LOCAL_MULTILIB := 32

LOCAL_RESOURCE_DIR = \
    $(LOCAL_PATH)/res \
    vendor/lewa/apps/LewaSupportLib/actionbar_4.4/res \

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages lewa.support.v7.appcompat

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := entaged:lib/entagged.jar baiduTing:lib/baidu-music-sdk-v2.1.9.jar
include $(BUILD_MULTI_PREBUILT)
 
