package io.onedev.server.model.support;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.annotation.Editable;

@Editable
public abstract class Widget implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Widget.class);

	private String title;
	
	private int left;
	
	private int top;
	
	private int right;
	
	private int bottom;
	
	@Editable(order=10)
	@NotEmpty
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
	
	public int getDefaultWidth() {
		return 16;
	}
	
	public int getDefaultHeight() {
		return 8;
	}

	public Component render(String componentId, boolean failsafe) {
		if (failsafe) {
			return new Label(componentId, "Not rendered in failsafe mode")
					.add(AttributeAppender.append("class", "text-info font-italic"));
		} else {
			try {
				return doRender(componentId);
			} catch (Exception e) {
				logger.error("Error rendering widget '" + getTitle() + "'", e);
				ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
				
				String message;
				if (explicitException != null) 
					message = HtmlEscape.escapeHtml5(explicitException.getMessage());
				else
					message = "Error rendering widget, check server log for details";
				
				message = "<div class='alert alert-notice alert-light-danger mb-0'>" + message + "</div>";
				
				return new Label(componentId, message).setEscapeModelStrings(false);
			}
		}
	}
	
	protected abstract Component doRender(String componentId);
	
	public boolean isIntersectedWith(Widget widget) {
		return !(widget.left >= right || widget.right <= left || widget.top >= bottom || widget.bottom <= top);		
	}
	
}
