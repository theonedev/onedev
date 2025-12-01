package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(indexes={@Index(columnList="o_user_id"), @Index(columnList="o_ai_id")})
public class Chat extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	public static final int MAX_TITLE_LEN = 255;

	public static final String PROP_USER = "user";

	public static final String PROP_AI = "ai";
	
	public static final String PROP_DATE = "date";

	public static final String PROP_TITLE = "title";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User ai;

	@Column(nullable=false)
	private Date date;
	
	@Column(nullable=false, length=MAX_TITLE_LEN)
	private String title = "New chat";

	@OneToMany(mappedBy="chat", cascade=CascadeType.REMOVE)
	private Collection<ChatMessage> messages = new ArrayList<>();

	private transient List<ChatMessage> sortedMessages;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getAi() {
		return ai;
	}

	public void setAi(User ai) {
		this.ai = ai;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = StringUtils.abbreviate(title, MAX_TITLE_LEN);
	}

	public Collection<ChatMessage> getMessages() {
		return messages;
	}
	
	public void setMessages(Collection<ChatMessage> messages) {
		this.messages = messages;
	}

	public List<ChatMessage> getSortedMessages() {
		if (sortedMessages == null) {
			sortedMessages = new ArrayList<>(messages);
			sortedMessages.sort(Comparator.comparing(ChatMessage::getId));
		}
		return sortedMessages;
	}
	
	public static String getChangeObservable(Long chatId) {
		return Chat.class.getName() + ":" + chatId;
	}

	public static String getPartialResponseObservable(Long chatId) {
		return Chat.class.getName() + ":partialResponse:" + chatId;
	}

	public static String getNewMessagesObservable(Long chatId) {
		return Chat.class.getName() + ":newMessages:" + chatId;
	}

}
