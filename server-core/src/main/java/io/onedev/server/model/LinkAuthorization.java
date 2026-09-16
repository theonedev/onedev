package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
		indexes={@Index(columnList="link_id"), @Index(columnList="role_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"link_id", "role_id"})})
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
