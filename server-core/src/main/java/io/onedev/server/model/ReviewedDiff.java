package io.onedev.server.model;

import javax.persistence.*;
import java.util.Date;

import static io.onedev.server.model.ReviewedDiff.*;

@Entity
@Table(
		indexes={
				@Index(columnList= PROP_OLD_COMMIT_HASH), @Index(columnList= PROP_NEW_COMMIT_HASH),
				@Index(columnList= PROP_BLOB_PATH), @Index(columnList = PROP_DATE)
		}, uniqueConstraints = {
				@UniqueConstraint(columnNames={"o_user_id", PROP_OLD_COMMIT_HASH, PROP_NEW_COMMIT_HASH, PROP_BLOB_PATH})
		}
)
public class ReviewedDiff extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_OLD_COMMIT_HASH = "oldCommitHash";
	
	public static final String PROP_NEW_COMMIT_HASH = "newCommitHash";
	
	public static final String PROP_BLOB_PATH = "blobPath";
	
	public static final String PROP_DATE = "date";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private String oldCommitHash;

	@Column(nullable=false)
	private String newCommitHash;
	
	@Column(nullable=false)
	private String blobPath;
	
	@Column(nullable = false)
	private Date date;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public void setOldCommitHash(String oldCommitHash) {
		this.oldCommitHash = oldCommitHash;
	}

	public String getNewCommitHash() {
		return newCommitHash;
	}

	public void setNewCommitHash(String newCommitHash) {
		this.newCommitHash = newCommitHash;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public void setBlobPath(String blobPath) {
		this.blobPath = blobPath;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
