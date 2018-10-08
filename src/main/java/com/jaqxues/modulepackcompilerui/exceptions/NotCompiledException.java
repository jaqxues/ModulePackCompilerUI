package com.jaqxues.modulepackcompilerui.exceptions;

/**
 * This file was created by Jacques (jaqxues) in the Project ModulePackCompilerUI.<br>
 * Date: 07.10.2018 - Time 18:09.
 */

public class NotCompiledException extends Exception {
    public NotCompiledException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotCompiledException(String message) {
        super(message);
    }
}
