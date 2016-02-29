package io.ucoin.app.model;

import io.ucoin.app.model.http_api.BlockchainBlock;

public interface UcoinBlocks extends SqlTable, Iterable<UcoinBlock> {
    UcoinBlock add(BlockchainBlock blockchainBlock);

    UcoinBlock getById(Long id);

    UcoinBlock getByNumber(Long number);

    UcoinBlock lastUdBlock();

    UcoinBlock currentBlock();
}
