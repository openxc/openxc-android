#!/bin/sh

set -ex

CURRENT_BRANCH=`git symbolic-ref HEAD 2>/dev/null | awk -F/ {'print $NF'}`
TEMP_PATH=/tmp/openxc-apidocs
rm -rf $TEMP_PATH
cp -R library/build/docs/javadoc/ $TEMP_PATH
git checkout gh-pages
git pull
rm -rf *.html references assets reference resources
cp -R $TEMP_PATH/* .
git add -A
git commit -m "Update Javadocs."
git push
git checkout $CURRENT_BRANCH
