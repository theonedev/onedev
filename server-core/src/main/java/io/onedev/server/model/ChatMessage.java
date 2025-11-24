package io.onedev.server.model;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.commons.utils.StringUtils;

@Entity
@Table(indexes={@Index(columnList="o_chat_id")})
public class ChatMessage extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

    private static final int MAX_CONTENT_LEN = 100000;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Chat chat;

	private boolean error;

	private boolean request;

	@Lob
	@Column(nullable=false, length=MAX_CONTENT_LEN)
    private String content;

	@Lob
    @Column(nullable=false, length=65535)
    private LinkedHashMap<String, String> attachments = new LinkedHashMap<>();

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public String getContent() {
		return content;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isRequest() {
		return request;
	}

	public void setRequest(boolean request) {
		this.request = request;
	}
	
	public void setContent(String content) {
		this.content = StringUtils.abbreviate(content, MAX_CONTENT_LEN);
	}
	
	public LinkedHashMap<String, String> getAttachments() {
		return attachments;
	}

	public void setAttachments(LinkedHashMap<String, String> attachments) {
		this.attachments = attachments;
	}
	
}
