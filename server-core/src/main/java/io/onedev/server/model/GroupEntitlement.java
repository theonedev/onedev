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
		indexes={@Index(columnList="ai_id"), @Index(columnList="group_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"ai_id", "group_id"})
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class GroupEntitlement extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_AI = "ai";
	
	public static final String PROP_GROUP = "group";

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User ai;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Group group;

	public User getAi() {
		return ai;
	}

	public void setAi(User ai) {
		this.ai = ai;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	
}
