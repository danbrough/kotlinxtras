#!/bin/bash

cd `dirname $0`

export SRCDIR=$(realpath ..)

PLATFORM=linux/amd64

DOCKER_IMAGE=docker.io/danbrough/debian:latest
NAME=kipfs_$(echo $PLATFORM | sed -e 's:\/:_:g')
USER=bob
TMPDIR=/tmp/$NAME
HOMEDIR=/src

CACHEDIR="$HOME/.cache"

[ ! -d $TMPDIR ] && mkdir $TMPDIR

if [ ! -z "$@" ]; then
  DOCKER_COMMAND="$@"
else
  DOCKER_COMMAND=bash
fi


CMD="docker run -it  --tmpfs /tmp \
  --name "${NAME}" \
  --platform=$PLATFORM -h ${NAME} \
	-v $SRCDIR:$HOMEDIR \
	-v $CACHEDIR:$HOMEDIR/.cache \
	-v "$HOME/.konan:$HOMEDIR/.konan" \
	--rm -u $USER -w $HOMEDIR \
	$DOCKER_IMAGE  $DOCKER_COMMAND"

#	-v $ANDROID_HOME:/opt/sdk/android  \


echo running $CMD
$CMD
#--rm -u $USER -w /home/$NAME \
#	--mount type=tmpfs,destination=/tmp \

