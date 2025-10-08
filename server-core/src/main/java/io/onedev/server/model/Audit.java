package io.onedev.server.model;

import static io.onedev.server.model.Audit.PROP_ACTION;
import static io.onedev.server.model.Audit.PROP_DATE;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
		indexes={
				@Index(columnList="o_project_id"), @Index(columnList= "o_user_id"),  
				@Index(columnList= PROP_DATE), @Index(columnList= PROP_ACTION)}
)
public class Audit extends AbstractEntity { 

	private static final long serialVersionUID = 1L;
	
	private static final int MAX_ACTION_LEN = 512;

	private static final int MAX_CONTENT_LEN = 1048576;
		
	public static final String PROP_PROJECT = "project";

	public static final String PROP_USER = "user";

	public static final String PROP_ACTION = "action";
	
	public static final String PROP_DATE = "date";
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private Project project;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false, length=MAX_ACTION_LEN)
	private String action;

	@Column(nullable = false)
	private Date date = new Date();

	@Column(length=MAX_CONTENT_LEN)
	private String oldContent;

	@Column(length=MAX_CONTENT_LEN)
	private String newContent;
	
	@Nullable
	public Project getProject() {
		return project;
	}

	public void setProject(@Nullable Project project) {
		this.project = project;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		if (action.length() > MAX_ACTION_LEN)
			throw new IllegalArgumentException("Audit action is too long: " + action);
		this.action = action;
	}
	
	@Nullable
	public String getOldContent() {
		return oldContent;
	}

	public void setOldContent(@Nullable String oldContent) {
		if (oldContent != null && oldContent.length() > MAX_CONTENT_LEN)
			throw new IllegalArgumentException("Audit content is too long: " + oldContent);
		this.oldContent = oldContent;
	}
	
	@Nullable
	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(@Nullable String newContent) {
		if (newContent != null && newContent.length() > MAX_CONTENT_LEN)
			throw new IllegalArgumentException("Audit content is too long: " + newContent);
		this.newContent = newContent;
	}
	
}
