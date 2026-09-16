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
		indexes={@Index(columnList="ai_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"ai_id", "user_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class UserEntitlement extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_AI = "ai";
	
	public static final String PROP_USER = "user";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User ai;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	public User getAI() {
		return ai;
	}

	public void setAI(User ai) {
		this.ai = ai;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
