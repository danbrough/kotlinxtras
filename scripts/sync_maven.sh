#!/bin/bash

cd `dirname $0` 
rsync -avHSx ../xtras/maven/ maven:~/m2/

