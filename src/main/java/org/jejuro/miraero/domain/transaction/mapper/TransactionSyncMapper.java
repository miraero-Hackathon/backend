package org.jejuro.miraero.domain.transaction.mapper;

import org.jejuro.miraero.domain.transaction.domain.TransactionSyncCommand;

public interface TransactionSyncMapper {

  int upsert(TransactionSyncCommand command);
}
