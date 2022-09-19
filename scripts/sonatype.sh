#!/bin/bash

cd `dirname "$0"`


[ ! -f "~/.sonarc" ] && touch ~/.sonarc
source ~/.sonarc

# SONA_PROFILE_ID is something like 92fd911232c8f
[ -z "SONA_USER" ] &&  echo SONA_USER not set && exit 1
[ -z "SONA_PASSWORD" ] &&  echo SONA_PASSWORD not set && exit 1
[ -z "$SONA_PROFILE_ID" ] &&  echo SONA_PROFILE_ID not set && exit 1

SONA_BASE_URL=https://s01.oss.sonatype.org/service/local

#SONA_OPEN_URL="https://s01.oss.sonatype.org/service/local/staging/profiles/$SONA_PROFILE_ID/start"

# for auto creating of staging repository
#SONA_URL="https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

# maven url for a particular repository
#SONA_URL="https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$SONA_REPO_ID/
#example https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/blah-1172

CURL="curl -X POST -u $SONA_USER:$SONA_PASSWORD -H Content-Type:application/xml "



function sona_open_payload(){
cat <<HERE
<promoteRequest>
    <data>
        <description>$@</description>
    </data>
</promoteRequest>
HERE
}

function sona_open_repo(){
  rm sona.log 2> /dev/null
  sona_open_payload $1   | $CURL -d @- -v $SONA_BASE_URL/staging/profiles/$SONA_PROFILE_ID/start | tee sona.log
  export SONA_REPO_ID="$(cat sona.log | sed -n -e "/stagedRepository/{s|\s*</*stagedRepositoryId>||g p}")"
  sed -i ~/.sonarc -e '/SONA_REPO_ID/d'
  echo SONA_REPO_ID=$SONA_REPO_ID >> ~/.sonarc
  echo SONA_REPO_ID=$SONA_REPO_ID
  sed -i ../gradle.properties  -e 's|^sonatypeRepoId=.*|sonatypeRepoId='$SONA_REPO_ID'|g'
}

echo SONA_REPO_ID is $SONA_REPO_ID


#Finally, you can close the staging repository with a POST request to ""/service/local/staging/profiles/<profile-id>/finish". The payload for this request is:

function sona_close_payload(){
cat <<HERE
<promoteRequest>
    <data>
        <stagedRepositoryId>$1</stagedRepositoryId>
        <description>$2</description>
    </data>
</promoteRequest>
HERE
}


function sona_close_repo(){
  sona_close_payload $SONA_REPO_ID $1 | $CURL POST -d @- -v $SONA_BASE_URL/staging/profiles/$SONA_PROFILE_ID/finish
  sed -i ../gradle.properties  -e 's|^sonatypeRepoId=.*|sonatypeRepoId=|g'
}

if [ "$1" == "open" ]; then
  echo opening repo .. with description "$2"
  sona_open_repo "$2"
elif [ "$1" == "close" ]; then
  echo closing repo $SONA_REPO_ID
  sona_close_repo "$@" && rm sona.log 2> /dev/null
else
  echo "usage $0 open|close description"
fi


