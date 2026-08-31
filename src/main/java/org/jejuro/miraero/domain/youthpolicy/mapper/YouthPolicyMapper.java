package org.jejuro.miraero.domain.youthpolicy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyDetailQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyListQueryResult;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicy;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyRegion;

@Mapper
public interface YouthPolicyMapper {

    int upsert(YouthPolicy youthPolicy);

    Long findYouthPolicyIdByPolicyNo(@Param("policyNo") String policyNo);

    int deleteRegionsByYouthPolicyId(@Param("youthPolicyId") Long youthPolicyId);

    int insertRegions(
            @Param("youthPolicyId") Long youthPolicyId,
            @Param("regions") List<YouthPolicyRegion> regions
    );

    List<YouthPolicyListQueryResult> findYouthPolicies(
            @Param("keyword") String keyword,
            @Param("regionCode") String regionCode,
            @Param("search") String search,
            @Param("offset") Long offset,
            @Param("size") Integer size
    );

    long countYouthPolicies(
            @Param("keyword") String keyword,
            @Param("regionCode") String regionCode,
            @Param("search") String search
    );

    List<YouthPolicyListQueryResult> findRecommendedYouthPolicies(
            @Param("age") Integer age,
            @Param("monthlyIncome") Long monthlyIncome,
            @Param("regionCode") String regionCode,
            @Param("size") Integer size
    );

    long countRecommendedYouthPolicies(
            @Param("age") Integer age,
            @Param("monthlyIncome") Long monthlyIncome,
            @Param("regionCode") String regionCode
    );

    YouthPolicyDetailQueryResult findYouthPolicyById(
            @Param("youthPolicyId") Long youthPolicyId
    );
}
