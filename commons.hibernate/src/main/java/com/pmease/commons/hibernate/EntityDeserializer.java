package com.pmease.commons.hibernate;

import java.io.IOException;

import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.pmease.commons.hibernate.dao.GeneralDao;

@SuppressWarnings("serial")
public class EntityDeserializer extends BeanDeserializer {

	private final Class<? extends AbstractEntity> entityClass;
	
	private final BeanDeserializer defaultDeserializer;
	
	private final GeneralDao generalDao;
	
	public EntityDeserializer(
			Class<? extends AbstractEntity> entityClass, 
			BeanDeserializer defaultDeserializer, 
			GeneralDao generalDao) {
		super(defaultDeserializer);
		
		this.entityClass = entityClass;
		this.defaultDeserializer = defaultDeserializer;
		this.generalDao = generalDao;
	}

	@Override
	public AbstractEntity deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		TreeNode node = jp.readValueAsTree();
		jp = node.traverse(jp.getCodec());
		jp.nextToken();
		TreeNode idNode = node.get("id");
		AbstractEntity entity;
		if (idNode != null && idNode.toString() != null) {
			entity = generalDao.load(entityClass, Long.valueOf(idNode.toString()));
			if (entity instanceof HibernateProxy) 
				entity = (AbstractEntity) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
			defaultDeserializer.deserialize(jp, ctxt, entity);
		} else {
			try {
				entity = entityClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			defaultDeserializer.deserialize(jp, ctxt, entity);
		}
		return entity;
	}
	
}
