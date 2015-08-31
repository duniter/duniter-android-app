TODO - in french, sorry ;/

Build
~~~~~

 - Change to the new NDK build (need gradle 2.5) : see http://tools.android.com/tech-docs/new-build-system/gradle-experimental

V0.4
~~~~

 - Transfer :
    * après transfer refermer keyboard

 - Service :
    * revoir le loadMembership pour exploiter le lookup/isMember
    * faire que le creditUD soit mis à jour quand on fait un updateWallet dans WalletService

vérifier si tous les cursor sont fermé :
http://stackoverflow.com/questions/9801304/android-contentprovider-calls-bursts-of-setnotificationuri-to-cursoradapter-wh

 - Contact :
    * Pouvoir ajouter un contact depuis un bouton add (depuis home)

 -  Wallet :
    * add a "renew" if member but expired

 - Sign a identity again always failed (Bad Request)

 - http://www.slf4j.org/android/

 - Wot/Community : Pourqoi je ne peux pas signer pschofonni ?

 - Enlever le controle des doubles dans le traitement de /tx/history
   => En attente de la correction de : https://github.com/ucoin-io/ucoin/issues/71


~~~~

V0.5

~~~~
 - Cache :
    * ajouter une cache dans : ServiceLocator.instance().getBlockchainRemoteService().getParameters()
    * idem pour getLastUD()

 - Contact :
    * implement 'add to existing contact'

 -  Wallet :
    * WOT : on scroll down: reduce header

 - Identity :
    * bouton favori
    * WOT : on scroll down: reduce header

 - WOT community
    * trier par time de signature (DESC)
    * barrer les signatures trop vielles
    * masquer les plus anciennes, sous un bouton (afficher l'historique)

 - gérer les transactions :
    * ajouter les transactions en attente après un transfert,
    * pour rafraichir la liste à partir des noeuds
    * pouvoir renvoyer une transaction qui n'est pas passée

 - revoir les transitions entre fragment, notamment :
    * dans l'assistance de création de compte
    * au niveau des transfer, quand un login est nécessaire

 - Login :
    * Ajouter une case à cocher "se rappeller de moi pendant XXX minutes" (XXX dans les settings)


 - sur les Wallets (home) :
    * rafraichissement du solde : il manque un délai avant de recommencer la mise à jour
    * il faudra un "credit" et un "expected_credit" calculé localement

V0.5

~~~~

 - Use RecyclerView (+ refresh) instead of ListView :
   * Home/WalletList
   * Community
   * Movements
   *
~~~~

