package com.jaqxues.modulepackcompilerui.utils;

import javafx.util.Pair;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 28.10.2018 - Time 00:11.
 */

public class BooleanPair<T> extends Pair<T, Boolean> implements RowCellFactory.ActiveStateManager {
    /**
     * Creates a new pair
     *
     * @param key   The key for this pair
     * @param value The value to use for this pair
     */
    public BooleanPair(T key, Boolean value) {
        super(key, value);
    }

    @Override
    public boolean active() {
        return getValue();
    }

    /**
     * Override for RowCellFactory
     *
     * @return The key (the String) of the BooleanPair
     */
    @Override
    public String toString() {
        return getKey().toString();
    }
}
