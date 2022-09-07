#!/bin/bash

cd "$(dirname "$0")"

TARGET=android-x86
API=21
export ANDROID_API=$API

export CFLAGS="-D__ANDROID_API__=$API -fPIC"
export LD_FLAGS="-lm -lc -ldl"
PREFIX_PATH=/opt/kotlinxtras/libs/openssl/OpenSSL_1_1_1q
export CC=clang
export CFLAGS="-O3  -Wno-macro-redefined -Wno-deprecated-declarations"
export PREFIX="$PREFIX_PATH/androidNativeX86"
export PATH=/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin

#export ANDROID_NDK_HOME="$(realpath ~/.konan/dependencies/target-toolchain-2-linux-android_ndk)"
#export CROSS_SYSROOT="$KONAN_DATA_DIR/dependencies/target-sysroot-1-android_ndk/android-$API/arch-$ARCH"


function configure_ndk() {
  export ANDROID_NDK_HOME=/mnt/files/sdk/android/ndk/25.0.8775105
  export PATH=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH
}


function configure_kotlin() {
  #export ANDROID_NDK_HOME=/mnt/files/sdk/android/ndk/25.0.8775105
  export ANDROID_NDK_HOME=$KONAN_DATA_DIR/dependencies/target-toolchain-2-linux-android_ndk
  export PATH=$ANDROID_NDK_HOME/bin:$PATH
}

configure_ndk
#configure_kotlin

cd src/openssl
( git reset --hard && git clean -xdf ) > /dev/null 2>&1

./Configure   $TARGET no-tests threads -D__ANDROID_API__=$API --prefix=$PREFIX | tee configure.log  || exit 1
#./Configure   $TARGET no-tests no-stdio -D__ANDROID_API__=$API --prefix=$PREFIX > configure.log 2>&1 || exit 1
make  -j5 | tee build.log
make install_sw -j5



#PREFIX="$(realpath lib/androidNativeX86)"
#cd src/openssl/
##git reset --hard && git clean -xdf
#
#./Configure   $TARGET no-tests no-stdio -D__ANDROID_API__=$API --prefix=$PREFIX
#sed -i Makefile -e 's|^AR=.*|AR=llvm-ar|g' -e 's|^RANLIB=.*|RANLIB=ranlib|g'
#
#make install_sw -j5

