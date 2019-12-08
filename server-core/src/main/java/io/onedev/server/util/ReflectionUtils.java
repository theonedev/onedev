package io.onedev.server.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExceptionUtils;

public class ReflectionUtils {
	
	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class.<p>
	 * (Taken from http://www.artima.com/weblogs/viewpost.jsp?thread=208860. 
	 * Thanks mathieu.grenonville for finding this solution!)
	 * <p>
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
	 * Get the underlying class for a type.
	 * <p>
	 * @param type
	 *          the type to get class from
	 * @return 
	 * 			the underlying class, or <tt>null</tt> if the type is a variable type
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

	/**
	 * Find method of specified name and parameter types in specified class and its super classes 
	 * in turn. 
	 * <p>
	 * @param clazz
	 * 			class to find method in. Super classes will be searched for the method if not 
	 * 			found in current class
	 * @param methodName
	 * 			method name to find
	 * @param paramTypes 
	 * 			parameter types to find
	 * @return
	 * 			method matching specified name and parameter types. Or <tt>null</tt> if not found
	 */
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Class<?> current = clazz;
		while (true) {
			try {
				return current.getDeclaredMethod(methodName, paramTypes);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				// ignore
			}
			if (current == Object.class)
				break;
			current = current.getSuperclass();
		}
		
