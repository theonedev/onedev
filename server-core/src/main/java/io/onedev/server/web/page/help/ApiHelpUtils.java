package io.onedev.server.web.page.help;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.objenesis.ObjenesisStd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.launcher.loader.ImplementationRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.ReflectionUtils;

public class ApiHelpUtils {

	public static Serializable getExampleValue(Type valueType) {
		return getExampleValue(valueType, Sets.newHashSet());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Serializable getExampleValue(Type valueType, Set<Class<?>> parsedTypes) {
		Class<?> valueClass = ReflectionUtils.getClass(valueType);
		Object value = new ExampleProvider(valueClass, valueClass.getAnnotation(Api.class)).getExample();
		if (value == null) {
			if (valueClass == int.class || valueClass == Integer.class) { 
				value = 1;
			} else if (valueClass == long.class || valueClass == Long.class) { 
				value = 1L;
			} else if (valueClass == String.class) { 
				value = "string";
			} else if (valueClass == boolean.class || valueClass == Boolean.class) { 
				value = true;
			} else if (valueClass == Date.class) {
				value = new Date();
			} else if (Enum.class.isAssignableFrom(valueClass)) {
				value = EnumSet.allOf((Class<Enum>) valueClass).iterator().next();
			} else if (Collection.class.isAssignableFrom(valueClass)) {
				Class<?> collectionElementType = ReflectionUtils.getCollectionElementClass(valueType);
				if (collectionElementType == null)
					throw new RuntimeException("Do not know how to generate collection element example");
				Collection<Object> collection;
				if (valueClass == List.class || valueClass == Collection.class) {
					collection = new ArrayList<Object>();
				} else if (valueClass == Set.class) {
					collection = new HashSet<>();
				} else {
					try {
						collection = (Collection<Object>) valueClass.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				collection.add(getExampleValue(collectionElementType, parsedTypes));
				return (Serializable) collection;
			} else if (Map.class.isAssignableFrom(valueClass)) {
				Type mapKeyType = ReflectionUtils.getMapKeyType(valueType);
				if (mapKeyType == null)
					throw new RuntimeException("Do not know how to generate map key example");
				Type mapValueType = ReflectionUtils.getMapValueType(valueType);
				if (mapValueType == null)
					throw new RuntimeException("Do not know how to generate map value example");
				
				Map<Object, Object> map;
				if (valueClass == Map.class) {
					map = new HashMap<>();
				} else {
					try {
						map = (Map<Object, Object>) valueClass.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				
				map.put(getExampleValue(mapKeyType, parsedTypes), getExampleValue(mapValueType, parsedTypes));
				
				return (Serializable) map;
			} else {
				try {
					if (parsedTypes.add(valueClass)) {
						Class<?> instantiationClass;
						if (Modifier.isAbstract(valueClass.getModifiers())) {
							List<Class<?>> implementations = new ArrayList<>(
									OneDev.getInstance(ImplementationRegistry.class).getImplementations(valueClass));
							Collections.sort(implementations, new Comparator<Class<?>>() {

								@Override
								public int compare(Class<?> o1, Class<?> o2) {
									return o1.getName().compareTo(o2.getName());
								}
								
							});
							instantiationClass = implementations.iterator().next();
						} else {
							instantiationClass = valueClass;
						}
						value = new ObjenesisStd(true).getInstantiatorOf(instantiationClass).newInstance();
						for (Field field: getJsonFields(instantiationClass)) {
							Object fieldValue = new ExampleProvider(valueClass, field.getAnnotation(Api.class)).getExample();
							if (fieldValue == null) {
								if (field.getAnnotation(ManyToOne.class) != null) {
									fieldValue = field.getType().newInstance();
									Field idField = AbstractEntity.class.getDeclaredField("id");
									Object id = new ExampleProvider(idField.getType(), idField.getAnnotation(Api.class)).getExample();
									if (id == null) 
										id = getExampleValue(idField.getGenericType(), new HashSet<>(parsedTypes));
									idField.setAccessible(true);
									idField.set(fieldValue, id);
								} else {
									fieldValue = getExampleValue(field.getGenericType(), new HashSet<>(parsedTypes));
								}
							}
							field.setAccessible(true);
							field.set(value, fieldValue);
						}
					} 
				} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return (Serializable) value;
	}
	
	public static List<Field> getJsonFields(Class<?> beanClass) {
		List<Field> fields = new ArrayList<>();
		for (Field field: BeanUtils.findFields(beanClass)) {
			if (field.getAnnotation(JsonIgnore.class) != null 
					|| field.getAnnotation(OneToMany.class) != null
					|| field.getAnnotation(Transient.class) != null
					|| Modifier.isTransient(field.getModifiers()))
				continue;
			if (field.getAnnotation(JsonProperty.class) != null) {
				fields.add(field);
			} else {
				Method getter = BeanUtils.findGetter(beanClass, field.getName());
				Method setter = BeanUtils.findSetter(beanClass, field.getName(), field.getType());
					
				if ((getter == null || getter.getAnnotation(JsonIgnore.class) == null) 
						&& (setter == null || setter.getAnnotation(JsonIgnore.class) == null)) {
					fields.add(field);
				}
			}
		}
		
		Collections.sort(fields, new ApiComparator());
		
		return fields;
	}	
	
}
