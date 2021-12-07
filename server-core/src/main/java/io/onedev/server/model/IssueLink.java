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
		indexes={@Index(columnList="o_source_id"), @Index(columnList="o_target_id"), @Index(columnList="o_spec_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_source_id", "o_target_id", "o_spec_id"})})
public class IssueLink extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_SOURCE = "source";
	
	public static final String PROP_TARGET = "target";
	
	public static final String PROP_SPEC = "spec";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue source;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue target;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private LinkSpec spec;
	
	public Issue getSource() {
		return source;
	}

	public void setSource(Issue source) {
		this.source = source;
	}

	public Issue getTarget() {
		return target;
	}

	public void setTarget(Issue target) {
		this.target = target;
	}

	public LinkSpec getSpec() {
		return spec;
	}

	public void setSpec(LinkSpec spec) {
		this.spec = spec;
	}
	
	public Issue getLinked(Issue issue) {
		return issue.equals(source)?target:source;
	}

}
