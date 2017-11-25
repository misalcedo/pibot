package com.salcedo.rapbot.learner;

import java.io.Serializable;

/**
 * Unlabeled instance type, Spark SQL can infer schema from Java Beans.
 */
public class JavaDocument implements Serializable {
    private final long id;
    private final String text;

    JavaDocument(final long id, final String text) {
        this.id = id;
        this.text = text;
    }

    public long getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }
}