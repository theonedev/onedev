package io.onedev.server.model.support;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.User;

@MappedSuperclass
public abstract class CommentRevision extends AbstractEntity {

	private static final long serialVersionUID = 1L;

    public static final String PROP_USER = "user";

	public static final int MAX_CONTENT_LEN = 100000;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();

    @Column(length=MAX_CONTENT_LEN)
	private String oldContent;

    @Column(length=MAX_CONTENT_LEN)
	private String newContent;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    @Nullable
    public String getOldContent() {
        return oldContent;
    }

    public void setOldContent(@Nullable String oldContent) {
		this.oldContent = StringUtils.abbreviate(oldContent, MAX_CONTENT_LEN);
    }

    @Nullable
    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(@Nullable String newContent) {
        this.newContent = StringUtils.abbreviate(newContent, MAX_CONTENT_LEN);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
