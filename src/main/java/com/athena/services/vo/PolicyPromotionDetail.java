package com.athena.services.vo;

import java.io.Serializable;

public class PolicyPromotionDetail implements Serializable {

    public PolicyPromotionDetail() {

    }

    public PolicyPromotionDetail(int np, int vaP, int vaTo, int conP, int vi) {
        this.numberP = np;
        this.valueP = vaP;
        this.valueTomorrow = vaTo;
        this.conditionP = conP;
        this.vip = vi;
    }

    private int numberP;
    private int valueP;
    private int valueTomorrow;
    private int goldHour;
    private int conditionP;
    private int vip;

    public int getNumberP() {
        return numberP;
    }

    public void setNumberP(int numberP) {
        this.numberP = numberP;
    }

    public int getValueP() {
        return valueP;
    }

    public void setValueP(int valueP) {
        this.valueP = valueP;
    }

    public int getValueTomorrow() {
        return valueTomorrow;
    }

    public void setValueTomorrow(int valueTomorrow) {
        this.valueTomorrow = valueTomorrow;
    }

    public int getGoldHour() {
        return goldHour;
    }

    public void setGoldHour(int goldHour) {
        this.goldHour = goldHour;
    }

    public int getConditionP() {
        return conditionP;
    }

    public void setConditionP(int conditionP) {
        this.conditionP = conditionP;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

}
