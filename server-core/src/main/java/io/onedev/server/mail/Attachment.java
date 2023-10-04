package io.onedev.server.mail;

abstract class Attachment {

	final String url;

	final String fileName;

	public Attachment(String url, String fileName) {
		this.url = url;
		this.fileName = fileName;
	}

	public abstract String getMarkdown();

}
