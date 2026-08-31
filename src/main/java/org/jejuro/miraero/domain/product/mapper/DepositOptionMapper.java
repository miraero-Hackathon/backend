package org.jejuro.miraero.domain.product.mapper;

import org.jejuro.miraero.domain.product.domain.DepositOption;

public interface DepositOptionMapper {

    int upsert(DepositOption depositOption);
}
