package io.ucoin.app.model;


import io.ucoin.app.model.http_api.UdHistory;

public interface UcoinUds extends SqlTable, Iterable<UcoinUd> {
    UcoinUd add(UdHistory.Ud ud);

    UcoinUd getById(Long id);
}