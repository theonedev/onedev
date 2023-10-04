package io.onedev.server.web.component.taskbutton;

import org.unbescape.html.HtmlEscape;

import java.io.Serializable;

public class TaskResult implements Serializable {
	
	private static final long serialVersionUID = 1L;

	final boolean successful;
	
	private final Message message;
	
	public TaskResult(boolean successful, Message message) {
		this.successful = successful;
		this.message = message;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public String getFeedback() {
		return String.format(
				"<div class='task-result alert-notice text-break alert %s'>%s</div>",
				successful? "alert-light-success": "alert-light-danger", message.getHtmlText());
	}

	public static interface Message extends Serializable {
		
		String getHtmlText();
	}
	
	public static class PlainMessage implements Message {

		private static final long serialVersionUID = 1L;
		
		private String text;

		public PlainMessage(String text) {
			this.text = text;
		}

		@Override
		public String getHtmlText() {
			return HtmlEscape.escapeHtml5(text);
		}
	}
	
	public static class HtmlMessgae implements Message {

		private static final long serialVersionUID = 1L;

		private String text;

		public HtmlMessgae(String text) {
			this.text = text;
		}

		@Override
		public String getHtmlText() {
			return text;
		}
		
	}
}