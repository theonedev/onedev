package com.pmease.gitop.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;
import com.pmease.commons.hibernate.AbstractEntity;

/**
 * Auto-pull is set up by owner of target branch project to sync target branch
 * head with source branch whenever there is new commit in source branch. 
 * Unlike git pull command, it will override target branch head even if source 
 * branch is not fast-forward of target branch.
 *  
 */
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"source", "target"})
})
@SuppressWarnings("serial")
public class AutoPull extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch source;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch target;
	
	public Branch getSource() {
		return source;
	}

	public void setSource(Branch source) {
		this.source = source;
	}

	public Branch getTarget() {
		return target;
	}

	public void setTarget(Branch target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("source", getSource())
				.add("target", getTarget())
				.toString();
	}

}
