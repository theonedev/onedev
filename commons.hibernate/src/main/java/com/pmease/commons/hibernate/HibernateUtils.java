package com.pmease.commons.hibernate;

import org.hibernate.proxy.HibernateProxy;


public class HibernateUtils {
	
	/**
	 * This method is created to get identifier of entity without triggering 
	 * lazy load the whole entity object.
	 * @param entity
	 * @return
	 */
	public static Long getId(AbstractEntity entity) {
		if (entity instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy) entity;
			return (Long) proxy.getHibernateLazyInitializer().getIdentifier();
		} else {
			return entity.getId();
		}
	}
	
	public static void setId(AbstractEntity entity, Long id) {
		if (entity instanceof HibernateProxy) {
			HibernateProxy proxy = (HibernateProxy) entity;
			proxy.getHibernateLazyInitializer().setIdentifier(id);
		} else {
			entity.setId(id);
		}
	}

}
