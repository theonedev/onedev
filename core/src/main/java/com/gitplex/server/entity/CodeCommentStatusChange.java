package com.gitplex.server.entity;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.gitplex.server.entity.support.CodeCommentActivity;

@Entity
@Table(indexes={@Index(columnList="g_comment_id"), @Index(columnList="g_user_id")})
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
