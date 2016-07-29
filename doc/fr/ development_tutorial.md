## Introduction

Cet article est un tutoriel d'initiation au code source de l'application Duniter App. Celui-ci vous permettra, à travers une succession d'étapes, d'accéder à la maîtrise des outils et méthodes utilisés quotidiennement par les développeurs de Duniter App pour créer et modifier l'application.

À la fin de ce tutoriel, vous serez donc <em>capable de modifier l'application</em>. Et si le cœur vous en dit, vous pourrez même réaliser une modification et partager celle-ci avec le dépôt de code principal, afin que celle-ci soit officiellement intégrée et disponible aux utilisateurs !

À vos claviers !

###Sommaire

* Niveau I : Récupérer le code source
* Niveau II : Installation logiciel
* Niveau III : mise en place de la librairie Libsodium
* Niveau IV : récupérer le projet
* Niveau V : Lancer l'application
* Niveau VI : À vous

##Niveau I : Récupérer le code source

Ce premier niveau consiste à créer <em>votre propre version</em> des sources du logiciel et de récupérer cette copie sur votre ordinateur. Vous y produirez :

* Votre propre compte <em>GitHub</em>
* Votre propre version du logiciel, votre <em>fork</em>
* Une copie locale des fichiers de code source provenant de votre <em>fork</em>

###Créez un compte GitHub

> Si vous disposez déjà d'un compte GitHub, vous pouvez passer cette étape.

Rendez-vous sur <a href="https://github.com" rel="nofollow">https://github.com</a> (site en anglais). Renseigner les 3 champs proposés :

* Nom d'utilisateur
* E-mail
* Mot de passe

<p><img src="https://forum.duniter.org/uploads/default/original/1X/13ade346327b73bbf1acc97027af147eeb4e9089.png" width="346" height="325"></p>

<p>Vous recevrez probablement un e-mail de confirmation qu'il vous faudra valider. Une fois cette étape passée, vous devriez disposer d'un compte GitHub .</p>

###Forkez le dépôt principal

<blockquote><p>Si vous avez déjà forké le dépôt principal <a href="https://github.com/duniter/duniter-android-app" rel="nofollow">duniter/duniter-android-app</a>, vous pouvez passer cette étape.</p></blockquote>

<p>Rendez-vous à l'adresse <a href="https://github.com/duniter/duniter-android-app" rel="nofollow">https://github.com/duniter/duniter-android-app</a>. Cliquez sur le bouton « Fork » en dans le coin supérieur droit de la page :</p>

<p><img src="https://forum.duniter.org/uploads/default/original/1X/3b9228c664520496d6a7e86e3f9c4c438f111914.png" width="388" height="98"></p>

<p>Vous aurez alors <em>votre propre version</em> du code de Duniter, dans <em>votre dépôt</em> GitHub :</p>

<p><img src="https://forum.duniter.org/uploads/default/original/1X/590e8e80dfbb23c72bbe77ce4ce5881a07e9722a.png" width="307"></p>

<h3>Installer Git</h3>

<p>L'installation de Git dépend de votre système d'exploitation. Suivez simplement les indications présentes sur : <a href="https://git-scm.com/" rel="nofollow">https://git-scm.com/</a></p>

<h3>Cloner votre fork</h3>

<p>A ce stade, vous êtes en mesure de récupérer votre version du code source (votre <em>fork</em>), afin de pouvoir travailler dessus.</p>

<h4>Ouvrez Git en ligne de commande</h4>

<p>Pour récupérer le code source, lancez Git en mode console.</p>

<ul>
<li>Sous Linux et MacOS, ouvrez tout simplement le Terminal</li>
<li>Sous Windows lancez le programme <em>Git Bash</em> :</li>
</ul>

<p><img src="https://forum.duniter.org/uploads/default/original/1X/6fc638dc0a22d88da7e84dbf0371e69747767f78.png" width="432" height="80"></p>

<h4>Clonez votre fork</h4>

