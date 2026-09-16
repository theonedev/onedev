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
		indexes={@Index(columnList="user_id"), @Index(columnList="dashboard_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"user_id", "dashboard_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class DashboardUserShare extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static String PROP_DASHBOARD = "dashboard";
	
	public static String PROP_USER = "user";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Dashboard dashboard;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
