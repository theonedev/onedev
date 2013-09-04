package com.pmease.gitop.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.pmease.commons.persistence.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class Commit extends AbstractEntity {
	
	@Column(nullable=false, unique=true)
	private String revision;
	
	private Date date;

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
