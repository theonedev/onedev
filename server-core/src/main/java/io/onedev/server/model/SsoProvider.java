package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import io.onedev.server.model.support.administration.sso.SsoConnector;

@Entity
@Table(indexes={@Index(columnList=SsoProvider.PROP_NAME)})
public class SsoProvider extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NAME = "name";
	
	@Column(nullable=false, unique=true)
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Group defaultGroup;
	
	@Lob
	@Column(nullable=false, length=65535)
	private SsoConnector connector;

	@OneToMany(mappedBy="provider", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<SsoAccount> accounts = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Group getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(Group defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public SsoConnector getConnector() {
		return connector;
	}

	public void setConnector(SsoConnector connector) {
		this.connector = connector;
	}
	
}
