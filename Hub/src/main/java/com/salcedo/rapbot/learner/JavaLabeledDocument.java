package com.salcedo.rapbot.learner;

import java.io.Serializable;

/**
 * Labeled instance type, Spark SQL can infer schema from Java Beans.
 */
public class JavaLabeledDocument extends JavaDocument implements Serializable {
    private final double label;

    JavaLabeledDocument(final long id, final String text, final double label) {
        super(id, text);
        this.label = label;
    }

    public double getLabel() {
        return this.label;
    }
}
