#!/bin/bash

cd `dirname $0`





#DOWNLOAD=https://dl.google.com/android/repository/android-ndk-r23b-linux.zip

#[ ! -f "ndk.zip" ] && wget $DOWNLOAD -O ndk.zip

#docker  build --no-cache=false  -t danbrough/$NAME:latest --push .
#docker  build --no-cache=true   -t danbrough/$NAME:latest --push .

NOCACHE=false

docker  build --no-cache=$NOCACHE -t danbrough/debian:latest --push .
#docker buildx build --no-cache=$NOCACHE --platform linux/amd64,linux/arm64,linux/arm/7  -t danbrough/debian:latest --push .
#docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7   -t danbrough/debby:latest --push .
# docker buildx imagetools inspect danbrough/$NAME:latest




#curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -

#After that, add the repository to your sources list so that you can easily upgrade the Yarn package in future with the rest of the system updates:

#sudo sh -c 'echo "deb https://dl.yarnpkg.com/debian/ stable main" >> /etc/apt/sources.list.d/yarn.list'
