package io.onedev.server.util.xstream;

import java.lang.reflect.Field;

import javax.persistence.ManyToOne;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.Mapper;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;

public class ReflectionConverter extends com.thoughtworks.xstream.converters.reflection.ReflectionConverter {

	public ReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}
	
    @SuppressWarnings("rawtypes")
	public ReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider, Class type) {
    	super(mapper, reflectionProvider, type);
    }
    
	@Override
	protected void marshallField(MarshallingContext context, Object newObj, Field field) {
		if (field.getAnnotation(ManyToOne.class) != null) 
			super.marshallField(context, ((AbstractEntity) newObj).getId(), field);
		else 
			super.marshallField(context, newObj, field);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Object unmarshallField(UnmarshallingContext context, Object result, Class type, Field field) {
		if (field.getAnnotation(ManyToOne.class) != null) {
			Long entityId = (Long) context.convertAnother(context.currentObject(), Long.class);
			return AppLoader.getInstance(Dao.class).load((Class<? extends AbstractEntity>) field.getType(), entityId);
		} else {
			return super.unmarshallField(context, result, type, field);
		}
	}
	
}
