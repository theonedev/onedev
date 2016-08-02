package com.pmease.gitplex.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.DynamicUpdate;

import com.pmease.gitplex.core.entity.support.CodeCommentActivity;

/*
 * @DynamicUpdate annotation here along with various @OptimisticLock annotations
 * on certain fields tell Hibernate not to perform version check on those fields
 * which can be updated from background thread.
 */
@Entity
@DynamicUpdate 
public class CodeCommentReply extends CodeCommentActivity {
	
	private static final long serialVersionUID = 1L;

	@Lob
	@Column(nullable=false, length=65535)
	private String content;
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getNote() {
		return getContent();
	}

}
