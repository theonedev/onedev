package io.onedev.server.util.jackson.hibernate;

import java.lang.reflect.Field;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

import io.onedev.server.model.AbstractEntity;

public class HibernateAnnotationIntrospector extends AnnotationIntrospector {

	@Override
	public Version version() {
		return Version.unknownVersion();
	}

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return m.hasAnnotation(Transient.class) || m.hasAnnotation(OneToMany.class) 
        		|| m.hasAnnotation(OneToOne.class) && !m.hasAnnotation(JoinColumn.class);
    }

	@Override
	public PropertyName findNameForSerialization(Annotated annotation) {
		if (annotation.hasAnnotation(ManyToOne.class) || annotation.hasAnnotation(JoinColumn.class)) 
			return new PropertyName(((Field)annotation.getAnnotated()).getName() + "Id");
		else
			return super.findNameForSerialization(annotation);
	}

	@Override
	public PropertyName findNameForDeserialization(Annotated annotation) {
		if (annotation.hasAnnotation(ManyToOne.class) || annotation.hasAnnotation(JoinColumn.class)) 
			return new PropertyName(((Field)annotation.getAnnotated()).getName() + "Id");
		else
			return super.findNameForDeserialization(annotation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object findSerializer(Annotated annotation) {
		if (annotation.hasAnnotation(ManyToOne.class) || annotation.hasAnnotation(JoinColumn.class)) {
			return new ForeignKeySerializer((Class<AbstractEntity>) annotation.getRawType());
		} else {
			return super.findSerializer(annotation);
		}
	}

	@Override
	public Object findDeserializer(Annotated am) {
		if (am.hasAnnotation(ManyToOne.class) || am.hasAnnotation(JoinColumn.class)) {
			return new ForeignKeyDeserializer(am.getRawType());
		} else {
			return super.findDeserializer(am);
		}
	}

}
