#!/bin/sh

VERSION=$1

echo "Updating version to $VERSION"

sed -i '' -E "s/sdkVersionName(.*): \"[0-9a-zA-Z.-]+\"/sdkVersionName\1: \"$VERSION\"/" ../build.gradle
