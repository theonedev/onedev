package io.onedev.server.web.page.help;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.page.help.ValueInfo.Origin;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import org.objenesis.ObjenesisStd;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

public class ApiHelpUtils {

	public static Serializable getExampleValue(Type valueType, ValueInfo.Origin origin) {
		return getExampleValue(valueType, Sets.newHashSet(), origin);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Serializable getExampleValue(Type valueType, Set<Class<?>> parsedTypes, ValueInfo.Origin origin) {
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
						collection = (Collection<Object>) valueClass.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
				collection.add(getExampleValue(collectionElementType, parsedTypes, origin));
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
						map = (Map<Object, Object>) valueClass.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
				
				map.put(getExampleValue(mapKeyType, parsedTypes, origin), 
						getExampleValue(mapValueType, parsedTypes, origin));
				
				return (Serializable) map;
			} else {
				try {
					if (parsedTypes.add(valueClass)) {
						Class<?> instantiationClass;
						if (Modifier.isAbstract(valueClass.getModifiers())) {
							List<Class<?>> implementations = getImplementations(valueClass);
							instantiationClass = implementations.iterator().next();
						} else {
							instantiationClass = valueClass;
						}
						value = new ObjenesisStd(true).getInstantiatorOf(instantiationClass).newInstance();
						for (Field field: getJsonFields(instantiationClass, origin)) {
							Object fieldValue = new ExampleProvider(valueClass, field.getAnnotation(Api.class)).getExample();
							if (fieldValue == null) {
								if (field.getAnnotation(ManyToOne.class) != null || field.getAnnotation(JoinColumn.class) != null) {
									fieldValue = field.getType().getDeclaredConstructor().newInstance();
									Field idField = AbstractEntity.class.getDeclaredField("id");
									Object id = new ExampleProvider(idField.getType(), idField.getAnnotation(Api.class)).getExample();
									if (id == null) 
										id = getExampleValue(idField.getGenericType(), new HashSet<>(parsedTypes), origin);
									idField.setAccessible(true);
									idField.set(fieldValue, id);
								} else {
									fieldValue = getExampleValue(field.getGenericType(), new HashSet<>(parsedTypes), origin);
								}
							}
							field.setAccessible(true);
							field.set(value, fieldValue);
						}
					} 
				} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException 
						| IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return (Serializable) value;
	}
	
	public static List<Class<?>> getImplementations(Class<?> clazz) {
		List<Class<?>> implementations = new ArrayList<>();
		if (clazz == ContributedAdministrationSetting.class) {
			for (AdministrationSettingContribution contribution: 
					OneDev.getExtensions(AdministrationSettingContribution.class)) {
				implementations.addAll(contribution.getSettingClasses());
			}
			if (implementations.isEmpty())
				implementations.add(ExamplePluginSetting.class);
		} else if (clazz == ContributedProjectSetting.class) {
			for (ProjectSettingContribution contribution: 
					OneDev.getExtensions(ProjectSettingContribution.class)) {
				implementations.addAll(contribution.getSettingClasses());
			}
			if (implementations.isEmpty())
				implementations.add(ExamplePluginSetting.class);
		} else {
			implementations.addAll(OneDev.getInstance(ImplementationRegistry.class).getImplementations(clazz));
		}
		
		Collections.sort(implementations, new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		
		return implementations;
	}
	
	public static List<Field> getJsonFields(Class<?> beanClass, ValueInfo.Origin origin) {
		List<Field> fields = new ArrayList<>();
		for (Field field: BeanUtils.findFields(beanClass)) {
			if (field.getAnnotation(JsonIgnore.class) != null 
					|| field.getAnnotation(OneToMany.class) != null
					|| field.getAnnotation(Transient.class) != null
					|| field.getAnnotation(OneToOne.class) != null && field.getAnnotation(JoinColumn.class) == null
					|| Modifier.isTransient(field.getModifiers())
					|| origin == Origin.REQUEST_BODY 
							&& field.getAnnotation(Api.class) != null 
							&& field.getAnnotation(Api.class).readOnly()) {
				continue;
			}
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
