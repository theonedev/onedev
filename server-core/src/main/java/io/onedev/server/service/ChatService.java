package io.onedev.server.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.Chat;
import io.onedev.server.model.ChatMessage;
import io.onedev.server.model.User;
import io.onedev.server.service.support.ChatResponding;

public interface ChatService extends EntityService<Chat> {

    List<Chat> query(User user, User ai, String term, int count);
		
	void createOrUpdate(Chat chat);

    void sendRequest(String sessionId, ChatMessage request);
	
    @Nullable
    ChatResponding getResponding(String sessionId, Chat chat);

}
