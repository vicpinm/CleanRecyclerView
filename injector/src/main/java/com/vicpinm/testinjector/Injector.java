package com.vicpinm.testinjector;

import org.mockito.InjectMocks;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Oesia on 28/11/2016.
 */

public class Injector {

    /**
     * Finds a field with the @InjectMocks annotation from Mockito, and injects on this class all the
     * fields annotated with @InjectMe from the class passed as parameter to this method
     * @param fromClass
     */
    public static void injectWith(Object fromClass){
        try {
            Field targetClassField = findFieldToInjectWithAnnotation(fromClass.getClass(), InjectMocks.class);
            if(targetClassField != null) {
                Object targetClass = getField(fromClass, targetClassField);
                Set<Field> toInject = findFieldsToInject(fromClass.getClass());
                if (toInject.size() > 0) {
                    for (Field f : toInject) {
                        Field targetField = findFieldWithType(targetClass, f.getType());
                        setField(targetClass, targetField, getField(fromClass, f));
                    }
                }
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Finds fields annotated with @InjectMe annotation from class passed as first parameter,
     * and inject this field to the class passed as second parameter
     */
    public static void injectTo(Object fromClass, Object toClass){
        try {
            Set<Field> toInject = findFieldsToInject(fromClass.getClass());
            if (toInject.size() > 0) {
                for (Field f : toInject) {
                    Field targetField = findFieldWithType(toClass, f.getType());
                    setField(toClass, targetField, getField(fromClass,f));
                }
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static Field findFieldToInjectWithAnnotation(Class c, Class anotation) {
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(anotation)) {
                    return field;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }


    private static Set<Field> findFieldsToInject(Class c) {
        Set<Field> set = new HashSet<>();
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(InjectMe.class)) {
                    set.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return set;
    }

    private static Field findFieldWithType(Object targetClass, Type type) {
        Class c = targetClass.getClass();
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.getType() == type){
                    return field;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private static void setField(Object targetClass, Field field, Object value){
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(targetClass, value);
            field.setAccessible(accessible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object getField(Object targetClass, Field field){
        Object value = null;
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            value = field.get(targetClass);
            field.setAccessible(accessible);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


}
