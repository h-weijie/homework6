package com.byted.camp.todolist.beans;

public enum Prority {
    LOW(0),MID(1),HIGHT(2);

    public final int intValue;

    Prority(int intValue) {
        this.intValue = intValue;
    }

    public static Prority from(int intValue) {
        for (Prority prority : Prority.values()) {
            if (prority.intValue == intValue) {
                return prority;
            }
        }
        return LOW; // default
    }
}
