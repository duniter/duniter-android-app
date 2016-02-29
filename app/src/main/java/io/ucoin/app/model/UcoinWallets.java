package io.ucoin.app.model;


import android.database.Cursor;

import java.util.ArrayList;

public interface UcoinWallets extends SqlTable, Iterable<UcoinWallet> {
    UcoinWallet add(String salt, String publicKey, String alias);

    UcoinWallet add(String salt, String alias, String publicKey, String privateKey);

    UcoinWallet getById(Long id);

    UcoinWallet getByPublicKey(String publicKey);

    Cursor getbyCurrency();

    ArrayList<UcoinWallet> list();
    String[] listPublicKey();
}