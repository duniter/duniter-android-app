# ucoin-android-app
uCoin Android client application.

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
- Android Studio: lastest version
- NDK (Native Developpement Kit): use android-ndk-r10d (64bit)
- clone the source repository from GitHub
- Configure the Android Studio project : edit the local.properties file, use by Gradle



