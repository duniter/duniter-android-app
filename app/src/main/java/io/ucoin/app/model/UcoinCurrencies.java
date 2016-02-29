package io.ucoin.app.model;

import android.database.Cursor;

import java.util.ArrayList;

import io.ucoin.app.model.http_api.BlockchainParameter;
import io.ucoin.app.model.http_api.NetworkPeering;

public interface UcoinCurrencies extends SqlTable, Iterable<UcoinCurrency> {
    UcoinCurrency add(BlockchainParameter parameter, NetworkPeering peer);

    UcoinCurrency getById(Long id);

    UcoinCurrency getByName(String name);

    Cursor getAll();

    ArrayList<UcoinCurrency> list();
}
