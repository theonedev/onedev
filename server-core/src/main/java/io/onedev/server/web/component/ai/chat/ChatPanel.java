package io.onedev.server.web.component.ai.chat;

import static io.onedev.server.security.SecurityUtils.getUser;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.json.JSONException;
import org.json.JSONWriter;
import org.jspecify.annotations.Nullable;

import io.onedev.server.ai.ChatService;
import io.onedev.server.model.Chat;
import io.onedev.server.model.ChatMessage;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.service.UserService;
import io.onedev.server.service.support.ChatResponding;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.DisplayNoneBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.select2.Select2Choice;
import io.onedev.server.web.component.user.UserAvatar;

public class ChatPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private static final String COOKIE_ACTIVE_AI = "active-ai";

	@Inject
	private UserService userService;

	@Inject
	private ChatService chatService;

	@Inject
	private Dao dao;

	private Long activeAiId;

	private RepeatingView messagesView;

	private WebMarkupContainer respondingContainer;

	private final IModel<List<User>> entitledAisModel = new LoadableDetachableModel<List<User>>() {

		@Override
		protected List<User> load() {
			if (getUser() != null) {
				return getUser().getEntitledAis();
			} else {
				return userService.cloneCache().values().stream()
						.filter(it -> it.getType() == User.Type.AI && !it.isDisabled() && it.isEntitleToAll())
						.sorted(Comparator.comparing(UserFacade::getDisplayName))
						.map(it->userService.load(it.getId()))
						.collect(Collectors.toList());
			}
		}

	};
			
	public ChatPanel(String componentId) {
		super(componentId);

		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_ACTIVE_AI);
		if (cookie != null) 
			activeAiId = Long.valueOf(cookie.getValue());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
					
		var aiSelector = new MenuLink("aiSelector") {
			
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				var activeAI = getActiveAI();
				var menuItems = new ArrayList<MenuItem>();
				for (var ai : getEntitledAis()) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return ai.getDisplayName();
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new AjaxLink<Void>(id) {

								@Override
								public void onClick(AjaxRequestTarget target) {
									setActiveAI(ai);
									WebSession.get().setActiveChatId(null);
									target.add(ChatPanel.this);									
								}
								
							};
						}
						
						public boolean isSelected() {
							return ai.equals(activeAI);
						}

					});
				}
				return menuItems;
			}
			
			@Override
			protected void onBeforeRender() {
				addOrReplace(new UserAvatar("avatar", getActiveAI()));
				addOrReplace(new Label("name", getActiveAI().getDisplayName()));
				super.onBeforeRender();
			}
		};
		add(aiSelector);

		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				hide(target);
			}
			
		});				

		var chatSelectorContainer = new WebMarkupContainer("chatSelectorContainer");
		chatSelectorContainer.setOutputMarkupId(true);		
		chatSelectorContainer.add(new Select2Choice<Chat>("chatSelector", new IModel<Chat>() {

			@Override
			public void detach() {
			}

			@Override
			public Chat getObject() {
				return getActiveChat();
			}

			@Override
			public void setObject(Chat object) {
				WebSession.get().setActiveChatId(object.getId());
			}

		}, new ChoiceProvider<Chat>() {

			@Override
			public void query(String term, int page, io.onedev.server.web.component.select2.Response<Chat> response) {
				var count = (page+1) * WebConstants.PAGE_SIZE + 1;
				List<Chat> chats;
				if (getUser() != null) {
					chats = chatService.query(getUser(), getActiveAI(), term, count);
				} else {
					chats = WebSession.get().getAnonymousChats().values().stream()
						.filter(it -> it.getAi().equals(getActiveAI()) && it.getTitle().toLowerCase().contains(term.toLowerCase()))
						.sorted(Comparator.comparing(Chat::getId).reversed())
						.limit(count)
						.collect(Collectors.toList());
				}
				new ResponseFiller<>(response).fill(chats, page, WebConstants.PAGE_SIZE);
			}

			@Override
			public void toJson(Chat choice, JSONWriter writer) throws JSONException {
				writer.key("id").value(choice.getId()).key("text").value(choice.getTitle());
			}

			@Override
			public Collection<Chat> toChoices(Collection<String> ids) {
				return ids.stream().map(it->chatService.load(Long.valueOf(it))).collect(Collectors.toList());
			}
			
		}).add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(ChatPanel.this);
			}

		}));
		chatSelectorContainer.add(new ChangeObserver() {

			@Override
			protected Collection<String> findObservables() {
				var chat = getActiveChat();
				if (chat != null)
					return Collections.singleton(Chat.getChangeObservable(chat.getId()));
				else
					return Collections.emptySet();
			}

		});
		chatSelectorContainer.add(new ChangeObserver() {

			@Override
			protected Collection<String> findObservables() {
				var chat = getActiveChat();
				if (chat != null)
					return Collections.singleton(Chat.getNewMessagesObservable(chat.getId()));
				else
					return Collections.emptySet();
			}

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				if (isVisible())
					showNewMessages(handler);
			}

		});

		chatSelectorContainer.add(new AjaxLink<Void>("newChat") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				WebSession.get().setActiveChatId(null);
				target.add(ChatPanel.this);
			}

		});
		chatSelectorContainer.add(new AjaxLink<Void>("deleteChat") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getSession().success(_T("Chat deleted"));
				if (getUser() != null) 
					chatService.delete(getActiveChat());
				else
					WebSession.get().getAnonymousChats().remove(getActiveChat().getId());
				WebSession.get().setActiveChatId(null);
				target.add(ChatPanel.this);
			}

			@Override
			protected void onConfigure() {				
				super.onConfigure();
				setVisible(getActiveChat() != null);
			}

		});
		add(chatSelectorContainer);

		respondingContainer = new WebMarkupContainer("responding") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getResponding() != null);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript(String.format("""
					setTimeout(() => {
						var $responding = $('#%s');
						if ($responding.is(":visible")) 
							$responding[0].scrollIntoView({ block: "end" });
					}, 0);
					""", getMarkupId())));
			}

		};
		respondingContainer.add(new MarkdownViewer("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				var responding = getResponding();
				if (responding != null) 
					return responding.getContent();
				else
					return null;
			}
			
		}, null));
		respondingContainer.add(new ChangeObserver() {

			@Override
			protected Collection<String> findObservables() {
				var chat = getActiveChat();
				if (chat != null)
					return Collections.singleton(Chat.getPartialResponseObservable(chat.getId()));
				else
					return Collections.emptySet();
			}

		});

		respondingContainer.setOutputMarkupPlaceholderTag(true);
		add(respondingContainer);

		var form = new Form<Void>("send");
		form.add(new TextArea<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return WebSession.get().getChatInput();
			}

			@Override
			public void setObject(String object) {		
				if (object != null && object.length() > ChatMessage.MAX_CONTENT_LEN)		
					getSession().error(MessageFormat.format(_T("Message is too long. Max {0} characters"), ChatMessage.MAX_CONTENT_LEN));
				else
					WebSession.get().setChatInput(object);
			}

		}).add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
			}
			
		}));
		form.setOutputMarkupId(true);

		form.add(new AjaxButton("submit") {
						
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				var input = WebSession.get().getChatInput().trim();
				var chat = getActiveChat();
				ChatMessage request = new ChatMessage();
				request.setRequest(true);
				request.setContent(input);
				if (chat == null) {
					chat = new Chat();
					chat.setUser(getUser());
					chat.setAi(getActiveAI());
					chat.setTitle(_T("New chat"));
					chat.setDate(new Date());
					if (getUser() != null) {
						chatService.createOrUpdate(chat);
						request.setChat(chat);
						chat.getMessages().add(request);
						dao.persist(request);				
					} else {
						chat.setId(chatService.nextAnonymousChatId());
						request.setId(chatService.nextAnonymousChatMessageId());
						request.setChat(chat);
						chat.getMessages().add(request);
						WebSession.get().getAnonymousChats().put(chat.getId(), chat);
					}
					WebSession.get().setActiveChatId(chat.getId());
					target.add(chatSelectorContainer);	
				} else {
					if (getUser() != null) {
						request.setChat(chat);
						chat.getMessages().add(request);
						dao.persist(request);				
					} else {
						request.setId(chatService.nextAnonymousChatMessageId());
						chat = SerializationUtils.clone(chat);
						request.setChat(chat);
						chat.getMessages().add(request);
						WebSession.get().getAnonymousChats().put(chat.getId(), chat);
					}
				}

				chatService.sendRequest(getPage(), request);

				showNewMessages(target);
				target.add(respondingContainer);

				WebSession.get().setChatInput(null);
				target.appendJavaScript("""
					var $send = $(".chat>.body>.send");
					$send.find("textarea").val("");
					$send.find("a.submit").attr("disabled", "disabled");
					""");
			}

		});

		form.add(new AjaxLink<Void>("stop") {
						
			@Override
			public void onClick(AjaxRequestTarget target) {
				var responding = getResponding();
				if (responding != null)
					responding.cancel();
			}

		});

		add(form);		

		add(AttributeAppender.append("class", "chat flex-column"));		
		setOutputMarkupPlaceholderTag(true);
	}

	private User getActiveAI() {
		if (activeAiId != null) {
			var ai = userService.get(activeAiId);
			if (ai != null && getEntitledAis().contains(ai))
				return ai;
		}
		return getEntitledAis().get(0);
	}

	private void setActiveAI(User ai) {
		activeAiId = ai.getId();

		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie = new Cookie(COOKIE_ACTIVE_AI, activeAiId.toString());
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	@Nullable
	private Chat getActiveChat() {
		var activeChatId = WebSession.get().getActiveChatId();
		if (activeChatId != null) {
			Chat chat;
			if (getUser() != null) 
				chat = chatService.get(activeChatId);
			else
				chat = WebSession.get().getAnonymousChats().get(activeChatId);			
			if (chat != null && chat.getAi().equals(getActiveAI()))
				return chat;
		}
		return null;		
	}

	@Nullable
	private ChatResponding getResponding() {
		var chat = getActiveChat();
		if (chat != null)
			return chatService.getResponding(WebSession.get(), chat);
		else
			return null;
	}

	private List<ChatMessage> getMessages() {
		var chat = getActiveChat();
		if (chat != null) 
			return chat.getSortedMessages();
		else
			return Collections.emptyList();
	}

	@SuppressWarnings("deprecation")
	private void showNewMessages(IPartialPageRequestHandler handler) {
		long prevLastMessageId;
		if (messagesView.size() != 0)
			prevLastMessageId = (Long) messagesView.get(messagesView.size() - 1).getDefaultModelObject();
		else
			prevLastMessageId = 0;
		
		getMessages().stream().filter(it -> it.getId() > prevLastMessageId).forEach(it -> {
			var messageContainer = newMessageContainer(messagesView.newChildId(), it);
			messagesView.add(messageContainer);
			handler.prependJavaScript(String.format("""
				$('#%s').before($("<li class='message' id='%s'></li>"));
				""", respondingContainer.getMarkupId(), messageContainer.getMarkupId()));
			handler.add(messageContainer);
		});

		var lastMessage = messagesView.get(messagesView.size() - 1);
		if ((Long) lastMessage.getDefaultModelObject() > prevLastMessageId) {
			handler.appendJavaScript(String.format("""
				$('#%s')[0].scrollIntoView({ block: "end" });
				""", lastMessage.getMarkupId()));
		}
	}

	private Component newMessageContainer(String containerId, ChatMessage message) {
		var messageContainer = new WebMarkupContainer(containerId, Model.of(message.getId()));
		if (message.isError() || message.isRequest()) {
			messageContainer.add(new MultilineLabel("content", message.getContent()));
		} else {
			messageContainer.add(new MarkdownViewer("content", Model.of(message.getContent()), null));
		}

		if (message.isError()) 
			messageContainer.add(AttributeAppender.append("class", "error"));
		else if (message.isRequest()) 
			messageContainer.add(AttributeAppender.append("class", "request"));
		else 
			messageContainer.add(AttributeAppender.append("class", "response"));

		messageContainer.setOutputMarkupId(true);

		return messageContainer;
	}

	@Override
	protected void onBeforeRender() {
		messagesView = new RepeatingView("messages");
		for (var message: getMessages()) {
			messagesView.add(newMessageContainer(messagesView.newChildId(), message));
		}		
		addOrReplace(messagesView);

		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie("chat.width");
		if (cookie != null) 
			add(AttributeAppender.append("style", "width:" + cookie.getValue() + "px;"));
		else
			add(AttributeAppender.append("style", "width:400px;"));				
		super.onBeforeRender();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(WebSession.get().isChatVisible() && !getEntitledAis().isEmpty());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ChatResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.chat.onDomReady();"));
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		entitledAisModel.detach();
	}

	public List<User> getEntitledAis() {
		return entitledAisModel.getObject();
	}

	public void show(AjaxRequestTarget target, @Nullable String prompt) {
		WebSession.get().setChatVisible(true);
		WebSession.get().setActiveChatId(null);
		if (prompt != null) {
			WebSession.get().setChatInput(prompt);
			target.appendJavaScript("$('.chat>.body>.send .submit').click();");
		}	
		add(new DisplayNoneBehavior());		
		target.add(this);
		target.appendJavaScript("$('.chat').show('slide', {direction: 'right'}, 160);");	
	}

	public void hide(AjaxRequestTarget target) {
		WebSession.get().setChatVisible(false);
		target.appendJavaScript("""
			$('.chat').hide('slide', {direction: 'right'}, 160, function() {
				$('.chat').removeClass().empty();
			});
			""");
	}
	
}