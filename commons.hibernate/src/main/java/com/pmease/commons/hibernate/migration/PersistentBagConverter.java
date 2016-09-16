package com.pmease.commons.hibernate.migration;

import java.util.ArrayList;

import org.hibernate.collection.internal.PersistentBag;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PersistentBagConverter extends CollectionConverter {

	public PersistentBagConverter(Mapper mapper) {
		super(mapper);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		source = new ArrayList((PersistentBag)source);
		super.marshal(source, writer, context);
	}

	public boolean canConvert(Class type) {
		return type == PersistentBag.class;
	}

}
