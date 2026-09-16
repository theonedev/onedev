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
		indexes={@Index(columnList="user_id"), @Index(columnList="group_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"user_id", "group_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Membership extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_GROUP = "group";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Group group;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	
}
