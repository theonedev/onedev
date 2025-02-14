package io.onedev.server.model.support.widget;

import io.onedev.server.annotation.Editable;
import org.apache.wicket.Component;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public abstract class WidgetTab<T extends TabState> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String title;

	@Editable(order=10)
	@NotEmpty
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public abstract Component render(String componentId, TabStateSupport<T> tabStateSupport);
	
}
