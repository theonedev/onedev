package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.model.support.issue.LinkSpecOpposite;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.facade.LinkSpecFacade;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.annotation.Editable;

@Editable
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class LinkSpec extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NAME = "name";
	
	public static final String PROP_OPPOSITE = "opposite";
	
	@Column(nullable=false, unique=true)
	private String name;
	
	private boolean multiple;
	
	private String issueQuery;
	
	@Lob
	@Column(length=65535)
	private LinkSpecOpposite opposite;
	
	private int order;
	
	@OneToMany(mappedBy=IssueLink.PROP_SPEC, cascade=CascadeType.REMOVE)
	private Collection<IssueLink> links = new ArrayList<>();
	
	@OneToMany(mappedBy=LinkAuthorization.PROP_LINK, cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<LinkAuthorization> authorizations = new ArrayList<>();
	
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


	@Editable(order=160, name="Linkable Issues", placeholder="All issues", 
			description="Optionally specify criteria of issues which can be linked")
	@io.onedev.server.annotation.IssueQuery
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
	
	public String getName(boolean opposite) {
		return opposite?getOpposite().getName():getName();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public IssueQuery getParsedIssueQuery(@Nullable Project project) {
		IssueQueryParseOption option = new IssueQueryParseOption();
		return IssueQuery.parse(project, issueQuery, option, false);
	}
	
	public Collection<IssueQueryUpdater> getQueryUpdaters() {
		Collection<IssueQueryUpdater> updaters = new ArrayList<>();
		updaters.add(new IssueQueryUpdater() {
			
			@Override
			protected void setIssueQuery(String issueQuery) {
				LinkSpec.this.issueQuery = issueQuery;
			}
			
			@Override
			protected boolean isAllowEmpty() {
				return true;
			}
			
			@Override
			protected String getIssueQuery() {
				return issueQuery;
			}

			@Override
			protected Usage getUsage() {
				return new Usage().add("linkable issues").prefix("link '" + getName() + "'");
			}
			
		});
		if (opposite != null) {
			updaters.add(new IssueQueryUpdater() {
				
				@Override
				protected void setIssueQuery(String issueQuery) {
					opposite.setIssueQuery(issueQuery);
				}
				
				@Override
				protected boolean isAllowEmpty() {
					return true;
				}
				
				@Override
				protected String getIssueQuery() {
					return opposite.getIssueQuery();
				}

				@Override
				protected Usage getUsage() {
					return new Usage().add("linkable issues").prefix("opposite").prefix("link '" + getName() + "'");
				}
				
			});
		}
		return updaters;
	}

	@Override
	public LinkSpecFacade getFacade() {
		return new LinkSpecFacade(getId(), getName(), getOpposite()!=null?getOpposite().getName():null);
	}

}
