package com.beumuth.math.core.internal.database;

import org.joda.time.DateTime;

import java.beans.PropertyEditorSupport;

//Taken from https://stackoverflow.com/a/7742122/3816779
public class JodaDateTimeEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(new DateTime(text)); // date time in ISO8601 format
        // (yyyy-MM-ddTHH:mm:ss.SSSZZ)
    }
    @Override
    public void setValue(final Object value) {
        super.setValue(value == null || value instanceof DateTime ? value
            : new DateTime(value));
    }
    @Override
    public DateTime getValue() {
        return (DateTime) super.getValue();
    }
    @Override
    public String getAsText() {
        return getValue().toString(); // date time in ISO8601 format
        // (yyyy-MM-ddTHH:mm:ss.SSSZZ)
    }
}
