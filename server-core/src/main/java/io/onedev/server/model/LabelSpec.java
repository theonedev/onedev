package io.onedev.server.model;

import static io.onedev.server.model.LabelSpec.PROP_NAME;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.rest.annotation.Api;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import io.onedev.server.annotation.Color;
import io.onedev.server.annotation.Editable;

@Entity
@Table(indexes={@Index(columnList=PROP_NAME)})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class LabelSpec extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_NAME = "name";

	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	@Api(example = "#0d87e9")
	private String color = "#0d87e9";
	
	@OneToMany(mappedBy="spec", cascade=CascadeType.REMOVE)
	private Collection<ProjectLabel> projectLabels = new ArrayList<>();
	
	@OneToMany(mappedBy="spec", cascade=CascadeType.REMOVE)
	private Collection<BuildLabel> buildLabels = new ArrayList<>();
	
	@OneToMany(mappedBy="spec", cascade=CascadeType.REMOVE)
	private Collection<PullRequestLabel> pullRequestLabels = new ArrayList<>();
	
	@Editable
	@Override
	public Long getId() {
		return super.getId();
	}

	@Override
	public void setId(Long id) {
		super.setId(id);
	}
	
	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200)
	@NotEmpty
	@Color
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Collection<ProjectLabel> getProjectLabels() {
		return projectLabels;
	}

	public void setProjectLabels(Collection<ProjectLabel> projectLabels) {
		this.projectLabels = projectLabels;
	}

	public Collection<BuildLabel> getBuildLabels() {
		return buildLabels;
	}

	public void setBuildLabels(Collection<BuildLabel> buildLabels) {
		this.buildLabels = buildLabels;
	}

	public Collection<PullRequestLabel> getPullRequestLabels() {
		return pullRequestLabels;
	}

	public void setPullRequestLabels(Collection<PullRequestLabel> pullRequestLabels) {
		this.pullRequestLabels = pullRequestLabels;
	}
	
}
