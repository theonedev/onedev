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
		indexes={@Index(columnList="o_ai_id"), @Index(columnList="o_group_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_ai_id", "o_group_id"})
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
