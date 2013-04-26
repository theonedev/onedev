package com.pmease.commons.product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class should only be used to launch the application from within IDE. 
 * To launch from command line, call Bootstrap instead.
 * @author robin
 *
 */
public class Launcher {

	private static Object readObject(File file) {
    	ObjectInputStream ois = null;
    	try {
    		ois = new ObjectInputStream(new FileInputStream(file));
    		return ois.readObject();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
    	}
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		File sandboxDir = new File("target/sandbox");
		if (sandboxDir.exists()) {
			Map<String, File> systemClasspath = (Map<String, File>) readObject(
					new File(sandboxDir, "bin/system.classpath"));
			Set<String> bootstrapKeys = (Set<String>) readObject(
					new File(sandboxDir, "bin/bootstrap.keys"));
			
			List<URL> urls = new ArrayList<URL>();
			for (String key: bootstrapKeys) {				
				File file = systemClasspath.get(key);
				if (file == null) 
					throw new RuntimeException("Unable to find bootstrap file for '" + key + "'.");
				try {
					urls.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			
			URLClassLoader bootClassLoader = new URLClassLoader(
					urls.toArray(new URL[0]), 
					Launcher.class.getClassLoader().getParent());
			Thread.currentThread().setContextClassLoader(bootClassLoader);
			
			try {
				Class<?> bootstrapClass = bootClassLoader.loadClass("com.pmease.commons.bootstrap.Bootstrap");
				bootstrapClass.getMethod("main", String[].class).invoke(null, new Object[]{args});
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
			
		} else {
			throw new RuntimeException("Unable to find sandbox directory: " + sandboxDir);
		}
	}

}
