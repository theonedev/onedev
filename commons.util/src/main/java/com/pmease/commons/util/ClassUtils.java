package com.pmease.commons.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.base.Preconditions;

public class ClassUtils extends org.apache.commons.lang3.ClassUtils {
	
	/**
	 * Find all sub classes inheriting from (or implementing) specified super class (or interface) in 
	 * package containing the package class.
	 * 
	 * @param superClass 
	 * 			super class (or interface) to match. 
	 * @param packageClass 
	 * 			find sub classes in the same package as this class. Package will not be searched recursively.
	 * @return collection of sub classes (not include the super class)
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<Class<T>> findSubClasses(Class<T> superClass, Class<?> packageClass) {
		Preconditions.checkNotNull(superClass);
		Preconditions.checkNotNull(packageClass);
		
		Collection<Class<T>> classes = new HashSet<Class<T>>();
		
		File location = new File(packageClass.getProtectionDomain()
				.getCodeSource().getLocation().getFile());
		if (location.isFile()) {
			String packagePath = packageClass.getPackage().getName().replace('.', '/') + "/";
			JarFile jarFile;
			try {
				jarFile = new JarFile(location);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().startsWith(packagePath) 
						&& entry.getName().substring(packagePath.length()).indexOf('/') == -1 
						&& entry.getName().endsWith(".class")) {
					
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
		} else {
			String packagePath = packageClass.getPackage().getName().replace('.', File.separatorChar);
			File packageDir = new File(location, packagePath);
			if (packageDir.exists()) {
				for (File file: packageDir.listFiles()) {
					if (file.getName().endsWith(".class")) {
						Class<T> clazz;
						try {
							String className = packageClass.getPackage().getName() + "." + 
									StringUtils.substringBeforeLast(file.getName(), ".");
							clazz = (Class<T>) superClass.getClassLoader().loadClass(className);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
						if (superClass.isAssignableFrom(clazz) && clazz != superClass)
							classes.add(clazz);
					}
				}
			} else {
				throw new IllegalStateException("Unable to find package directory: " + 
						packageDir.getAbsolutePath());
			}
		}
		return classes;
	}
	
	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class. (Taken from http://www.artima.com/weblogs/viewpost.jsp?thread=208860. Thanks
	 * mathieu.grenonville for finding this solution!)
	 * 
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
		Preconditions.checkArgument(!baseClass.isInterface(), "baseClass should not be an interface");
		Preconditions.checkArgument(baseClass.isAssignableFrom(childClass), "childClass should be a sub class of baseClass");
		
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (!getClass(type).equals(baseClass)) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				type = ((Class<?>) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType
						.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes
							.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) {
			actualTypeArguments = ((Class<?>) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type)
					.getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}
	
	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 * 
	 * @param type
	 *            the type
	 * @return the underlying class
	 */
	private static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type)
					.getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
}
