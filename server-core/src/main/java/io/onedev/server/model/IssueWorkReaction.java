package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.onedev.server.model.support.EntityReaction;

@Entity
@Table(
		indexes={@Index(columnList="o_work_id"), @Index(columnList="o_user_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_work_id", "o_user_id", "emoji"})}
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
