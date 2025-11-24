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
		indexes={@Index(columnList="o_ai_id"), @Index(columnList="o_user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_ai_id", "o_user_id"})
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
