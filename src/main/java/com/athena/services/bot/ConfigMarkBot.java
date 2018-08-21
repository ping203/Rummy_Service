package com.athena.services.bot;

public class ConfigMarkBot {

    int mark;
    int type;
    float countPercent;
    int count;
    int countPlus;
    
    public ConfigMarkBot(int mark, int type, float countPercent) {
        this.mark = mark;
        this.type = type;
        this.countPercent = countPercent;
        this.count = 0;
        this.countPlus = 0;
    }
    
    public int getMark() {
        return mark;
    }
    public void setMark(int mark) {
        this.mark = mark;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public float getCountPercent() {
        return countPercent;
    }
    public void setCountPercent(float countPercent) {
        this.countPercent = countPercent;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public int getCountPlus() {
        return countPlus;
    }
    public void setCountPlus(int countPlus) {
        this.countPlus = countPlus;
    }
    
    public void reset() {
        this.count = 0;
        this.countPlus = 0;
    }
}
