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
		indexes={@Index(columnList="group_id"), @Index(columnList="dashboard_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"group_id", "dashboard_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class DashboardGroupShare extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static String PROP_DASHBOARD = "dashboard";
	
	public static String PROP_GROUP = "group";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Dashboard dashboard;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Group group;

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
