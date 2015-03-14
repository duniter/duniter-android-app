TODO - in french, sorry ;/

V0.3
~~~~

 - Sign
    * après le sign, retour à identity fragment

 - Service :
    * revoir le loadMembership pour exploiter le lookup/isMember

 - Contact :
    * Pouvoir ajouter un contact depuis une fiche identity
    * Pouvoir ajouter un contact depuis un bouton add (depuis home)

 -  Wallet :
    * add a "renew" if member but expired

 - Transfer :
    * après transfer retour à identity fragment
    * après transfer refermer keyboard

- sur les Wallets (home) :
    * gérer le rafraichissement du solde, en tache de fond à l'ouverture et après un transfert
     => fait, mais il manque un délai avant de recommencer la mise à jour. et un bouton pour le déclencher
    * il faudra un "credit" et un "expected_credit" calculé localement

V0.4
~~~~

 - Cache :
    * ajouter une cache dans : ServiceLocator.instance().getBlockchainRemoteService().getParameters()
    * idem pour getLastUD()

 -  Wallet :
    * WOT : on scroll down: reduce header

 - Identity :
    * bouton favori
    * WOT : on scroll down: reduce header

 - WOT community
    * trier par date de signature (DESC)
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