#!/bin/bash

cd /tmp


#object WolfSSL {
 # const val extensionName = "wolfSSL"
#  const val sourceURL = "https://github.com/wolfSSL/wolfssl.git"
#  const val version = "5.6.3"
#  const val tag = "v5.6.3-stable"
#}
COMMIT=9ffa9faecda87a7c0ce7521c83996c65d4e86943
COMMIT=v5.6.2-stable
URL=https://github.com/wolfSSL/wolfssl.git



rm -rf /tmp/test*

git init --bare /tmp/test
cd /tmp/test
git remote add origin $URL
git fetch --depth 1 origin $COMMIT
git reset FETCH_HEAD --soft


git clone /tmp/test /tmp/test2
cd /tmp/test2
git log





