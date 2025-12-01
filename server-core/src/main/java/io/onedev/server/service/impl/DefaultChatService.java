package io.onedev.server.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.cp.IAtomicLong;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.PartialToolCallContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.model.Chat;
import io.onedev.server.model.ChatMessage;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ChatService;
import io.onedev.server.service.support.ChatResponding;
import io.onedev.server.service.support.ChatTool;
import io.onedev.server.web.SessionListener;
import io.onedev.server.web.WebSession;
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

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private SessionService sessionService;

	@Inject
	private ClusterService clusterService;

	/*
	 * Use cluster wide id for anonymous chats and messages as we use id to route 
	 * websocket observable changes to correct connections
	 */
	private volatile IAtomicLong nextAnonymousChatId;

	private volatile IAtomicLong nextAnonymousChatMessageId;

	private final Map<String, Map<Long, ChatRespondingImpl>> respondings = new ConcurrentHashMap<>();
	
	@Override
	public List<Chat> query(User user, User ai, String term, int count) {
		EntityCriteria<Chat> criteria = EntityCriteria.of(Chat.class);
		criteria.add(Restrictions.eq(Chat.PROP_USER, user));
		criteria.add(Restrictions.eq(Chat.PROP_AI, ai));
		criteria.add(Restrictions.ilike(Chat.PROP_TITLE, "%" + term + "%"));
		criteria.addOrder(Order.desc(Chat.PROP_ID));
		return query(criteria);
	}

	@Override
	public void createOrUpdate(Chat chat) {
		dao.persist(chat);
	}

	@Sessional
	@Override
	public ChatResponding getResponding(WebSession session, Chat chat) {
		return getResponding(session.getId(), chat.getId());
	}

	@Transactional
	@Override
	public void sendRequest(WebSession session, ChatMessage request, List<ChatTool> tools, int timeoutSeconds) {
		var sessionId = session.getId();
		var requestId = request.getId();
		var chatId = request.getChat().getId();
		var anonymous = request.getChat().getUser() == null;

		var modelSetting = request.getChat().getAi().getAiSetting().getModelSetting();
		var streamingChatModel = modelSetting.getStreamingChatModel();
		var chatModel = modelSetting.getChatModel();
		
		var messages = request.getChat().getSortedMessages()
			.stream()
			.filter(it->!it.isError())
			.collect(Collectors.toList());
		
		if (messages.size() > MAX_HISTORY_MESSAGES)
			messages = messages.subList(messages.size()-MAX_HISTORY_MESSAGES, messages.size());
		
		var langchain4jMessages = new ArrayList<dev.langchain4j.data.message.ChatMessage>();
		langchain4jMessages.addAll(messages.stream()
			.map(it -> {
				var content = it.getContent();
				if (!it.equals(request))
					content = StringUtils.abbreviate(content, MAX_HISTORY_MESSAGE_LEN);
				if (it.isRequest())
					return (dev.langchain4j.data.message.ChatMessage) new UserMessage(content);
				else
					return (dev.langchain4j.data.message.ChatMessage) new AiMessage(content);
			})
			.collect(Collectors.toList()));

		var toolSpecifications = tools.stream()
			.map(ChatTool::getSpecification)
			.collect(Collectors.toList());
		var completableFuture = new CompletableFuture<String>();
		var executableFuture = executorService.submit(new Runnable() {

			private StreamingChatResponseHandler newResponseHandler(Subject subject) {
				return new StreamingChatResponseHandler() {

					private long lastPartialResponseNotificationTime = System.currentTimeMillis();

					@Override
					public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {	
						ThreadContext.bind(subject);				
						if (completableFuture.isDone()) {
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
						ThreadContext.bind(subject);				
						if (completableFuture.isDone())
							context.streamingHandle().cancel();
					}

					@Override
					public void onPartialToolCall(PartialToolCall partialToolCall, PartialToolCallContext context) {
						ThreadContext.bind(subject);				
						if (completableFuture.isDone()) {
							context.streamingHandle().cancel();
						}
					}

					@Override
					public void onCompleteResponse(ChatResponse completeResponse) {	
						ThreadContext.bind(subject);
						sessionService.runAsync(() -> {
							try {
								var aiMessage = completeResponse.aiMessage();
								if (aiMessage.hasToolExecutionRequests()) {
									langchain4jMessages.add(aiMessage);
									var toolRequests = aiMessage.toolExecutionRequests();
									for (int i = 0; i < toolRequests.size(); i++) {
										var toolRequest = toolRequests.get(i);
										String toolName = toolRequest.name();
										String toolArgs = toolRequest.arguments();
										var toolResult = tools.stream()
												.filter(it->it.getSpecification().name().equals(toolName))
												.findFirst()
												.orElseThrow(() -> new ExplicitException("Unknown tool: " + toolName))
												.execute(objectMapper.readTree(toolArgs));														
										ToolExecutionResultMessage toolResultMessage = ToolExecutionResultMessage.from(
											toolRequest.id(), toolName, toolResult);
										langchain4jMessages.add(toolResultMessage);
									}
									
									var handler = newResponseHandler(SecurityUtils.getSubject());
									var langchain4jChatRequest = ChatRequest.builder()
											.messages(new ArrayList<>(langchain4jMessages))
											.toolSpecifications(toolSpecifications)
											.build();							
									streamingChatModel.chat(langchain4jChatRequest, handler);
								} else {
									completableFuture.complete(aiMessage.text());
								}	
							} catch (Throwable t) {
								completableFuture.completeExceptionally(t);
							}
						});
					}

					@Override
					public void onError(Throwable error) {
						ThreadContext.bind(subject);				
						completableFuture.completeExceptionally(error);
					}

				};		
			}

			private void createResponse(String content, @Nullable Throwable throwable) {
				var responding = getResponding(session.getId(), chatId, requestId);
				if (responding != null) {
					transactionService.run(() -> {
						if (throwable != null)
							logger.error("Error getting chat response", throwable);
						var response = new ChatMessage();
						response.setError(throwable != null);
						response.setContent(content);
						if (anonymous) {
							var chat = SerializationUtils.clone(checkNotNull(session.getAnonymousChats().get(chatId)));
							response.setChat(chat);
							response.setId(nextAnonymousChatMessageId());
							chat.getMessages().add(response);
							session.getAnonymousChats().put(chatId, chat);
						} else {
							response.setChat(load(chatId));
							dao.persist(response);
						}
						webSocketService.notifyObservableChange(Chat.getNewMessagesObservable(chatId), null);
					});
				};
			}	

			private void createIncompleteResponse(String reason) {
				var responding = getResponding(sessionId, chatId, requestId);
				if (responding != null) { 
					var content = responding.getContent();
					if (content == null)
						content = "";
					else
						content += "\n\n";							
					content += "<b class='text text-danger'>" + reason + "</b>";
					createResponse(content, null);
				}
			}

			@Override
			public void run() {
				var handler = newResponseHandler(SecurityUtils.getSubject());
				try {
					var langchain4jChatRequest = ChatRequest.builder()
						.messages(langchain4jMessages.stream()
							.map(msg -> (dev.langchain4j.data.message.ChatMessage) msg)
							.collect(Collectors.toList()))
						.toolSpecifications(toolSpecifications)
						.build();
					
					streamingChatModel.chat(langchain4jChatRequest, handler);
					transactionService.run(() -> {
						var chat = anonymous ? checkNotNull(session.getAnonymousChats().get(chatId)) : load(chatId);
						var requests = chat.getMessages().stream().filter(it->it.isRequest()).collect(Collectors.toList());
						if (requests.size() == 1) {
							var systemPrompt = String.format("""
								Summarize provided message to get a compact title with below requirements:
								1. Just summarize the message itself, no need to ask user for clarification or confirmation
								2. It should be within %d characters
								3. Only title is returned, no other text or comments
								4. Display in %s
								""", Chat.MAX_TITLE_LEN, session.getLocale().getDisplayLanguage());
							var title = chatModel.chat(new SystemMessage(systemPrompt), new UserMessage(requests.get(0).getContent())).aiMessage().text();
							if (anonymous) {
								chat = SerializationUtils.clone(chat);
								chat.setTitle(title);
								session.getAnonymousChats().put(chatId, chat);
							} else {
								chat.setTitle(title);
								dao.persist(chat);
							}
							webSocketService.notifyObservableChange(Chat.getChangeObservable(chatId), null);
						}
					});		
					var response = completableFuture.get(timeoutSeconds, TimeUnit.SECONDS);
					if (StringUtils.isBlank(response))
						throw new ExplicitException("Received empty response");		
					createResponse(response, null);
				} catch (Throwable e) {
					if (ExceptionUtils.find(e, InterruptedException.class) != null) {
						createIncompleteResponse("Conversation cancelled");
					} else if (ExceptionUtils.find(e, TimeoutException.class) != null) {
						createIncompleteResponse("Conversation timed out");
					} else {
						logger.error("Error getting chat response", e);
						String errorMessage = e.getMessage();
						if (errorMessage == null)
							errorMessage = "Error getting chat response, check server log for details";
						createResponse(errorMessage, e);
					}
				} finally {
					completableFuture.cancel(false);
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
			}
		});

		var previousResponding = respondings.computeIfAbsent(sessionId, it->new ConcurrentHashMap<>()).put(chatId, new ChatRespondingImpl(requestId, executableFuture));
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

	@Listen
	public void on(SystemStarting event) {
		nextAnonymousChatId = clusterService.getHazelcastInstance().getCPSubsystem().getAtomicLong("nextAnonymousChatId");
		nextAnonymousChatMessageId = clusterService.getHazelcastInstance().getCPSubsystem().getAtomicLong("nextAnonymousChatMessageId");
		if (clusterService.isLeaderServer()) {
			nextAnonymousChatId.set(1L);
			nextAnonymousChatMessageId.set(1L);
		}		
	}

	@Override
	public long nextAnonymousChatId() {
		return nextAnonymousChatId.getAndIncrement();
	}

	@Override
	public long nextAnonymousChatMessageId() {
		return nextAnonymousChatMessageId.getAndIncrement();
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