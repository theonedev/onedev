package io.onedev.server.model;

import static io.onedev.server.model.GitLfsLock.PROP_PATH;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import io.onedev.server.annotation.Editable;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={@Index(columnList="o_owner_id"), @Index(columnList=PROP_PATH)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class GitLfsLock extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_PATH = "path";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;
	
	@Column(nullable=false, unique=true)
	private String path;
	
	@Column(nullable=false)
	private Date date = new Date();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
