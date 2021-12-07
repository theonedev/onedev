package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
		indexes={@Index(columnList="o_link_id"), @Index(columnList="o_role_id")}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_link_id", "o_role_id"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class LinkAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_LINK = "link";
	
	public static final String PROP_ROLE = "role";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private LinkSpec link;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Role role;

	public LinkSpec getLink() {
		return link;
	}

	public void setLink(LinkSpec link) {
		this.link = link;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
}
