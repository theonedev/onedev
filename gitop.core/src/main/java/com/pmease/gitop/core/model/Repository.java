package com.pmease.gitop.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;

@Entity
@org.hibernate.annotations.Cache(
		usage=org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"owner", "name"})
})
@SuppressWarnings("serial")
public class Repository extends AbstractEntity {
	
	@Column(nullable=false)
	private String name;
	
	private String description;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_REPO_OWNER")
	private User owner;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
	
}
