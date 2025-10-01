package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.ValidationException;

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

	public void validate() {
		if (getSource().equals(getTarget()))
			throw new ValidationException("Can not link to self");
		if (getSpec().getOpposite() != null) {
			if (getSource().getTargetLinks().stream()
					.anyMatch(it -> it.getSpec().equals(getSpec()) && it.getTarget().equals(getTarget())))
				throw new ValidationException("Source issue already linked to target issue via specified link spec");
			if (!getSpec().isMultiple()
					&& getSource().getTargetLinks().stream().anyMatch(it -> it.getSpec().equals(getSpec())))
				throw new ValidationException(
						"Link spec is not multiple and the source issue is already linked to another issue via this link spec");
			if (!getSpec().getParsedIssueQuery(getSource().getProject()).matches(getTarget()))
				throw new ValidationException("Link spec not allowed to link to the target issue");
			if (!getSpec().getOpposite().isMultiple()
					&& getTarget().getSourceLinks().stream().anyMatch(it -> it.getSpec().equals(getSpec())))
				throw new ValidationException(
						"Opposite side of link spec is not multiple and the target issue is already linked to another issue via this link spec");
			if (!getSpec().getOpposite().getParsedIssueQuery(getSource().getProject())
					.matches(getSource()))
				throw new ValidationException("Opposite side of link spec not allowed to link to the source issue");
		} else {
			if (getSource().getLinks().stream().anyMatch(it -> it.getSpec().equals(getSpec())
					&& it.getLinked(getSource()).equals(getTarget())))
				throw new ValidationException("Specified issues already linked via specified link spec");
			if (!getSpec().isMultiple()
					&& getSource().getLinks().stream().anyMatch(it -> it.getSpec().equals(getSpec())))
				throw new ValidationException(
						"Link spec is not multiple and source issue is already linked to another issue via this link spec");
			var parsedIssueQuery = getSpec().getParsedIssueQuery(getSource().getProject());
			if (!parsedIssueQuery.matches(getSource()) || !parsedIssueQuery.matches(getTarget()))
				throw new ValidationException("Link spec not allowed to link specified issues");
		}
	}
}
