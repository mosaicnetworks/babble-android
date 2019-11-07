# build babble core (bbc)
set -e


mydir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" > /dev/null && pwd )"


# Create tmp working sub directory
if [ -d "${mydir}/tmp" ] ; then
    rm -rf "${mydir}/tmp"
fi
mkdir -p "${mydir}/tmp"





if [ ! -z "$GOPATH" ] ; then 
    ZIPPATH="$GOPATH/src/github.com/mosaicnetworks/babble/build/distmobile"
else
    ZIPPATH="$HOME/go/src/github.com/mosaicnetworks/babble/build/distmobile"   
fi   

if [ ! -d "$ZIPPATH" ] ; then
    echo "$ZIPPATH not found. Aborting."
    exit 2
fi

ZIP=$(ls -1 $ZIPPATH/babble_*_android_library.zip)
ret=$?

if [ $ret -ne 0 ] ; then
    echo "Cannot find any android_library.zip files"
    exit 3
fi


STRIPPED=$(basename $ZIP _android_library.zip)
BABBLE_CORE_RELEASE_VERSION=${STRIPPED#babble_}


cp $ZIP ${mydir}/tmp/babble.zip
ret=$?

if [ $ret -ne 0 ] ; then
    echo "Failed to copy $ZIP"
fi

# unzip into the tmp folder
unzip ${mydir}/tmp/babble.zip -d ${mydir}/tmp

if [ -d "${mydir}/jni" ]; then rm -rf "${mydir}/jni"; fi

cd "${mydir}/tmp"

unzip babble_${BABBLE_CORE_RELEASE_VERSION}_mobile.aar -d unzip

cd ${mydir}
cp tmp/unzip/classes.jar .
cp -r tmp/unzip/jni .

rm -rf tmp
