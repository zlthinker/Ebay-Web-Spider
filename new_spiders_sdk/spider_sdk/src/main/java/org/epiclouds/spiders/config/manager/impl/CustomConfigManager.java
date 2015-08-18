package org.epiclouds.spiders.config.manager.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.epiclouds.spiders.config.field.abstracts.AbstractField;
import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.springframework.stereotype.Component;

/**
 * 
 * @author xianglong
 * @created 2015年6月2日 下午6:53:35
 * @version 1.0
 */
@Component
public class CustomConfigManager extends AbstractConfigManager{

	public CustomConfigManager() throws UnsupportedEncodingException, FileNotFoundException, IOException{
		Properties pros=new Properties();
		pros.load(new InputStreamReader(new FileInputStream("config"), "UTF-8"));

		for(Object key:pros.keySet()){
			String value=pros.getProperty((String)key);
			String[] values=value.split(" ");
			if(values!=null){
				AbstractField<?> field=AbstractField.buildFeild((String) key, values[0], values[1], 
						Boolean.parseBoolean(values[2]), Integer.parseInt(values[3]));
				this.register(field);
			}
		}
	}
	

	
	

}
