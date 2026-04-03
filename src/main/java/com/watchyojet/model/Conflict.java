package com.watchyojet.model;

public class Conflict {

    private Aircraft a1;
    private Aircraft a2;

    public Conflict(Aircraft a1, Aircraft a2) {
        this.a1 = a1;
        this.a2 = a2;
    }

    public Aircraft getA1() { return a1; }
    public Aircraft getA2() { return a2; }
}