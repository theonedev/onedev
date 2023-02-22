package io.onedev.server.model.support;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.facade.ProjectBelongingFacade;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public abstract class EntityComment extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final int MAX_CONTENT_LEN = 100000;

	public static final String PROP_USER = "user";
	
	public static final String PROP_CONTENT = "content";
	
	@Column(nullable=false)
	private Date date = new Date();

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false, length=MAX_CONTENT_LEN)
	private String content;

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = StringUtils.abbreviate(content, MAX_CONTENT_LEN);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public abstract AbstractEntity getEntity();
	
}
