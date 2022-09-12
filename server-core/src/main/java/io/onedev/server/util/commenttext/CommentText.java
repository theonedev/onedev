package io.onedev.server.util.commenttext;

public abstract class CommentText {

	private final String content;
	
	public CommentText(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public abstract String getHtmlContent();

	public abstract String getPlainContent();
	
}
