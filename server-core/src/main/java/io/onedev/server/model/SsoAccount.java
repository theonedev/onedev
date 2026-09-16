package io.onedev.server.model;

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
		indexes={@Index(columnList="user_id"), @Index(columnList="provider_id"), @Index(columnList=SsoAccount.PROP_SUBJECT)},
		uniqueConstraints={@UniqueConstraint(columnNames={"provider_id", SsoAccount.PROP_SUBJECT})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class SsoAccount extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_PROVIDER = "provider";

	public static final String PROP_SUBJECT = "subject";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private SsoProvider provider;

	@Column(nullable=false)
	private String subject;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public SsoProvider getProvider() {
		return provider;
	}

	public void setProvider(SsoProvider provider) {
		this.provider = provider;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
