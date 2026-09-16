package io.onedev.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.onedev.commons.utils.StringUtils;

@Entity
@Table(indexes={@Index(columnList="chat_id")})
public class ChatMessage extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

    public static final int MAX_CONTENT_LEN = 100000;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Chat chat;

	private boolean error;

	private boolean request;

	@Column(nullable=false, length=MAX_CONTENT_LEN)
    private String content;

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
	
}
