package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
		indexes={@Index(columnList="work_id"), @Index(columnList="user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"work_id", "user_id", "emoji"})}
)
public class IssueWorkReaction extends EntityReaction {

	private static final long serialVersionUID = 1L;

	public static final String PROP_WORK = "work";

	@ManyToOne
	@JoinColumn(nullable=false)
	private IssueWork work;

	public IssueWork getWork() {
		return work;
	}

	public void setWork(IssueWork work) {
		this.work = work;
	}

	@Override
	protected AbstractEntity getEntity() {
		return work;
	}
	
}
