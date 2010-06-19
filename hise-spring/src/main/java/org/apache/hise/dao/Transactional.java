/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.hise.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@org.springframework.transaction.annotation.Transactional
public @interface Transactional {

}
