# Duniter-app
Duniter Android client application.

Started on december 2014, still in progress... Developers need ! ;o)

## Features

Main idea is to be able :
- to manage a contact list, using (if possible) smartphone contacts
- to lookup/sign someone
- to paid someone (a store, ...)
- to paid from a smartphone to another, even if there is no Internet connection (like sending a signed transaction document ?)
- to see transfer history, with a indicator when a transaction has been processed by the blockchain
+ balance

And maybe :
- to manage multi-account.
  The idea is to never store salt/passwd of the main account (signed account for member with UD), but use attached accounts for daily transaction, with saved salt/passwd (because it's boring to fill it for each payment !). e.g. once by month, you should connect with the main account and transfer your UD on secondary accounts. So if you loose your smartphone, you keep your main account secure.
The connection to the main account could also be asked automatically (when a transfer from secondaries account's could not be done)

## Developers
Developpement use Android Studio , and Android NDK (Native Developpement Kit) to be able to use TweetNaCl (a compact crypto library).

You should install
- [Android Studio](https://developer.android.com/sdk/index.html)
- [NDK (Native Developpement Kit)](https://developer.android.com/tools/sdk/ndk/index.html): use android-ndk-r10d (64bit)
- Configure the Android Studio project : edit the local.properties file, use by Gradle
```
sdk.dir=/path/to/android-sdks
ndk.dir=/path/to/android-ndks/android-ndk-r10d
```

- Install dependencies for needed for kalium-jni compilation
```
sudo apt-get install build-essential libpcre3 libpcre3-dev libtool automake python-dev
```
- Clone the source repository from GitHub and generate static libsodium for all Android architectures using the following instructions :

	First export the path of the android NDK previously installed
	```
	export ANDROID_NDK_HOME=/absolutepath/to/android-ndk
	```

	Then

	```
	git clone https://github.com/duniter/duniter-android-app.git --recursive
	cd duniter-android-app
	./autogen.sh
	```