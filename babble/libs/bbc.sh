# build babble core (bbc)
set -e

babble_core_version=test

if [ -d jni ]; then rm -rf jni; fi

if [ -d build ]; then rm -r build; fi
mkdir build
cd build

if [ -z ${GOPATH+x} ]; then 
    # GOPATH is not set, so we'll use the standard Go tree
    cp ~/go/src/github.com/mosaicnetworks/babble/src/mobile/build/${babble_core_version}/mobile.aar .
else 
    cp ${GOPATH}/src/github.com/mosaicnetworks/babble/src/mobile/build/${babble_core_version}/mobile.aar .
fi

unzip mobile.aar -d mobile.aar.unzip
cd ..
cp build/mobile.aar.unzip/classes.jar .
cp -r build/mobile.aar.unzip/jni .

rm -r build
