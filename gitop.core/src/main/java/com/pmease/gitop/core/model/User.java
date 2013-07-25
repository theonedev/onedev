package com.pmease.gitop.core.model;

import javax.persistence.Entity;

import com.pmease.commons.security.AbstractUser;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class User extends AbstractUser {
	
}