<p>Retournez sur la page web GitHub, puis trouvez le bouton « Clone or download » : </p>

<p><img src="https://forum.duniter.org/uploads/default/original/1X/b012974929db5ff5a1d6c16a85b061902a2ea830.png" width="492" height="117"></p>

<p>Cliquez dessus, vous pourrez alors copier l'URL de clonage en cliquant sur l'icône de valise : </p>

<img src="https://forum.duniter.org/uploads/default/original/1X/97f75b4ff9774b9c7503fc4de027df36d34b498f.png" width="471">

<p>Vous n'avez plus qu'à retourner dans votre console Git et saisir : </p>

<p></p><pre><code>git clone [coller l'URL copiée] --recursive</code></pre>

<p>ce qui donne dans mon cas : </p>

<p></p><pre><code class="hljs cs">git clone https://github.com/naivalf27/duniter-android-app.git --recursive
Cloning into 'duniter-android-app'...
remote: Counting objects: 6212, done.
remote: Total 6212 (delta 0), reused 0 (delta 0), pack-reused 6212
Receiving objects: 100% (6212/6212), 6.26 MiB | 793.00 KiB/s, done.
Resolving deltas: 100% (3594/3594), done.
Checking connectivity... done.
Submodule 'kalium-jni/src/main/jni/libsodium' (https://github.com/jedisct1/libsodium) registered for path 'kalium-jni/src/main/jni/libsodium'
Cloning into 'kalium-jni/src/main/jni/libsodium'...
remote: Counting objects: 18327, done.
remote: Total 18327 (delta 0), reused 0 (delta 0), pack-reused 18327
Receiving objects: 100% (18327/18327), 5.12 MiB | 461.00 KiB/s, done.
Resolving deltas: 100% (10127/10127), done.
Checking connectivity... done.
Submodule path 'kalium-jni/src/main/jni/libsodium': checked out '194a3c60480dde18150eff18b829b0c17c4dc19b'</code></pre>

<p>Si vous êtes arrivés à un comportement similaire, <strong>bravo</strong>, vous posséder désormais le code source Duniter App!</p>

<h2>Niveau II : Installation logiciel</h2>

<p>Ce second niveau vise à obtenir les outils de base pour exécuter le code source, et vérifier son bon fonctionnement. Vous y réaliserez : </p>

<ul>
<li>l'installation du <em>JDK</em></li>
<li>l'installation du logiciel <em>Android Studio</em></li>
<li>l'installation du <em>NDK Android</em></li>
</ul>

<h3>Installer JDK</h3>

Le JDK ou _Java Development Kit_ 

Vous pouvez la télécharger sur le site [d'Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

<img src="https://forum.duniter.org/uploads/default/original/1X/fef4f4dfe7c2168cb27c9e7f5e399fd547ce774a.png" width="400">

À vous de télécharger le bon fichier en fonction de votre OS.

Il ne vous reste plus qu'à l'installer normalement.

<h3>Installer Android Studio</h3>

Vous trouverez toute les sources a cette adresse [AndroidStudio-Downloads](https://developer.android.com/studio/index.html#downloads)

Pour Windows télécharger le fichier sans SDK Android:

<img src="https://forum.duniter.org/uploads/default/original/1X/3b8fa2f5c0465b13ae5ce74d49702e0c9f027866.png" width="690" height="237">

<h4>Sous Linux</h4>

Il vous suffit de décompresser le fichier ZIP, d'ouvrir un terminal dans ce dossier et de taper la commande:

> ./bin/studio.sh

<h4>Sous Windows et Mac OS</h4>

Il vous suffit d'installer l'exécutable que vous venez de télécharger.

<h4>Toutes machines confondues</h4>

A la fin de l'installation ou au premier lancement, Andorid Studio vous indiqueras que vous ne possédez pas de SDK et vous proposera de l'installer. Si vous l'avez déjà installé vous pouvez indiqué ou il se trouve.Sinon installer la version qu'il vous propose.

<h3>Installer NDK</h3>

Le NDK est utiliser pour l'exécution de code sous C++.
Vous pouvez le télécherger sur [ce site](https://developer.android.com/ndk/downloads/index.html)

Attention : n'installer pas la version 12 du NDK. Elle n'est pas encore stable.

Encore une fois télécharger la version qui vous concerne et décompresser le à coté de votre SDK.

Dans android studio aller dans `Project Structure...`

<img src="https://forum.duniter.org/uploads/default/original/1X/04e64b769cbd45b9d275cd5f81002a399a1a7684.png" width="300">

Une fenêtre comme celle-ci devrait s'ouvrir : 

<img src="https://forum.duniter.org/uploads/default/original/1X/ceb75301172038e75f5c43b328dd7febd7bedc7e.png" width="450">

Vous pouvez donc renseigner la position du NDK à cette endroit.

<h2>Niveau III : mise en place de la librairie Libsodium</h2>

<h3>Sous Linux</h3>

Aller dans votre clone du projet Duniter-android-app et lancer un terminal.

Une fois dedans lancer la commande:

> ./autogen.sh

Vous n'avez plus qu'à patienter.

<h4>Sous Windows et Mac OS</h4>

Je suis désoler mais je n'ai pas encore trouver de solution autre que récupérer les fichier depuis un post sous Linux.

<h2>Niveau IV : récupérer le projet</h2>

Lors du lancement d'Android Studio vous arriver sur cette fenetre:

<img src="https://forum.duniter.org/uploads/default/original/1X/33266d44fdbfd6c8b44e46a3664edafacaf0a316.png" width="500">

Sélectionner : "Open an existing Android Studio project"
et indiquer le dossier de votre clone.

<h2>Niveau V : Lancer l'application</h2>

Pour pouvoir lancer un émulateur, on va devoir en créer un.

Pour cela cliqué sur l'icone suivante :

<img src="https://forum.duniter.org/uploads/default/original/1X/46e959d1e616e34972a41f4d120a1d4f5beb0955.png" width="690" height="42">

Une fenêtre va s'ouvrir et vous proposer de créer un "Virtual Device" suivez le logiciel.

Si vous avez un téléphone Android vous pouvez le mettre en mode développeur et le brancher si vous souhaitez vois l'application sur votre téléphone.

Puis une fois l'émulateur de créer vous pouvez le bouton "Play" (<img src="https://forum.duniter.org/uploads/default/original/1X/70b2ce88a5e7aa5754f6a771cf5efed3c639a27b.png" width="46" height="44">) pour lancer l'application.
Vous pouvez aussi utiliser l'icone (<img src="https://forum.duniter.org/uploads/default/original/1X/b7c419b33a43f6a43c5b756074ee0c199072f7d1.png" width="40" height="44">) pour lancer l'application en mode debug.

Android Studio vous demandera sur quel appareil vous souhaitez lancer l'application, sélectionner l'émulateur ou le téléphone et laissez faire. 

<h2>Niveau VI : À vous</h2>

Pour essayer vous pouvez essayer de modifier la page des règle de la monnaie pour quelle ressemble à ça :

<img src="https://forum.duniter.org/uploads/default/original/1X/e4a5ffe88d16b5e54ea43c4de88a6251f27a8240.png" width="248" height="500">

Pour cela vous aurez besoin de trouvez deux fichier:

Le controler : RulesFragment.java
La view : fragment_currency_rules.xml

Vous aurez besoin de la méthode :

> SqlService.getBlockSql(getActivity()).last(currency.getId());

Qui sert à récupérer dans la base de donnée le dernier object Block qui possède un Dividende.

> BlockService.getCurrentBlock(getActivity(), currency, new CallbackBlock() {
>             @Override
>             public void methode(BlockUd blockUd) {
>
>             }
>         });

Qui sert à récupérer dans la blockchain le block courant.
<p>Bonne chance !</p>

Pour les tricheurs ;) : [correction](https://github.com/duniter/duniter-android-app/blob/master/Exercice/exerch) à appliquer.