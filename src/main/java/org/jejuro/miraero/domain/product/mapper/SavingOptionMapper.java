package org.jejuro.miraero.domain.product.mapper;

import org.jejuro.miraero.domain.product.domain.SavingOption;

public interface SavingOptionMapper {

    int upsert(SavingOption savingOption);
}