		return null;
	}
	
	/**
	 * Find non-static field in specified class hierarchy with specified name.
	 * <p>
	 * @param clazz
	 * 			class to search non-static field in. All the super classes will also 
	 * 			be consulted if not found in sub class
	 * @param fieldName
	 * 			name of the field to search for
	 * @return 
	 * 			found non-static field, or <tt>null</tt> if not found
	 */
	public static Field findField(Class<?> clazz, String fieldName) {
		Field field = null;
		Class<?> current = clazz;
		while (current != null) {
			for (Field declaredField: current.getDeclaredFields()) {
				if (!Modifier.isStatic(declaredField.getModifiers()) 
						&& fieldName.equals(declaredField.getName())) {
					field = declaredField;
					break;
				}
			}
			current = current.getSuperclass();
		}
		return field;
	}

	/**
	 * Find all non-static fields in specified class and all its super classes. 
	 * If a non-static field declared in sub class has the same name as another 
	 * non-static field declared in super class, only the non-static field in 
	 * sub class will be returned.
	 * <p>
	 * @param clazz
	 * 			the class to find non-static fields. All its super classes will 
	 * 			also be consulted
	 * @return
	 * 			list of found non-static fields, with non-static fields belonging 
	 * 			to sub classes coming first   
	 */
	public static List<Field> findFields(Class<?> clazz) {
		Map<String, Field> fields = new LinkedHashMap<String, Field>();

		Class<?> current = clazz;
		while (current != null) {
			for (Field field: current.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) 
						&& !field.isSynthetic()
						&& !fields.containsKey(field.getName())) {
					fields.put(field.getName(), field);
				}
			}
			current = current.getSuperclass();
		}
		return new ArrayList<Field>(fields.values());
	}

	/**
	 * Get declared method in specified class and super classes.
	 * <p>
	 * @param clazz
	 * 			clazz to search the method. If not found, its super classes 
	 * 			will also be searched
	 * @param methodName
	 * 			name of the method to find
	 * @param paramTypes
	 * 			parameter types of the method to find
	 * @return
	 * 			method matching specified name and parameter types 
	 * @throws 
	 * 			RuntimeException if the method can not be found
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>...paramTypes) {
		Method method = findMethod(clazz, methodName, paramTypes);
		if (method == null) {
			throw new RuntimeException("Can not find method named '" + 
					methodName + "' in class hierarchy of '" + clazz.getName() + 
					"'.");
		} else {
			return method;
		}
	}

	/**
	 * Invoke specified static method with specified parameters. Parameter types 
	 * of invoked static method should be compatible with types of specified 
	 * parameters. The static method will be searched in specified class and all 
	 * its super classes in turn. 
	 * <p>
	 * @param clazz
	 * 			class to invoke static method in. Static method will be searched 
	 * 			in super classes if not found in specified class 
	 * @param methodName
	 * 			name of the static method
	 * @param params
	 * 			parameters to be passed to the static method
	 * @return
	 * 			return value of invoked static method
	 * @throws
	 * 			RuntimeException if no static method matching specified name and parameters
	 */
	public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object...params) {
		Class<?>[] paramTypes = new Class<?>[params.length];
		
		for (int i = 0; i < params.length; i++) 
			paramTypes[i] = params[i].getClass();
		
		Method method = getMethod(clazz, methodName, paramTypes);
	    if (!Modifier.isStatic(method.getModifiers())) {
	        throw new RuntimeException("Method '" + methodName + "' should be a static method.");
	    }
	    method.setAccessible(true);
        try {
			return method.invoke(null, params);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	/**
	 * Invoke specified method with specified parameters. Parameter types 
	 * of invoked method should be compatible with types of specified 
	 * parameters. The method will be searched in specified class and all 
	 * its super classes in turn. 
	 * <p>
	 * @param clazz
	 * 			class to invoke method in. Method will be searched 
	 * 			in super classes if not found in specified class 
	 * @param methodName
	 * 			name of the method
	 * @param params
	 * 			parameters to be passed to the method
	 * @return
	 * 			return value of invoked method
	 * @throws
	 * 			RuntimeException if no method matching specified name and parameters
	 */
	public static Object invokeMethod(Object object, String methodName, Object...params) {
		Class<?>[] paramTypes = new Class<?>[params.length];
		
		for (int i = 0; i < params.length; i++) 
			paramTypes[i] = params[i].getClass();
		
		Method method = getMethod(object.getClass(), methodName, paramTypes);
	    if (Modifier.isStatic(method.getModifiers())) {
	        throw new RuntimeException("Method '" + methodName + "' should not be a static method.");
	    }
	    method.setAccessible(true);
        try {
			return method.invoke(object, params);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	/**
	 * Instantiate specified class with specified parameters. The constructor 
	 * used for instantiation should match types of specified parameters.
	 * <p>
	 * @param clazz
	 * 			class to be instantiated
	 * @param params 
	 * 			parameters passed to constructor 
	 * @return
	 * 			instance of the class
	 * @throws
	 * 			RuntimeException if constructor with parameter types
	 * 			matching specified parameters can not be found
	 */
	public static <T> T instantiateClass(Class<T> clazz, Object... params) {
		Class<?>[] paramTypes = new Class<?>[params.length];
		
		for (int i = 0; i < params.length; i++) 
			paramTypes[i] = params[i].getClass();
		
		Constructor<T> constructor = findConstructor(clazz, paramTypes);
		
		if (constructor == null) {
			StringBuilder sb = new StringBuilder();
			
			if (paramTypes.length > 0) {
				for (Class<?> type : paramTypes) 
					sb.append(type).append(", ");
			}
			
			throw new RuntimeException("Unable to find constructor for " + clazz + 
					" with param type: " + sb.toString());
		}
		
		try {
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	/**
	 * Find constructor of specified parameter types in specified class. 
	 * <p>
	 * @param clazz
	 * 			class to find constructor in
	 * @param expectedParamTypes
	 * 			expected parameter types of the constructor
	 * @return
	 * 			matched constructor, or <tt>null</tt> if not found
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... expectedParamTypes) {
		if (expectedParamTypes.length == 0) {
			try {
				return clazz.getConstructor();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		
		Constructor<?>[] constructors = clazz.getConstructors();
		
		for (Constructor<?> constructor : constructors) {
			Class<?>[] paramTypes = constructor.getParameterTypes();
			if (paramTypes.length != expectedParamTypes.length) 
				continue;
			
			boolean found = true;
			for (int i = 0; i < paramTypes.length; i++) {
				if (!paramTypes[i].isAssignableFrom(expectedParamTypes[i])) {
					found = false;
					break;
				}
			}
			
			if (found) 
				return (Constructor<T>) constructor;
		}
		
		return null;
	}
	
	public static Class<?> getCollectionElementType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type rawType = parameterizedType.getRawType();
			if (rawType instanceof Class<?>) {
				Class<?> rawClazz = (Class<?>) rawType;
				if (Collection.class.isAssignableFrom(rawClazz)) {
					Type elementType = parameterizedType.getActualTypeArguments()[0];
					if (elementType instanceof Class<?>) 
						return (Class<?>) elementType;
				}
			}
		}
		return null;
	}
	
	public static Class<?> getMapKeyType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type rawType = parameterizedType.getRawType();

			if (rawType instanceof Class<?>) {
				Class<?> rawClazz = (Class<?>) rawType;
				if (Map.class.isAssignableFrom(rawClazz)) {
					Type valueType = parameterizedType.getActualTypeArguments()[0];
					if (valueType instanceof Class<?>) 
						return (Class<?>) valueType;
				}
			}
		}
		return null;
	}

	public static Class<?> getMapValueType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type rawType = parameterizedType.getRawType();

			if (rawType instanceof Class<?>) {
				Class<?> rawClazz = (Class<?>) rawType;
				if (Map.class.isAssignableFrom(rawClazz)) {
					Type valueType = parameterizedType.getActualTypeArguments()[1];
					if (valueType instanceof Class<?>) {
						return (Class<?>) valueType;
					} else if (valueType instanceof ParameterizedType) {
						return (Class<?>) ((ParameterizedType)valueType).getRawType();
					}
				}
			}
		}
		return null;
	}
	
}
