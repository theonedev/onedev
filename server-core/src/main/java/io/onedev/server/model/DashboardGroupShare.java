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
		indexes={@Index(columnList="o_group_id"), @Index(columnList="o_dashboard_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_group_id", "o_dashboard_id"})
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
