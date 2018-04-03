package io.onedev.server.web.page.test;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public class Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private int age;

	@Editable(order=200)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=300)
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}
