package com.github.commerce.web.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;


public enum PaymentMethodEnum {

    PAY_MONEY(1, "페이머니"),
    CREDIT_CARD(2, "신용카드");

    private final int value;
    private final String label;

    PaymentMethodEnum(int value, String label){
        this.value = value;
        this.label = label;
    }

    public static String getByCode(int code){
        switch(code){

            case 1: return PAY_MONEY.label;
            case 2: return CREDIT_CARD.label;

            default: return PAY_MONEY.label;
        }
    }
}
