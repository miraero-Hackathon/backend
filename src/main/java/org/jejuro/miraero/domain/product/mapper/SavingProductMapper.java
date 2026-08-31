package org.jejuro.miraero.domain.product.mapper;

import java.util.List;
import org.jejuro.miraero.domain.product.domain.SavingProduct;
import org.jejuro.miraero.domain.product.domain.SavingOption;
import org.jejuro.miraero.domain.product.domain.SavingProductDetailQueryResult;
import org.jejuro.miraero.domain.product.domain.SavingProductListQueryResult;
import org.apache.ibatis.annotations.Param;

public interface SavingProductMapper {

    int upsert(SavingProduct savingProduct);

    List<SavingProductListQueryResult> findSavingProductList();

    SavingProductDetailQueryResult findSavingProductDetailById(
            @Param("savingProductId") Long savingProductId
    );

    List<SavingOption> findSavingOptionsBySavingProductId(
            @Param("savingProductId") Long savingProductId
    );
}
