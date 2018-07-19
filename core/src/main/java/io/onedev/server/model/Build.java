package io.onedev.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="g_configuration_id"), @Index(columnList="commit")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_configuration_id", "commit"})}
)
public class Build extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String STATUS = "status";
	
	public enum Status {
		SUCCESS("successful"), 
		FAILURE("failed"), 
		ERROR("in error"), 
		RUNNING("running");
		
		private final String description;
		
		Status(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

	};

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Configuration configuration;
	
	@Column(nullable=false)
	private String commit;
	
	@Column(nullable=false)
	private Status status; 
	
	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false)
	private String description;
	
	@Column(nullable=false)
	private String url;

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
