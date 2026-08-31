package org.jejuro.miraero.domain.transaction.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpenseCategory {
    FOOD("식비"),
    CAFE("카페"),
    TRANSPORTATION("교통"),
    SHOPPING("쇼핑"),
    CULTURE("문화"),
    MEDICAL("의료"),
    ETC("기타");

    private final  String displayName;
}
