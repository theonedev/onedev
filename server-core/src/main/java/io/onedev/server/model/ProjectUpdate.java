package io.onedev.server.model;

import static io.onedev.server.model.ProjectUpdate.PROP_DATE;

import java.util.Date;

import javax.persistence.*;

import io.onedev.server.web.editable.annotation.Editable;

@Entity
@Table(indexes={@Index(columnList="o_project_id"), @Index(columnList=PROP_DATE)})
@Editable
public class ProjectUpdate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_DATE = "date";
	
	@OneToOne
	@JoinColumn(unique=true, nullable=false)
	private Project project; 

	@Column(nullable=false)
	private Date date = new Date();

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
