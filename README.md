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
Developpement use Android Studio (lastest version), and Android NDK (Native Developpement Kit) to be able to use TweetNaCl1 (a compact crypto library).

You should instal
- NDK :
  Use version r9d (version r10d has a bug: http://www.cocos2d-x.org/news/307)



