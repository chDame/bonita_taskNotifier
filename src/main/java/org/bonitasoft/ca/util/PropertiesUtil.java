package org.bonitasoft.ca.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesUtil {

	private PropertiesUtil() {
		
	}
	
	private static Properties prop = null;
	
	private static synchronized Properties getProperties() {
		if (prop!=null) {
			return prop;
		}
		
		/*URL propUrl = PropertiesUtil.class.getClassLoader().getResource("conf/TaskNotifier.properties");
		if (propUrl == null) {
			throw new IllegalArgumentException("TaskNotifier properties file : conf/TaskNotifier.properties doesnt exist");
		}*/
		File propFile = Paths.get("conf/TaskNotifier.properties").toAbsolutePath().toFile();
		if (!propFile.exists()) {
			throw new IllegalArgumentException("TaskNotifier properties file : "+propFile.getAbsolutePath()+" doesnt exist");
		}
		prop = new Properties();
		try {
			InputStream is = new FileInputStream(propFile.getPath());
			prop.load(is);
			is.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("TaskNotifier properties file : "+propFile.getPath()+" is not readable");
		}
		return prop;
	}
	
	public static String getProperty(String propertyKey) {
		return getProperties().getProperty(propertyKey);
	}
}
