package io.onedev.server.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Chat;
import io.onedev.server.model.ChatMessage;
import io.onedev.server.model.User;
import io.onedev.server.service.support.ChatResponding;
import io.onedev.server.service.support.ChatTool;
import io.onedev.server.web.WebSession;

public interface ChatService extends EntityService<Chat> {

    List<Chat> query(User user, User ai, String term, int count);
		
	void createOrUpdate(Chat chat);

    void sendRequest(WebSession session, ChatMessage request, List<ChatTool> tools, int timeoutSeconds);
	
    @Nullable
    ChatResponding getResponding(WebSession session, Chat chat);

    long nextAnonymousChatId();

    long nextAnonymousChatMessageId();

}
