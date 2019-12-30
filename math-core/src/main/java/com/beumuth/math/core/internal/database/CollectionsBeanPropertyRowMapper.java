package com.beumuth.math.core.internal.database;

import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

public class CollectionsBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

    public CollectionsBeanPropertyRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public static <T> CollectionsBeanPropertyRowMapper<T> newInstance(Class<T> clazz) {
        return new CollectionsBeanPropertyRowMapper<>(clazz);
    }

    @Override
    protected void initBeanWrapper(BeanWrapper beanWrapper) {
        beanWrapper.registerCustomEditor(DateTime.class, new JodaDateTimeEditor());
    }
}