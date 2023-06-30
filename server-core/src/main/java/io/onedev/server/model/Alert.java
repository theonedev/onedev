package io.onedev.server.model;

import io.onedev.server.annotation.Editable;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static io.onedev.server.model.Alert.PROP_DATE;
import static io.onedev.server.model.Alert.PROP_MESSAGE;

@Editable
@Entity
@Table(indexes={@Index(columnList=PROP_DATE), @Index(columnList=PROP_MESSAGE)})
public class Alert extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;

	public static final int MAX_MESSAGE_LEN = 255;

	public static final String PROP_DATE = "date";
	
	public static final String PROP_MESSAGE = "message";
	
	private Date date;

	@Column(nullable=false, unique = true, length=MAX_MESSAGE_LEN)
	private String message;
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = StringUtils.abbreviate(message, MAX_MESSAGE_LEN);
	}

	public static String getChangeObservable() {
		return Alert.class.getName();
	}
	
}
