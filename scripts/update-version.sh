#!/bin/sh

POSITIONAL=()
while [[ $# -gt 0 ]]; do
	key="$1"
	case $key in
		-v|--version)
			VERSION=$2
			shift; shift; # past argument
			;;
		*) # unknown
			POSITIONAL+=("$1") # save it in an array for later
			shift # past argument
			;;
	esac
done

echo "Updating version to $VERSION"

sed -i '' -E "s/sdkVersionName(.*): \"[0-9a-zA-Z.-]+\"/sdkVersionName\1: \"$VERSION\"/" ./build.gradle
