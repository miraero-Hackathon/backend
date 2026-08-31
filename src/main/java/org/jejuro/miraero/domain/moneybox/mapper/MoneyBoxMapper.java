package org.jejuro.miraero.domain.moneybox.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;

import java.util.List;

@Mapper
public interface MoneyBoxMapper {

  void insert(MoneyBox moneyBox);

  int increaseBalance(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("amount") Long amount
  );

  int decreaseBalance(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("userId") Long userId,
      @Param("amount") Long amount
  );

  Long findBalanceById(@Param("moneyBoxId") Long moneyBoxId);

  // 계좌 잔액에서 빼야 할 금액. 저금통 돈은 계좌 안에 있지만 쓸 수 없다.
  Long sumBalanceByAccountId(@Param("accountId") Long accountId);

  boolean existsByIdAndUserId(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("userId") Long userId
  );

  MoneyBox findById(@Param("moneyBoxId") Long moneyBoxId);

  List<MoneyBox> findByIds(
          @Param("moneyBoxIds") List<Long> moneyBoxIds,
          @Param("userId") Long userId
  );

  MoneyBox findPaceMakerMoneyBoxByIdAndUserIdForUpdate(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("userId") Long userId
  );

  void deleteById(@Param("moneyBoxId") Long moneyBoxId);
}
