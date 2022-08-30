#!/bin/bash

extensionpath=$1


#check input arguments
if [ $# != 1 ]; then
	echo "------------------------------------------------------------------"
	echo "ERROR - usage:"
	echo "$(basename $0) command /path/to/extension"
	echo "Example: $basename $0 /repository/hybris/custom/core/core.helper"

	echo "------------------------------------------------------------------"
	exit 1
fi

#check if the full extension path exists
if [[ ! -e $1 ]]; then
	echo "ERROR: $1 doesn't exists"
	exit 1
fi

#check if the full extension path is a directory
if [[ ! -d $1 ]]; then
	echo "ERROR: $1 is not a directory"
	exit 1
fi

# copy gitignore file into extension root
cp gitignore $extensionpath/.gitignore

