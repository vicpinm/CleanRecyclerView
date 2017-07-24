package com.vicpinm.testinjector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Created by Oesia on 28/11/2016.
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectMe {
}
