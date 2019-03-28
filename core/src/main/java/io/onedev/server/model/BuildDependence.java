package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
		indexes={@Index(columnList="o_dependent_id"), @Index(columnList="o_dependency_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_dependent_id", "o_dependency_id"})
})
public class BuildDependence extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build2 dependent;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Build2 dependency;

	public Build2 getDependent() {
		return dependent;
	}

	public void setDependent(Build2 dependent) {
		this.dependent = dependent;
	}

	public Build2 getDependency() {
		return dependency;
	}

	public void setDependency(Build2 dependency) {
		this.dependency = dependency;
	}
	
}
