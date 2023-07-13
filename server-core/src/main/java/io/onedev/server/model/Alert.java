package io.onedev.server.model;

import io.onedev.server.annotation.Editable;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static io.onedev.server.model.Alert.PROP_DATE;
import static io.onedev.server.model.Alert.PROP_SUBJECT;

@Editable
@Entity
@Table(indexes={@Index(columnList=PROP_DATE), @Index(columnList= PROP_SUBJECT)})
public class Alert extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;

	public static final int MAX_SUBJECT_LEN = 255;
	
	public static final int MAX_DETAIL_LEN = 10000;

	public static final String PROP_DATE = "date";
	
	public static final String PROP_SUBJECT = "subject";
	
	private Date date;

	@Column(nullable=false, unique = true)
	private String subject;

	@Column(length= MAX_DETAIL_LEN)
	private String detail;
	
	private boolean mailError;
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = StringUtils.abbreviate(subject, MAX_SUBJECT_LEN);
	}

	@Nullable
	public String getDetail() {
		return detail;
	}

	public boolean isMailError() {
		return mailError;
	}

	public void setMailError(boolean mailError) {
		this.mailError = mailError;
	}

	public void setDetail(@Nullable String detail) {
		if (detail != null)
			this.detail = StringUtils.abbreviate(detail, MAX_DETAIL_LEN);
		else 
			this.detail = null;
	}

	public static String getChangeObservable() {
		return Alert.class.getName();
	}
	
}
