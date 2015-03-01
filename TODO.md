TODO - in french, sorry ;/

 - WOT :
  * bouton favori

 - WOT : sign
   * après le sign, retour à identity fragment

 - Contact :
   * Pouvoir ajouter un contact

   - Transfer :
    - wallet spinner : le montant est caché
    - ajouter une option de fragmen pour avoir choisir le destinataire (par spinner ou pubkey)
    - après le transfer retour à identity fragment

- sur les Wallets (home) :
   * gérer le rafraichissement du solde, en tache de fond à l'ouverture et après un transfert
     - fait, mais il manque un délai avant de recommencer la mise à jour. et un bouton pour le déclencher
     - il faudrai un progress bar pendant la rafraichissement
   * il faudra un "credit" et un "expected_credit" calculé localement
   * Mettre une icon différente pour les Wallet non member

- gérer les transactions :
    * ajouter les transactions en attente après un transfert,
    * pour rafraichir la liste à partir des noeuds
    * pourvoir renvoyer une transaction qui' n'est pas passée

- revoir les transitions entre fragment, notamment :
  * dans l'assistance de création de compte
  * au niveau des transfer, quand un login est nécessaire

- Login :
   * Ajouter une case à cocher "se rappeller de moi pendant XXX minutes" (XXX dans les settings)