package com.pmease.commons.product.model;

import javax.persistence.Entity;

import com.pmease.commons.hibernate.AbstractEntity;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class User extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private String email;
	
	private String password;
	
	private String sex;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

}
