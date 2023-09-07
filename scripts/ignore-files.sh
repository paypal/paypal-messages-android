#!/bin/sh

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

FILES_LIST=("demo/src/main/res/values/locals.xml")

if [[ $ON ]];
then
	echo "Ignoring files"
	echo "git update-index --skip-worktree $FILES_LIST"
	# Ignores future changes to the specified files
	git update-index --skip-worktree $FILES_LIST
elif [[ $OFF ]];
then
	echo "Stop ignoring files"
	echo "git update-index --no-skip-worktree $FILES_LIST"
	# Stops ignoring files
	git update-index --no-skip-worktree $FILES_LIST
fi

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
