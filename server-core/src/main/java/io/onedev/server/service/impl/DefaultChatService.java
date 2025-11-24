package io.onedev.server.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.PartialToolCallContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.Chat;
import io.onedev.server.model.ChatMessage;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.ChatService;
import io.onedev.server.service.support.ChatResponding;
import io.onedev.server.web.SessionListener;
import io.onedev.server.web.websocket.WebSocketService;

@Singleton
public class DefaultChatService extends BaseEntityService<Chat> implements ChatService, SessionListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultChatService.class);
	
	private static final int MAX_HISTORY_MESSAGES = 25;

	private static final int MAX_HISTORY_MESSAGE_LEN = 1024;

	private static final int PARTIAL_RESPONSE_NOTIFICATION_INTERVAL = 1000;

	@Inject
	private ExecutorService executorService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private WebSocketService webSocketService;

	private final Map<String, Map<Long, ChatRespondingImpl>> respondings = new ConcurrentHashMap<>();
	
	@Override
	public List<Chat> query(User user, User ai, String term, int count) {
		EntityCriteria<Chat> criteria = EntityCriteria.of(Chat.class);
		criteria.add(Restrictions.eq(Chat.PROP_USER, user));
		criteria.add(Restrictions.eq(Chat.PROP_AI, ai));
		criteria.add(Restrictions.ilike(Chat.PROP_TITLE, "%" + term + "%"));
		criteria.addOrder(Order.desc(Chat.PROP_DATE));
		return query(criteria);
	}

	@Override
	public void createOrUpdate(Chat chat) {
		dao.persist(chat);
	}

	@Sessional
	@Override
	public ChatResponding getResponding(String sessionId, Chat chat) {
		return getResponding(sessionId, chat.getId());
	}

	@Transactional
	@Override
	public void sendRequest(String sessionId, ChatMessage request) {
		var requestId = request.getId();
		var chatId = request.getChat().getId();
		var modelSetting = request.getChat().getAi().getAiSetting().getModelSetting();
		var messages = request.getChat().getSortedMessages()
			.stream()
			.filter(it->!it.isError())
			.collect(Collectors.toList());
		
		if (messages.size() > MAX_HISTORY_MESSAGES)
			messages = messages.subList(messages.size()-MAX_HISTORY_MESSAGES, messages.size());
		
		var langchain4jMessages = messages.stream()
			.map(it -> {
				var content = it.getContent();
				if (!it.equals(request))
					content = StringUtils.abbreviate(content, MAX_HISTORY_MESSAGE_LEN);
				if (it.isRequest())
					return new UserMessage(content);
				else
					return new AiMessage(content);
			})
			.collect(Collectors.toList());

		var future = executorService.submit(() -> {
			var latch = new CountDownLatch(1);
			var handler = new StreamingChatResponseHandler() {

				private long lastPartialResponseNotificationTime = System.currentTimeMillis();

				@Override
				public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {					
					if (latch.getCount() == 0) {
						context.streamingHandle().cancel();
					} else {
						var responding = getResponding(sessionId, chatId, requestId);
						if (responding != null) {
							var content = responding.getContent();
							if (content == null)
								content = "";
							content += partialResponse.text();
							responding.content = content;
							if (System.currentTimeMillis() - lastPartialResponseNotificationTime > PARTIAL_RESPONSE_NOTIFICATION_INTERVAL) {
								lastPartialResponseNotificationTime = System.currentTimeMillis();
								webSocketService.notifyObservableChange(Chat.getPartialResponseObservable(chatId), null);
							}
						}		
					}
				}				
				
				@Override
				public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {				
					if (latch.getCount() == 0)
						context.streamingHandle().cancel();
				}

				@Override
			 	public void onPartialToolCall(PartialToolCall partialToolCall, PartialToolCallContext context) {
					if (latch.getCount() == 0)
						context.streamingHandle().cancel();
			   	}

				@Override
				public void onCompleteResponse(ChatResponse completeResponse) {	
					try {
						createResponseIfNecessary(sessionId, chatId, requestId, completeResponse.aiMessage().text(), null);												
					} finally {
						latch.countDown();													
					}
				}

				@Override
				public void onError(Throwable error) {
					try {
						createResponseIfNecessary(sessionId, chatId, requestId, "Error getting chat response, check server log for details", error);
					} finally {
						latch.countDown();
					}
				}
				
			};
			try {
				modelSetting.getStreamingChatModel().chat(langchain4jMessages, handler);
				transactionService.run(() -> {
					var chat = load(chatId);
					var requests = chat.getMessages().stream().filter(it->it.isRequest()).collect(Collectors.toList());
					if (requests.size() == 1) {
						var systemPrompt = String.format("""
							Summarize provided message to get a compact title with below requirements:
							1. It should be within %d characters
							2. Only title is returned, no other text or comments
						""", Chat.MAX_TITLE_LEN);
						var title = modelSetting.getChatModel().chat(new SystemMessage(systemPrompt), new UserMessage(requests.get(0).getContent())).aiMessage().text();
						chat.setTitle(title);
						webSocketService.notifyObservableChange(Chat.getChangeObservable(chatId), null);
					}
				});				
				latch.await();	
			} catch (Exception e) {			
				if (ExceptionUtils.find(e, InterruptedException.class) == null) {
					logger.error("Error getting chat response", e);
					String errorMessage = e.getMessage();
					if (errorMessage == null)
						errorMessage = "Error getting chat response, check server log for details";
					createResponseIfNecessary(sessionId, chatId, requestId, errorMessage, e);
				} else {
					var responding = getResponding(sessionId, chatId, requestId);
					if (responding != null && responding.getContent() != null) 
						createResponseIfNecessary(sessionId, chatId, requestId, responding.getContent(), null);
				}
			} finally {
				latch.countDown();
				var respondingsOfSession = respondings.get(sessionId);				
				if (respondingsOfSession != null) {
					var responding = respondingsOfSession.get(chatId);
					if (responding != null) {
						respondingsOfSession.computeIfPresent(chatId, (k, v)-> {
							if (v.requestId.equals(requestId)) 
								return null;
							else
								return v;
						});
						webSocketService.notifyObservableChange(Chat.getPartialResponseObservable(chatId), null);
					}
				}
			}
		});

		var previousResponding = respondings.computeIfAbsent(sessionId, it->new ConcurrentHashMap<>()).put(chatId, new ChatRespondingImpl(requestId, future));
		if (previousResponding != null) 
			previousResponding.cancel();
	}

	private ChatRespondingImpl getResponding(String sessionId, Long chatId) {
		var respondingsOfSession = respondings.get(sessionId);
		if (respondingsOfSession != null) {
			return respondingsOfSession.get(chatId);
		}
		return null;
	}

	private ChatRespondingImpl getResponding(String sessionId, Long chatId, Long requestId) {
		var responding = getResponding(sessionId, chatId);
		if (responding != null && responding.requestId.equals(requestId)) 
			return responding;
		else
			return null;
	}

	private void createResponseIfNecessary(String sessionId, Long chatId, Long requestId, String content, @Nullable Throwable throwable) {
		var responding = getResponding(sessionId, chatId, requestId);
		if (responding != null) {
			transactionService.run(() -> {
				var chat = load(chatId);			
				if (throwable != null)
					logger.error("Error getting chat response", throwable);
				var response = new ChatMessage();
				response.setChat(chat);
				response.setError(throwable != null);
				response.setContent(content);
				dao.persist(response);
				webSocketService.notifyObservableChange(Chat.getNewMessagesObservable(chatId), null);
			});
		};
	}

	@Override
	public void sessionCreated(String sessionId) {
	}

	@Override
	public void sessionDestroyed(String sessionId) {
		var respondingsOfSession = respondings.remove(sessionId);
		if (respondingsOfSession != null) {
			for (var responding : respondingsOfSession.values())
				responding.cancel();
		}
	}

	private static class ChatRespondingImpl implements ChatResponding {

		private final Long requestId;

		private final Future<?> future;
		
		private volatile String content;

		ChatRespondingImpl(Long requestId, Future<?> future) {
			this.requestId = requestId;
			this.future = future;
		}
		
		@Override
		public String getContent() {
			return content;
		}

		@Override
		public void cancel() {
			future.cancel(true);
		}

	}
}