#!/bin/bash
# build babble core (bbc)
set -e

BABBLE_CORE_RELEASE_VERSION="0.5.9"

mydir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" > /dev/null && pwd )"


# Create tmp working sub directory
if [ -d "${mydir}/tmp" ] ; then
    rm -rf "${mydir}/tmp"
fi
mkdir -p "${mydir}/tmp"

# Get released library
RELEASE_ZIP="https://github.com/mosaicnetworks/babble/releases/download/v${BABBLE_CORE_RELEASE_VERSION}/babble_${BABBLE_CORE_RELEASE_VERSION}_android_library_dev.zip"

wget -q -O ${mydir}/tmp/babble.zip "$RELEASE_ZIP" 
ret=$?

if [ $ret -ne 0 ] ; then
    echo "Failed to download release $RELEASE_ZIP"
fi

# unzip into the tmp folder
unzip ${mydir}/tmp/babble.zip -d ${mydir}/tmp

if [ -d "${mydir}/jni" ]; then rm -rf "${mydir}/jni"; fi

cd "${mydir}/tmp"

unzip babble_${BABBLE_CORE_RELEASE_VERSION}_mobile.aar -d unzip

cd ${mydir}
cp tmp/unzip/classes.jar .
cp -r tmp/unzip/jni .
[[ -e tmp/git.version ]] && cp tmp/git.version .

rm -rf tmp
