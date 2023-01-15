package io.onedev.server.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static io.onedev.server.model.ProjectUpdate.PROP_DATE;

@Entity
@Table(indexes={@Index(columnList=PROP_DATE)})
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class ProjectUpdate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROP_DATE = "date";
	
	@Column(nullable=false)
	private Date date = new Date();

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
