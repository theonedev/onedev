package io.onedev.server.model;

import static io.onedev.server.model.Dashboard.PROP_NAME;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import io.onedev.server.model.support.widget.Widget;

@Entity
@Table(
		indexes={@Index(columnList="o_owner_id"), @Index(columnList=PROP_NAME)}, 
		uniqueConstraints={@UniqueConstraint(columnNames={"o_owner_id", PROP_NAME})})
public class Dashboard extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_OWNER = "owner";
	
	public static final String PROP_NAME = "name";
	
	public static final String PROP_FOR_EVERYONE = "forEveryone";
	
	public static final int COLUMNS = 16;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;
	
	@Column(nullable=false)
	private String name;
	
	private boolean forEveryone;
	
	@Lob
	@Column(nullable=false, length=65535)
	private ArrayList<Widget> widgets = new ArrayList<>();
	
	@OneToMany(mappedBy="dashboard", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<DashboardVisit> visits = new ArrayList<>();
	
	@OneToMany(mappedBy="dashboard", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<DashboardUserShare> userShares = new ArrayList<>();
	
	@OneToMany(mappedBy="dashboard", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<DashboardGroupShare> groupShares = new ArrayList<>();
	
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isForEveryone() {
		return forEveryone;
	}

	public void setForEveryone(boolean forEveryone) {
		this.forEveryone = forEveryone;
	}

	public ArrayList<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(ArrayList<Widget> widgets) {
		this.widgets = widgets;
	}

	public Collection<DashboardUserShare> getUserShares() {
		return userShares;
	}

	public void setUserShares(Collection<DashboardUserShare> userShares) {
		this.userShares = userShares;
	}

	public Collection<DashboardGroupShare> getGroupShares() {
		return groupShares;
	}

	public void setGroupShares(Collection<DashboardGroupShare> groupShares) {
		this.groupShares = groupShares;
	}

	public Collection<DashboardVisit> getVisits() {
		return visits;
	}

	public void setVisits(Collection<DashboardVisit> visits) {
		this.visits = visits;
	}
	
}
