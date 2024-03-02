#!/bin/sh

VERSION="\"$1\""
VERSION_MATCH='"[0-9a-zA-Z.-]+"'

echo "Updating version to $VERSION"

sed -i "s/sdkVersionName(.*): $VERSION_MATCH/sdkVersionName\1: $VERSION/" ./build.gradle
