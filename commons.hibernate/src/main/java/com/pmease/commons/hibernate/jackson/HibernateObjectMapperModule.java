package com.pmease.commons.hibernate.jackson;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;

@Singleton
public class HibernateObjectMapperModule extends Module {

	private final Dao generalDao;
	
	@Inject
    public HibernateObjectMapperModule(Dao generalDao) {
		this.generalDao = generalDao;
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
			
		});
		
		context.addBeanDeserializerModifier(new BeanDeserializerModifier() {

			@Override
			public JsonDeserializer<?> modifyDeserializer(
					DeserializationConfig config, BeanDescription beanDesc,
					final JsonDeserializer<?> deserializer) {
				if (AbstractEntity.class.isAssignableFrom(beanDesc.getBeanClass())) {
					Class<? extends AbstractEntity> entityClass = (Class<? extends AbstractEntity>) beanDesc.getBeanClass();
					BeanDeserializer defaultDeserializer = (BeanDeserializer) deserializer;
					return new EntityDeserializer(entityClass, defaultDeserializer, generalDao);
				} else {
					return super.modifyDeserializer(config, beanDesc, deserializer);
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
