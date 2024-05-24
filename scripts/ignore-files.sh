#!/bin/sh

# Add files to be ignored into ignore-files-list.txt
# Files can be ignored with
#   ./scripts/ignore-files.sh -y
# Files can stop being ignored with
#   ./scripts/ignore-files.sh -n
# Ignored files can be listed with
#   ./scripts/ignore-files.sh -l

POSITIONAL=()
while [[ $# -gt 0 ]]; do
	key="$1"
	case $key in
		-o|--on|-y|--yes)
			ON=1
			shift # past argument
			;;
		-f|--off|-n|--no)
			OFF=1
			shift # past argument
			;;
		-l|--list)
			LIST=1
			shift # past argument
			;;
		*) # unknown
			POSITIONAL+=("$1") # save it in an array for later
			shift # past argument
			;;
	esac
done

# Change to root folder (paypal-messages-android)
SCRIPT_PATH=$(dirname "${BASH_SOURCE[0]}")
PARENT_PATH=$(cd "$SCRIPT_PATH" ; pwd -P)
cd $PARENT_PATH/..

if [[ $ON ]];
then
	echo "Ignoring files"
elif [[ $OFF ]];
then
	echo "Stop ignoring files"
fi

ignoreFilesList="scripts/ignore-files-list.txt"
exec 3<$ignoreFilesList

while
	IFS= read -r ignoreFileName <&3
do
	if [[ $ON ]];
	then
		echo "git update-index --skip-worktree $ignoreFileName"
		# Ignores future changes to the specified files
		git update-index --skip-worktree $ignoreFileName
	elif [[ $OFF ]];
	then
		echo "git update-index --no-skip-worktree $ignoreFileName"
		# Stops ignoring files
		git update-index --no-skip-worktree $ignoreFileName
	fi
done

if [[ $LIST ]];
then
	echo "List ignored files"
	echo "git ls-files -v . | grep ^S"
	git ls-files -v . | grep ^S
fi

# Ignore future changes to a file with this command, after you've committed it
# git update-index --skip-worktree file_name

# Stop ignoring a file with this command
# git update-index --no-skip-worktree file_name

# See all files that are ignored by git with this command
# git ls-files -v . | grep ^S
