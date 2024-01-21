package io.onedev.server.mail;

class FileAttachment extends Attachment {

	public FileAttachment(String url, String fileName) {
		super(url, fileName);
	}

	@Override
	public String getMarkdown() {
		return String.format("[%s](%s)", fileName, url);
	}

}
