set -e

if [ -z "$JAVA_HOME" ]; then
    echo "ERROR You should set JAVA_HOME"
    echo "Exiting!"
    exit 1
fi


C_INCLUDE_PATH="${JAVA_HOME}/include:${JAVA_HOME}/include/linux:/System/Library/Frameworks/JavaVM.framework/Headers"
export C_INCLUDE_PATH

rm -f *.java
rm -f *.c
rm -f *.so

#swig -java sodium.i
swig -java -package org.abstractj.kalium -outdir ../java/org/abstractj/kalium sodium.i


jnilib=libkaliumjni.so
destlib=../libs
if uname -a | grep -q -i darwin; then
  jnilib=libkaliumjni.jnilib
fi


mkdir -p $destlib/armeabi
mkdir -p $destlib/mips
mkdir -p $destlib/x86

#In order to compile for arm/armv7/x86/mips you should build your own standalone android-toolchain as in libsodium:android-build.sh
#https://github.com/jedisct1/libsodium/blob/master/dist-build/android-build.sh
#And then use gcc binary from there. 

#arm:
echo $destlib/armeabi/$jnilib
/installs/libsodium/android-toolchain-arm/arm-linux-androideabi/bin/gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I/installs/libsodium/libsodium-android-arm/include sodium_wrap.c -shared -fPIC -L/installs/libsodium/libsodium-android-arm/lib -lsodium -o $jnilib
cp -r $jnilib $destlib/armeabi/

#mips:
echo $destlib/mips/$jnilib
/installs/libsodium/android-toolchain-mips/mipsel-linux-android/bin/gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I/installs/libsodium/libsodium-android-mips/include sodium_wrap.c -shared -fPIC -L/installs/libsodium/libsodium-android-mips/lib -lsodium -o $jnilib
cp -r $jnilib $destlib/mips/

#x86:
echo $destlib/x86/$jnilib
/installs/libsodium/android-toolchain-x86/i686-linux-android/bin/gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I/installs/libsodium/libsodium-android-x86/include sodium_wrap.c -shared -fPIC -L/installs/libsodium/libsodium-android-x86/lib -lsodium -o $jnilib
cp -r $jnilib $destlib/x86/

#Remove the last file (has been copied into $destDir)
rm -f $jnilib
#rm -f sodium_wrap.c