package com.watchyojet;

import com.watchyojet.model.Aircraft;
import com.watchyojet.simulation.MovementEngine;

public class Main {
    public static void main(String[] args) {

        Aircraft a = new Aircraft("A1", 0, 0, 30000, 800, 45);

        MovementEngine engine = new MovementEngine();
        engine.update(a);

        System.out.println("New position: " + a.getLat() + ", " + a.getLon());
    }
}