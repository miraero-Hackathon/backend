package org.jejuro.miraero.domain.product.mapper;

import java.util.List;
import org.jejuro.miraero.domain.product.domain.DepositProduct;
import org.jejuro.miraero.domain.product.domain.DepositOption;
import org.jejuro.miraero.domain.product.domain.DepositProductDetailQueryResult;
import org.jejuro.miraero.domain.product.domain.DepositProductListQueryResult;
import org.apache.ibatis.annotations.Param;

public interface DepositProductMapper {

    int upsert(DepositProduct depositProduct);

    List<DepositProductListQueryResult> findDepositProductList();

    DepositProductDetailQueryResult findDepositProductDetailById(
            @Param("depositProductId") Long depositProductId
    );

    List<DepositOption> findDepositOptionsByDepositProductId(
            @Param("depositProductId") Long depositProductId
    );
}
