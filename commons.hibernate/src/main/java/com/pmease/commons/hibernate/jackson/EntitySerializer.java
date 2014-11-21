package com.pmease.commons.hibernate.jackson;

import java.io.IOException;

import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;

public class EntitySerializer extends BeanSerializer {

	public EntitySerializer(BeanSerializer defaultSerializer) {
		super(defaultSerializer);
	}

	@Override
	protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) 
			throws IOException, JsonGenerationException {
		if (bean instanceof HibernateProxy)
			bean = ((HibernateProxy) bean).getHibernateLazyInitializer().getImplementation();

		super.serializeFields(bean, jgen, provider);
	}

}
