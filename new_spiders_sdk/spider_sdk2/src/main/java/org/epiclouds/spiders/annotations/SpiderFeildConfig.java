package org.epiclouds.spiders.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author xianglong
 * @created 2015年6月4日 下午4:33:21
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target(value={ ElementType.FIELD}) 
@Inherited
@Documented
public @interface SpiderFeildConfig{ 
	/**
	 * the description of the spider object field config
	 * @return
	 */
    public String desc(); 
}
