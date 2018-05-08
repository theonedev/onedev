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
		indexes={@Index(columnList="g_current_id"), @Index(columnList="g_other_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_current_id", "g_other_id"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class IssueRelation extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue current;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Issue other;

	public Issue getCurrent() {
		return current;
	}

	public void setCurrent(Issue current) {
		this.current = current;
	}

	public Issue getOther() {
		return other;
	}

	public void setOther(Issue other) {
		this.other = other;
	}
	
}
