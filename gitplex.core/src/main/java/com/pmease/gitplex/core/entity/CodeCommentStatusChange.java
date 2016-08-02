package com.pmease.gitplex.core.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicUpdate;

import com.pmease.gitplex.core.entity.support.CodeCommentActivity;

@Entity
@DynamicUpdate 
public class CodeCommentStatusChange extends CodeCommentActivity {

	private static final long serialVersionUID = 1L;
	
	private boolean resolved;
	
	private String note;
	
	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	@Override
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
