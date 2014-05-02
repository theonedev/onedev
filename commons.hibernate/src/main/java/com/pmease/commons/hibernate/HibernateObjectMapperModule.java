package com.pmease.commons.hibernate;

import javax.inject.Singleton;
import javax.persistence.Embedded;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

@Singleton
public class HibernateObjectMapperModule extends Hibernate4Module {

    public HibernateObjectMapperModule() {
        enable(Feature.FORCE_LAZY_LOADING);
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
				return super.hasIgnoreMarker(m) || m.hasAnnotation(OneToMany.class);
			}

			@Override
			public Object findSerializer(Annotated am) {
				if (am.hasAnnotation(Embedded.class)) {
					System.out.println(am);
				}
				if (am.hasAnnotation(ManyToOne.class)) {
					System.out.println(am);
				}
				return super.findSerializer(am);
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
