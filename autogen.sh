#!/bin/bash
# Indique au système que l'argument qui suit est le programme utilisé pour exécuter ce fichier
# En règle générale, les "#" servent à mettre en commentaire le texte qui suit comme ici
cd ./kalium-jni/src/main/jni/libsodium
sh ./autogen.sh
sh ./dist-build/android-arm.sh
sh ./dist-build/android-mips.sh
sh ./dist-build/android-x86.sh
 
exit 0
