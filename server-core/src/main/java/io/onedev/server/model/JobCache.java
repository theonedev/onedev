package io.onedev.server.model;

import javax.persistence.*;

import java.util.Date;

import static io.onedev.server.model.JobCache.PROP_ACCESS_DATE;
import static io.onedev.server.model.JobCache.PROP_KEY;

/**
 * @author robin
 *
 */
@Entity
@Table(
		indexes={@Index(columnList="o_project_id"), @Index(columnList=PROP_KEY), @Index(columnList = PROP_ACCESS_DATE)}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_project_id", PROP_KEY})})
public class JobCache extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT = "project";
	
	public static final String PROP_KEY = "key";
	
	public static final String PROP_ACCESS_DATE = "accessDate";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Project project;
	
	@Column(nullable=false)
	private String key;
	
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
