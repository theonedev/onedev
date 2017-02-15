package com.gitplex.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gitplex.server.persistence.AbstractEntity;
import com.gitplex.server.security.privilege.DepotPrivilege;

@Entity
@Table(
		indexes={@Index(columnList="g_user_id"), @Index(columnList="g_depot_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_user_id", "g_depot_id"})
})
public class UserAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Depot depot;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;
	
	@Column(nullable=false)
	private DepotPrivilege privilege = DepotPrivilege.READ;
	
	public Depot getDepot() {
		return depot;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}

	public DepotPrivilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(DepotPrivilege privilege) {
		this.privilege = privilege;
	}

}
