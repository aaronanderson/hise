/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.hise.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a place holder annotation that will not be packaged in the final
 * distribution. It is expected that this annotation will be annotated with
 * container specific metadata indicating that any class or method annotated
 * with this annotation should be be invoked in a transactional context. In
 * Spring this would involve adding the @Transactional Spring annotation below
 *
 *
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

}
