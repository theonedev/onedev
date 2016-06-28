package com.pmease.commons.hibernate.dao;

import com.pmease.commons.hibernate.AbstractEntity;

public interface DaoListener {
	
	void onPersistEntity(AbstractEntity entity);
	
	void onRemoveEntity(AbstractEntity entity);
	
}
