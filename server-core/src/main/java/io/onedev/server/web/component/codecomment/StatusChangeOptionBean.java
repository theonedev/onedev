package io.onedev.server.web.component.codecomment;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(name="Confirm your action")
public class StatusChangeOptionBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String note;

	@Editable(placeholder="Leave a note")
	@Multiline
	@OmitName
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
}
