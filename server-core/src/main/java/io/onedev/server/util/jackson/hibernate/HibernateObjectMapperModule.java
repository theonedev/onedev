package io.onedev.server.util.jackson.hibernate;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import org.hibernate.proxy.HibernateProxy;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HibernateObjectMapperModule extends Module {

	private final Dao dao;
	
	@Inject
    public HibernateObjectMapperModule(Dao generalDao) {
		this.dao = generalDao;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void setupModule(SetupContext context) {
		
		context.appendAnnotationIntrospector(new HibernateAnnotationIntrospector());

		context.addSerializers(new Serializers.Base(){

			@Override
		    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
		        Class<?> raw = type.getRawClass();
		        if (HibernateProxy.class.isAssignableFrom(raw)) 
		            return new HibernateProxySerializer();
		        else
		        	return null;
		    }

			@Override
			public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type,
					BeanDescription beanDesc, TypeSerializer elementTypeSerializer,
					JsonSerializer<Object> elementValueSerializer) {
				// without this logic, Jackson will not be able to handle collections containing 
				// hibernate proxies
				if (AbstractEntity.class.isAssignableFrom(type.getContentType().getRawClass())) {
					return new CollectionSerializer(type.getContentType(), false, 
							elementTypeSerializer, elementValueSerializer);
				} else {
					return null;
				}
			}

			@SuppressWarnings("deprecation")
			@Override
			public JsonSerializer<?> findMapSerializer(SerializationConfig config, MapType type,
					BeanDescription beanDesc, JsonSerializer<Object> keySerializer,
					TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
				// without this logic, Jackson will not be able to handle maps containing 
				// hibernate proxies
				if (AbstractEntity.class.isAssignableFrom(type.getContentType().getRawClass())) {
	                Object filterId = config.getAnnotationIntrospector().findFilterId((Annotated)beanDesc.getClassInfo());
	                return MapSerializer.construct(
	                		config.getAnnotationIntrospector().findPropertyIgnorals(beanDesc.getClassInfo()).getIgnored(), 
	                		type, false, elementTypeSerializer, keySerializer, elementValueSerializer, filterId);
				} else {
					return null;
				}
			}
			
		});
		
		context.addBeanDeserializerModifier(new BeanDeserializerModifier() {

			@Override
			public JsonDeserializer<?> modifyDeserializer(
					DeserializationConfig config, BeanDescription beanDesc,
					final JsonDeserializer<?> deserializer) {
				if (AbstractEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
					Class<? extends AbstractEntity> entityClass = (Class<? extends AbstractEntity>) beanDesc.getBeanClass();
					return new EntityDeserializer(entityClass, (BeanDeserializer) deserializer, dao);
				} else {
					return deserializer;
				}
			}
			
		});
	}

	@Override
	public String getModuleName() {
		return "HibernateModule";
	}

	@Override
	public Version version() {
		return Version.unknownVersion();
	}

}
