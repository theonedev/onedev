package com.pmease.gitplex.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

@Entity
@Table(
		indexes={@Index(columnList="g_team_id"), @Index(columnList="g_depot_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_team_id", "g_depot_id"})
})
public class TeamAuthorization extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Depot depot;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Team team;
	
	@Column(nullable=false)
	private DepotPrivilege privilege = DepotPrivilege.READ;
	
	public Depot getDepot() {
		return depot;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public DepotPrivilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(DepotPrivilege privilege) {
		this.privilege = privilege;
	}

}
