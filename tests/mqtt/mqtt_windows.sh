#!/bin/bash

cd "$(dirname "$0")"

source env.sh



[ -d $BUILD ] && rm -rf $BUILD
mkdir $BUILD

export OPENSSL=/home/dan/workspace/xtras/xtras/libs/openSSL/$VERSION/mingwX64



#PATH=$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH
#export PATH=$NDK/prebuilt/linux-x86_64/bin:$PATH
#PATH=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/bin:$PATH

#export AR=llvm-ar
#export RANDLIB=ranlib
#export CC="clang --target=aarch64-linux-android"
#export CC=aarch64-linux-android21-clang
#export LD_LIBRARY_PATH=/mnt/files/sdk/android/ndk/25.0.8775105/toolchains/llvm/prebuilt/linux-x86_64/lib64/
#export LD=ld

export LD_LIBRARY_PATH=$DEMOS
export PATH=$DEMOS:$PATH

echo PREFIX $PREFIX
[ -d $PREFIX ] && rm -rf $PREFIX

cd $SRC && git clean -xdf
#export CC=aarch64-linux-android21-clang
export LD_FLAGS="-L/home/dan/.wine/drive_c/windows/syswow64"
export PATH=/home/dan/.wine/drive_c/windows/syswow64:$PATH
#export LD_LIBRARY_PATH=$OPENSSL/lib:lib:$DEMOS/build/src:
#-DCMAKE_COMPILER_TARGET=$CC
export CC=clang

OPTS="-DPAHO_BUILD_DOCUMENTATION=FALSE -DPAHO_BUILD_SAMPLES=FALSE -DCMAKE_MINIMUM_REQUIRED_VERSION=2.8 \
-DCMAKE_INSTALL_PREFIX=$PREFIX \
-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc \
-DCMAKE_SYSTEM_NAME=Windows \
-DCMAKE_SYSTEM_VERSION=1 \
-DPAHO_BUILD_STATIC=FALSE \
-DPAHO_ENABLE_TESTING=FALSE -DPAHO_WITH_SSL=TRUE \
-DOPENSSL_ROOT_DIR=$OPENSSL  \
 -DOPENSSL_LIBRARIES=$OPENSSL/bin -DOPENSSL_INCLUDE_DIR=$OPENSSL/include "

#-DCMAKE_TOOLCHAIN_FILE=$SRC/cmake/toolchain.win64.cmake \
#-DOPENSSL_CRYPTO_LIBRARY=/home/dan/workspace/xtras/xtras/libs/openSSL/3.1.3/androidNativeArm64/lib/libcrypto.so \



cd $BUILD && cmake $OPTS $SRC && make install

#cmake -G "Visual Studio 17 2022" -A x64 -Bbuild -H.
#[ -d ../build ] && rm -rf ../build
#mkdir ../build
#cmake -Bbuild -H. $OPTS  -DCMAKE_INSTALL_PREFIX=$PREFIX || exit 1
#cmake -Bbuild -H. $OPTS   || exit 1
#cd build && make install

