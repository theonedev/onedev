package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.issue.LinkSpecOpposite;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Entity
@Editable
public class LinkSpec extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Column(nullable=false, unique=true)
	private String name;
	
	private boolean multiple;
	
	private String issueQuery;
	
	@Lob
	@Column(length=65535)
	private LinkSpecOpposite opposite;
	
	@OneToMany(mappedBy=IssueLink.PROP_SPEC, cascade=CascadeType.REMOVE)
	private Collection<IssueLink> links = new ArrayList<>();
	
	@Editable(order=100, description="Name of the link")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=150, name="Multiple", description="Whether or not multiple issues can be linked")
	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}


	@Editable(order=160, name="Linkable Issues", description="Optionally specify criteria of issues which can be linked")
	@IssueQuery(withCurrentBuildCriteria=false, withCurrentCommitCriteria=false, withCurrentPullRequestCriteria=false, 
				withCurrentUserCriteria=false, withOrder=false)
	@NameOfEmptyValue("All issues")
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(String issueQuery) {
		this.issueQuery = issueQuery;
	}

	@Editable(order=175, name="Asymmetric", description="Whether or not the link is asymmetric. A asymmetric link has different "
			+ "meaning from different side. For instance a 'parent-child' link is asymmetric, while a 'related to' link is symmetric")
	public LinkSpecOpposite getOpposite() {
		return opposite;
	}

	public void setOpposite(LinkSpecOpposite opposite) {
		this.opposite = opposite;
	}
	
}
