package com.pmease.commons.product.pages;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;

import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.product.Counter;
import com.pmease.commons.product.model.User;

public class Index {

	@Persist
	@Property
	private List<Counter> counters;

	@InjectComponent
	private Zone zone;
	
	@Property
	private Counter counter;
	
	@Inject
	private GeneralDao dao;
	
	void setupRender() {
		if (counters == null) {
			counters = new ArrayList<Counter>();
			counters.add(new Counter());
			counters.add(new Counter());
		}
	}
	
	void onActionFromModify() {
		System.out.println(dao.getReference(User.class, 1L).getEmail());
	}
	
	Object onActionFromIncrease(int index) {
		counter = counters.get(index);
		counter.increase();
		return zone;
	}
	
	public String getZoneId() {
		return zone.getClientId();
	}
}
