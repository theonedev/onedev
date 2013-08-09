package com.pmease.gitop.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.pmease.commons.persistence.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class InvolvedCommit extends AbstractEntity {
	
	@Column(nullable=false, unique=true)
	private String revision;
	
}
