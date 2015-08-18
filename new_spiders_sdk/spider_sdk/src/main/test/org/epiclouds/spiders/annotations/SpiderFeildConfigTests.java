package org.epiclouds.spiders.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;


/**
 * 
 * @author xianglong
 * @created 2015年6月4日 下午4:33:21
 * @version 1.0
 */
public class SpiderFeildConfigTests extends AbstractConfigTests{ 
	
	@SpiderFeildConfig(desc="名称！")
	private String name22;
	
	@Test
	public static void getAnnotations() throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException{
		Class<SpiderFeildConfigTests> clazz=SpiderFeildConfigTests.class;
		Field[] fs=clazz.getFields();
		SpiderFeildConfigTests sss=clazz.newInstance();
		for(Field f:fs){
			System.err.println(f.getName());
			System.err.println(f.getAnnotation(SpiderFeildConfig.class));
			f.set(sss, "222");
		}
		
		
		Class<AbstractConfigTests> clazz2=AbstractConfigTests.class;
		fs=clazz2.getDeclaredFields();
		AbstractConfigTests ttt=clazz2.cast(sss);
		for(Field f:fs){
			System.err.println(f.getName());
			System.err.println(f.getAnnotation(SpiderFeildConfig.class));
			
		}
		Method mm=clazz2.getMethod("getName11");
		System.err.println(mm.invoke(sss));
		// 获取字段注解
		//Annotation[] as=clazz.getAnnotationsByType(SpiderFeildConfig.class);
		
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException{
		getAnnotations();
	}
}
