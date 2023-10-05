
NDK=/mnt/files/sdk/android/ndk/25.0.8775105
#NDK=/mnt/files/sdk/android/ndk/22.1.7171670/
DEMOS=`pwd`
#ARCH=aarch64
SRC=$DEMOS/src/mqtt
BUILD=$DEMOS/build

VERSION=1_1_1w
VERSION=3.1.3


export ANDROID_ABI=x86
export OPENSSL=/home/dan/workspace/xtras/xtras/libs/openSSL/$VERSION/androidNativeX86


export ANDROID_ABI=x86_64
export OPENSSL=/home/dan/workspace/xtras/xtras/libs/openSSL/$VERSION/androidNativeX64

export ANDROID_ABI=arm64-v8a
export OPENSSL=/home/dan/workspace/xtras/xtras/libs/openSSL/$VERSION/androidNativeArm64

export ANDROID_ABI=armeabi-v7a
export OPENSSL=/home/dan/workspace/xtras/xtras/libs/openSSL/$VERSION/androidNativeArm32

export PREFIX=$DEMOS/mqtt/$ANDROID_ABI

#OPENSSL=/home/dan/workspace/xtras/xtras/libs/openSSL/3.1.3/androidNativeArm64
#OPENSSL=/home/dan/workspace/xtras/xtras/build/openSSL/3.1.3/androidNativeArm64

[ -d $BUILD ] && rm -rf $BUILD
mkdir $BUILD


if [ ! -d $SRC ]; then
    [ ! -d src ] && mkdir src
    git clone https://github.com/eclipse/paho.mqtt.c.git src/mqtt
    cd src/mqtt && git checkout v1.3.12 && cd $DEMOS
fi
