package io.onedev.server.ai;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
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
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cp.IAtomicLong;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
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
import dev.langchain4j.model.chat.response.StreamingHandle;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopping;
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
import io.onedev.server.service.SettingService;
import io.onedev.server.service.impl.BaseEntityService;
import io.onedev.server.service.support.ChatResponding;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.ai.tools.TaskComplete;
import io.onedev.server.web.SessionListener;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.websocket.AiToolExecution;
import io.onedev.server.web.websocket.WebSocketService;

@Singleton
public class DefaultChatService extends BaseEntityService<Chat> implements ChatService, SessionListener, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultChatService.class);
	
	private static final int TIMEOUT_SECONDS = 600;

	private static final int MAX_HISTORY_MESSAGES = 25;

	private static final int MAX_HISTORY_MESSAGE_LEN = 1024;

	private static final int HOUSE_KEEPING_PRIORITY = 50;

	@Inject
	private ExecutorService executorService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private WebSocketService webSocketService;

	@Inject
	private SessionService sessionService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private Application application;

	@Inject
	private SettingService settingService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;

	private volatile String taskId;

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
	public void sendRequest(Page page, ChatMessage request) {
		var toolSpecifications = ((BasePage) page).findChatTools()
			.stream()
			.map(ChatTool::getSpecification)
			.collect(Collectors.toList());
		
		// Add the taskComplete tool - this must always be present
		toolSpecifications.add(TaskComplete.getSpecification());

		ToolUtils.filterDuplications(toolSpecifications);

		WebSession session = (WebSession) page.getSession();
		var sessionId = page.getSession().getId();
		var pageKey = new PageIdKey(page.getPageId());
		
		var requestId = request.getId();
		var chatId = request.getChat().getId();
		var anonymous = request.getChat().getUser() == null;

		var aiSetting = request.getChat().getAi().getAiSetting();
		var modelSetting = aiSetting.getModelSetting();
		var streamingChatModel = modelSetting.getStreamingChatModel();
		var chatModel = modelSetting.getChatModel();
		
		var messages = request.getChat().getSortedMessages()
			.stream()
			.collect(Collectors.toList());
		
		if (messages.size() > MAX_HISTORY_MESSAGES)
			messages = messages.subList(messages.size()-MAX_HISTORY_MESSAGES, messages.size());
		
		var langchain4jMessages = new ArrayList<dev.langchain4j.data.message.ChatMessage>();
		langchain4jMessages.add(new SystemMessage("Your name is %s".formatted(request.getChat().getAi().getName())));
		var systemPrompt = request.getChat().getAi().getAiSetting().getSystemPrompt();
		if (systemPrompt != null)
			langchain4jMessages.add(new SystemMessage(systemPrompt));
		langchain4jMessages.add(new SystemMessage(TaskComplete.SYSTEM_MESSAGE));
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

		var responseFuture = new CompletableFuture<String>();
		var executionFuture = executorService.submit(new Runnable() {

			private int noToolRetryCount = 0;

			private StreamingChatResponseHandler newResponseHandler(Subject subject) {
				return new StreamingChatResponseHandler() {

					private final Set<String> loggedToolCalls = new HashSet<>();

					private void updateRespondingContent(StreamingHandle streamingHandle, String partialContent) {
						ThreadContext.bind(subject);				
						if (responseFuture.isDone()) {
							streamingHandle.cancel();
						} else {
							var responding = getResponding(sessionId, chatId, requestId);
							if (responding != null) {
								var content = responding.getContent();
								if (content == null)
									content = "";
								content += partialContent;
								responding.content = content;
								webSocketService.notifyObservableChange(Chat.getPartialResponseObservable(chatId), null);
							}		
						}
					}

					@Override
					public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {	
						updateRespondingContent(context.streamingHandle(), partialResponse.text());
					}				
					
					@Override
					public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {				
						updateRespondingContent(context.streamingHandle(), partialThinking.text());
					}

					@Override
					public void onPartialToolCall(PartialToolCall partialToolCall, PartialToolCallContext context) {
						ThreadContext.bind(subject);				
						if (responseFuture.isDone()) {
							context.streamingHandle().cancel();
						} else {
							var toolCallId = partialToolCall.id();
							if (toolCallId != null && loggedToolCalls.add(toolCallId)) {
								var responding = getResponding(sessionId, chatId, requestId);
								if (responding != null) {
									var content = responding.getContent();
									if (content == null)
										content = "";
									else 
										content += "\n\n";
									
									content += "<div class='text-muted'>Calling tool \"" + partialToolCall.name() + "\"...</div>\n\n";
									
									responding.content = content;
									webSocketService.notifyObservableChange(Chat.getPartialResponseObservable(chatId), null);
								}
							}
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
									var connectionRegistry = WebSocketSettings.Holder.get(application).getConnectionRegistry();
									var connection = connectionRegistry.getConnection(application, sessionId, pageKey);
									if (connection == null || !connection.isOpen())
										throw new ExplicitException("Conversation context lost");
									for (var toolRequest: toolRequests) {
										if (responseFuture.isDone())										
											return;
										String toolName = toolRequest.name();
										
										// Handle taskComplete specially - it signals conversation completion
										var taskCompleteResponse = TaskComplete.getResponse(toolRequest);
										if (taskCompleteResponse != null) {
											responseFuture.complete(taskCompleteResponse);
											return;
										}
										
										try {
											var toolExecution = new AiToolExecution(toolName, ToolUtils.getToolArguments(toolRequest));
											connection.sendMessage(toolExecution);
											var toolExecutionFuture = toolExecution.getFuture();
											if (toolExecutionFuture == null)
												throw new ExplicitException("Tool not found: " + toolName);
											while (true) {
												try {
													var toolExecutionResult = toolExecutionFuture.get(1, TimeUnit.SECONDS);
													toolExecutionResult.addToMessages(langchain4jMessages, toolRequest);
													break;
												} catch (TimeoutException e) {
													if (responseFuture.isDone())
														return;
												} catch (CancellationException e) {
													// may get cancelled in DefaultManagedFutureService
													throw new ExplicitException("Timed out calling tool: " + toolName);
												}
											}
										} catch (Throwable t) {
											ToolUtils.handleCallException(toolName, t);
										}
									}
									
									if (responseFuture.isDone())
										return;
									
									var handler = newResponseHandler(SecurityUtils.getSubject());
									var langchain4jChatRequest = ChatRequest.builder()
											.messages(new ArrayList<>(langchain4jMessages))
											.toolSpecifications(toolSpecifications)
											.build();							
									streamingChatModel.chat(langchain4jChatRequest, handler);
								} else {
									// LLM didn't call any tool - prompt it to continue or call taskComplete
									noToolRetryCount++;
									if (noToolRetryCount <= TaskComplete.MAX_NO_TOOL_RETRIES) {
										var text = aiMessage.text();
										if (StringUtils.isNotBlank(text)) {
											langchain4jMessages.add(aiMessage);
										}
										langchain4jMessages.add(new UserMessage(TaskComplete.CONTINUATION_PROMPT));
										
										var handler = newResponseHandler(SecurityUtils.getSubject());
										var langchain4jChatRequest = ChatRequest.builder()
												.messages(new ArrayList<>(langchain4jMessages))
												.toolSpecifications(toolSpecifications)
												.build();
										streamingChatModel.chat(langchain4jChatRequest, handler);
									} else {
										// Max retries exceeded, accept whatever response we have
										responseFuture.complete(aiMessage.text());
									}
								}	
							} catch (Throwable t) {
								responseFuture.completeExceptionally(t);
							}
						});
					}

					@Override
					public void onError(Throwable error) {
						ThreadContext.bind(subject);				
						responseFuture.completeExceptionally(error);
					}

				};		
			}

			private void createResponse(String content, boolean error) {
				var responding = getResponding(sessionId, chatId, requestId);
				if (responding != null) {
					transactionService.run(() -> {
						var response = new ChatMessage();
						response.setError(error);
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
					createResponse(content, false);
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
							var prompt = "Summarize below message as a compact title (only title, no comments or other text): \n\n" + requests.get(0).getContent();
							var title = chatModel.chat(new UserMessage(prompt)).aiMessage().text();
							if (StringUtils.isNotBlank(title)) {
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
						}
					});		
					var response = responseFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
					if (StringUtils.isBlank(response))
						throw new ExplicitException("Received empty response");		
					createResponse(response, false);
				} catch (Throwable e) {
					if (ExceptionUtils.find(e, InterruptedException.class) != null) {
						createIncompleteResponse("Conversation cancelled");
					} else if (ExceptionUtils.find(e, TimeoutException.class) != null) {
						createIncompleteResponse("Conversation timed out");
					} else {
						var explicitException = ExceptionUtils.find(e, ExplicitException.class);
						if (explicitException != null) {
							createResponse(explicitException.getMessage(), true);
						} else {
							createResponse("Error getting chat response, check server log for details", true);
							logger.error("Error getting chat response", e);
						}
					}
				} finally {
					responseFuture.cancel(false);
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

		var previousResponding = respondings.computeIfAbsent(sessionId, it->new ConcurrentHashMap<>()).put(chatId, new ChatRespondingImpl(requestId, executionFuture));
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

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
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

	@Sessional
	@Override
	public void execute() {
		batchWorkExecutionService.submit(new BatchWorker("chat-cleanup") {

			@Override
			public void doWorks(List<Prioritized> works) {
				transactionService.run(() -> {
					var preserveDays = settingService.getAiSetting().getChatPreserveDays();
					var threshold = Date.from(Instant.now().minus(preserveDays, ChronoUnit.DAYS));
					var criteria = newCriteria();
					criteria.add(Restrictions.lt(Chat.PROP_DATE, threshold));
					var deletedCount = 0;
					for (var chat: query(criteria)) {
						delete(chat);
						deletedCount++;
					}
					if (deletedCount > 0)
						logger.info("Deleted {} chat(s) older than {} days", deletedCount, preserveDays);
				});
			}
			
		}, new Prioritized(HOUSE_KEEPING_PRIORITY));
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
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