package com.pmease.commons.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.base.Preconditions;

public class ClassUtils extends org.apache.commons.lang3.ClassUtils {
	
	/**
	 * Find all sub classes inheriting from (or implementing) specified super class (or interface) 
	 * recursively in package containing the package class.
	 * 
	 * @param superClass 
	 * 			super class (or interface) to match. 
	 * @param packageLocator 
	 * 			find sub classes in the same package as this class. Package will be searched recursively.
	 * @return collection of sub classes (not include the super class)
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<Class<? extends T>> findSubClasses(Class<T> superClass, Class<?> packageLocator) {
		Preconditions.checkNotNull(superClass);
		Preconditions.checkNotNull(packageLocator);
		
		Collection<Class<? extends T>> classes = new HashSet<Class<? extends T>>();
		
		File location = new File(packageLocator.getProtectionDomain()
				.getCodeSource().getLocation().getFile());
		if (location.isFile()) {
			String packagePath = packageLocator.getPackage().getName().replace('.', '/') + "/";
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(location);
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.getName().startsWith(packagePath) && entry.getName().endsWith(".class")) {
						String className = entry.getName().replace('/', '.');
						className = StringUtils.substringBeforeLast(className, ".");
						Class<T> clazz;
						try {
							clazz = (Class<T>) superClass.getClassLoader().loadClass(className);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
						if (superClass.isAssignableFrom(clazz) && clazz != superClass)
							classes.add(clazz);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (jarFile != null) {
					try {
						jarFile.close();
					} catch (IOException e) {
					}
				}
			}
		} else {
			String packagePath = packageLocator.getPackage().getName().replace('.', File.separatorChar);
			File packageDir = new File(location, packagePath);
			if (packageDir.exists()) {
				for (File file: FileUtils.listFiles(packageDir, "**/*.class")) {
					Class<T> clazz;
					try {
						String relativePath = PathUtils.parseRelative(file.getAbsolutePath(), 
								packageDir.getAbsolutePath());
						String className = packageLocator.getPackage().getName() + 
								StringUtils.substringBeforeLast(relativePath.replace('/', '.'), ".");
						ClassLoader classLoader = superClass.getClassLoader();
						if (classLoader == null)
							classLoader = packageLocator.getClassLoader();
						Preconditions.checkNotNull(classLoader);
						clazz = (Class<T>) classLoader.loadClass(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
					if (superClass.isAssignableFrom(clazz) && clazz != superClass)
						classes.add(clazz);
				}
			} else {
				throw new IllegalStateException("Unable to find package directory: " + 
						packageDir.getAbsolutePath());
			}
		}
		return classes;
	}
	
	public static boolean isConcrete(Class<?> clazz) {
		return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());		
	}
	
	public static <T> Collection<Class<? extends T>> findImplementations(Class<T> abstractClass, Class<?> packageLocator) {
		Collection<Class<? extends T>> implementations = new HashSet<Class<? extends T>>();
		
		for (Class<? extends T> each: findSubClasses(abstractClass, packageLocator)) {
			if (isConcrete(each))
				implementations.add(each);
		}
		return implementations;
	}
	
}
