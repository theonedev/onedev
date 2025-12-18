package io.onedev.server.service;

import java.util.List;

import org.apache.wicket.Page;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Chat;
import io.onedev.server.model.ChatMessage;
import io.onedev.server.model.User;
import io.onedev.server.service.support.ChatResponding;
import io.onedev.server.web.WebSession;

public interface ChatService extends EntityService<Chat> {

    List<Chat> query(User user, User ai, String term, int count);
		
	void createOrUpdate(Chat chat);

    /**
     * Send a request to the chat service.
     * 
     * @param page the page that the request is sent from
     * @param request the request to send
     * @param timeout the timeout in seconds
     */
    void sendRequest(Page page, ChatMessage request, int timeout);
	
    @Nullable
    ChatResponding getResponding(WebSession session, Chat chat);

    long nextAnonymousChatId();

    long nextAnonymousChatMessageId();

}
