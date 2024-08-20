package io.onedev.server.web.page.help;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.page.layout.AdministrationSettingContribution;
import io.onedev.server.web.page.layout.ContributedAdministrationSetting;
import io.onedev.server.web.page.project.setting.ContributedProjectSetting;
import io.onedev.server.web.page.project.setting.ProjectSettingContribution;
import org.objenesis.ObjenesisStd;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static io.onedev.server.web.page.help.ValueInfo.Origin.CREATE_BODY;
import static io.onedev.server.web.page.help.ValueInfo.Origin.UPDATE_BODY;
import static java.util.Comparator.comparing;

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
						for (var member: getJsonMembers(instantiationClass, origin)) {
							Object memberValue = new ExampleProvider(valueClass, member.getAnnotation(Api.class)).getExample();
							if (memberValue == null) {
								if (member.getAnnotation(ManyToOne.class) != null || member.getAnnotation(JoinColumn.class) != null) {
									memberValue = member.getType().getDeclaredConstructor().newInstance();
									Field idField = AbstractEntity.class.getDeclaredField("id");
									Object id = new ExampleProvider(idField.getType(), idField.getAnnotation(Api.class)).getExample();
									if (id == null)
										id = getExampleValue(idField.getGenericType(), new HashSet<>(parsedTypes), origin);
									idField.setAccessible(true);
									idField.set(memberValue, id);
								} else {
									memberValue = getExampleValue(member.getGenericType(), new HashSet<>(parsedTypes), origin);
								}
							}
							member.setValue(value, memberValue);
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
		
		Collections.sort(implementations, comparing((Class<?> o) -> o.getName()));
		
		return implementations;
	}
	
	public static List<JsonMember> getJsonMembers(Class<?> beanClass, ValueInfo.Origin origin) {
		List<JsonMember> members = new ArrayList<>();
		for (Field field: BeanUtils.findFields(beanClass)) {
			var jsonProperty = field.getAnnotation(JsonProperty.class);
			Method getter = BeanUtils.findGetter(beanClass, field.getName());
			if (field.getAnnotation(JsonIgnore.class) != null 
					|| field.getAnnotation(OneToMany.class) != null
					|| field.getAnnotation(Transient.class) != null
					|| field.getAnnotation(OneToOne.class) != null && field.getAnnotation(JoinColumn.class) == null
					|| Modifier.isTransient(field.getModifiers())
					|| (origin == CREATE_BODY || origin == UPDATE_BODY) && jsonProperty != null && jsonProperty.access() == READ_ONLY
					|| origin == UPDATE_BODY && field.getAnnotation(Immutable.class) != null
					|| getter != null && getter.getAnnotation(JsonProperty.class) != null) {
				continue;
			}
			if (getter == null || getter.getAnnotation(JsonIgnore.class) == null) {
				members.add(new JsonMember(field.getName(), field) {

					@Override
					public Class<?> getType() {
						return field.getType();
					}

					@Override
					public Type getGenericType() {
						return field.getGenericType();
					}

					@Override
					public Object getValue(Object obj) {
						field.setAccessible(true);
						try {
							return field.get(obj);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public void setValue(Object obj, Object value) {
						field.setAccessible(true);
						try {
							field.set(obj, value);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
					
				});
			}
		}
		
		for (Method getter: BeanUtils.findGetters(beanClass)) {
			var jsonProperty = getter.getAnnotation(JsonProperty.class);
			if (getter.getAnnotation(JsonIgnore.class) == null 
					&& jsonProperty != null
					&& (origin != CREATE_BODY && origin != UPDATE_BODY || jsonProperty.access() != READ_ONLY)
					&& (origin != UPDATE_BODY || getter.getAnnotation(Immutable.class) == null)) {
				members.add(new JsonMember(BeanUtils.getPropertyName(getter), getter) {

					@Override
					public Class<?> getType() {
						return getter.getReturnType();
					}

					@Override
					public Type getGenericType() {
						return getter.getGenericReturnType();
					}

					@Override
					public Object getValue(Object obj) {
						try {
							return getter.invoke(obj);
						} catch (IllegalAccessException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public void setValue(Object obj, Object value) {
						var setter = BeanUtils.findSetter(getter);
						if (setter != null) {
							try {
								setter.invoke(obj, value);
							} catch (IllegalAccessException | InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
				});
			}
		}
		
		Collections.sort(members, new ApiComparator());
		
		return members;
	}
}
