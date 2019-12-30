package com.beumuth.math.core.internal.database;

import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

public class MathBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

    public MathBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public static <T> MathBeanPropertyRowMapper<T> newInstance(Class<T> clazz) {
        return new MathBeanPropertyRowMapper<>(clazz);
    }

    @Override
    protected void initBeanWrapper(BeanWrapper beanWrapper) {
        beanWrapper.registerCustomEditor(DateTime.class, new JodaDateTimeEditor());
    }
}