package io.onedev.server.model;

import static io.onedev.server.model.RunCache.PROP_ACCESS_DATE;
import static io.onedev.server.model.RunCache.PROP_CHECKSUM;
import static io.onedev.server.model.RunCache.PROP_KEY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList=PROP_KEY), @Index(columnList = PROP_ACCESS_DATE)}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", PROP_KEY, PROP_CHECKSUM})})
public class RunCache extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_KEY = "key";
	
	public static final String PROP_CHECKSUM = "checksum";
	
	public static final String PROP_ACCESS_DATE = "accessDate";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String key;
	
	private String checksum;
	
	private Date accessDate;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Nullable
	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(@Nullable String checksum) {
		this.checksum = checksum;
	}

	public Date getAccessDate() {
		return accessDate;
	}

	public void setAccessDate(Date accessDate) {
		this.accessDate = accessDate;
	}

	public static String getLockName(Long projectId, Long cacheId) {
		return "job-cache:" + projectId + ":" + cacheId;
	}
	
}
