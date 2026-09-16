package io.onedev.server.model;

import java.util.Date;

import jakarta.persistence.Column;
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
public class DashboardVisit extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static String PROP_DASHBOARD = "dashboard";
	
	public static String PROP_USER = "user";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Dashboard dashboard;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();

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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
