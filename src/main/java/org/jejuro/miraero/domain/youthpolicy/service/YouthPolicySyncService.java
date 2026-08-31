package org.jejuro.miraero.domain.youthpolicy.service;

import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiItem;

public interface YouthPolicySyncService {

    void syncYouthPolicy(YouthPolicyApiItem source);

    void syncYouthPolicies();
}
