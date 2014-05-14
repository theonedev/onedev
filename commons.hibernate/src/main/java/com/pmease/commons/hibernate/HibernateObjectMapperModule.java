package com.pmease.commons.hibernate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.pmease.commons.hibernate.dao.GeneralDao;

@Singleton
public class HibernateObjectMapperModule extends Hibernate4Module {

	private final GeneralDao generalDao;
	
	@Inject
    public HibernateObjectMapperModule(GeneralDao generalDao) {
		this.generalDao = generalDao;
        enable(Feature.FORCE_LAZY_LOADING);
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);

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

	@SuppressWarnings("serial")
	@Override
	protected AnnotationIntrospector annotationIntrospector() {
		return new AnnotationIntrospector() {

			@Override
			public Version version() {
				return Version.unknownVersion();
			}

			@Override
			public boolean hasIgnoreMarker(AnnotatedMember m) {
				return super.hasIgnoreMarker(m) || m.hasAnnotation(OneToMany.class) || m.hasAnnotation(Transient.class);
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object findSerializer(Annotated am) {
				if (am.hasAnnotation(ManyToOne.class)) {
					return new ManyToOneSerializer((Class<AbstractEntity>) am.getRawType());
				} else {
					return super.findDeserializer(am);
				}
			}

			@Override
			public Object findDeserializer(Annotated am) {
				if (am.hasAnnotation(ManyToOne.class)) {
					return new ManyToOneDeserializer(am.getRawType());
				} else {
					return super.findDeserializer(am);
				}
			}

		};
	}

}
