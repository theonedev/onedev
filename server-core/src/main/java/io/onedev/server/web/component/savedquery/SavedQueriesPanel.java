package io.onedev.server.web.component.savedquery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.watch.WatchStatus;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.subscriptionstatus.SubscriptionStatusLink;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.watchstatus.WatchStatusLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public abstract class SavedQueriesPanel<T extends NamedQuery> extends Panel {

	private static final String COOKIE_PREFIX = "savedQueries.closed.";
	
	private boolean closed;
	
	public SavedQueriesPanel(String id) {
		super(id);
	}

	private String getCookieName() {
		return COOKIE_PREFIX + getPage().getClass().getSimpleName();
	}
	
	private WatchStatus getWatchStatus(String queryName) {
		QueryPersonalization<T> personalization = getQueryPersonalization();
		if (personalization != null)
			return personalization.getQueryWatchSupport().getWatchStatus(queryName);
		else
			return WatchStatus.DEFAULT;
	}
	
	private boolean getSubscriptionStatus(String queryName) {
		QueryPersonalization<T> personalization = getQueryPersonalization();
		if (personalization != null)
			return personalization.getQuerySubscriptionSupport().getQuerySubscriptions().contains(queryName);
		else
			return false;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SavedQueriesCssResourceReference()));
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof SavedQueriesOpened) {
			SavedQueriesOpened savedQueriesOpened = (SavedQueriesOpened) event.getPayload();
			toggle(savedQueriesOpened.getHandler());
			((BasePage) getPage()).resizeWindow(savedQueriesOpened.getHandler());
		}
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!closed);
	}

	private void toggle(IPartialPageRequestHandler handler) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		
		Cookie cookie = new Cookie(getCookieName(), closed?"no":"yes");
		cookie.setPath("/");
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		closed = !closed;
		handler.add(this);
	}
	
	private boolean canEditCommonQueries() {
		return SecurityUtils.isAdministrator() || Project.get() != null && SecurityUtils.canManage(Project.get());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(getCookieName());
		closed = cookie != null && "yes".equals(cookie.getValue());
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				toggle(target);
				send(getPage(), Broadcast.BREADTH, new SavedQueriesClosed(target));
				((BasePage) getPage()).resizeWindow(target);
			}
			
		});
		
		add(new ModalLink("edit") {

			private static final String TAB_PANEL_ID = "tabPanel";
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getUser() != null && (getQueryPersonalization() != null || canEditCommonQueries()));
			}

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}

			private Component newPersonalQueriesEditor(String componentId, ModalPanel modal) {
				ArrayList<T> personalQueries = getQueryPersonalization().getQueries();
				return new NamedQueriesEditor(componentId, personalQueries, null) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, ArrayList<T> queries) {
						target.add(SavedQueriesPanel.this);
						modal.close();
						
						QueryPersonalization<T> personalization = getQueryPersonalization();
						personalization.setQueries(queries);
						personalization.onUpdated();
					}
					
					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
			private Component newCommonQueriesEditor(String componentId, ModalPanel modal) {
				ArrayList<T> queries = getCommonQueries();
				UseDefaultListener useDefaultListener;
				if (queries == null) {
					queries = new ArrayList<>(getInheritedCommonQueries());
					useDefaultListener = null;
				} else if (getInheritedCommonQueries() != null) {
					useDefaultListener = new UseDefaultListener() {
						
						@Override
						public void onUseDefault(AjaxRequestTarget target) {
							target.add(SavedQueriesPanel.this);
							modal.close();
							onSaveCommonQueries(null);
						}
						
					};
				} else {
					useDefaultListener = null;
				}
				
				return new NamedQueriesEditor(componentId, queries, useDefaultListener) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, ArrayList<T> queries) {
						target.add(SavedQueriesPanel.this);
						modal.close();
						onSaveCommonQueries(queries);
					}
					
					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment;
				if (getQueryPersonalization() != null) {
					fragment = new Fragment(id, "editPersonalAwareSavedQueriesFrag", SavedQueriesPanel.this);
					List<Tab> tabs = new ArrayList<>();

					tabs.add(new AjaxActionTab(Model.of("Mine")) {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
						}

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component editor = newPersonalQueriesEditor(TAB_PANEL_ID, modal);
							fragment.replace(editor);
							target.add(editor);
						}
						
					});
					fragment.add(newPersonalQueriesEditor(TAB_PANEL_ID, modal));
					
					if (canEditCommonQueries()) {
						tabs.add(new AjaxActionTab(Model.of("All Users")) {

							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
							}

							@Override
							protected void onSelect(AjaxRequestTarget target, Component tabLink) {
								Component editor = newCommonQueriesEditor(TAB_PANEL_ID, modal);
								fragment.replace(editor);
								target.add(editor);
							}
							
						});
					}
					
					fragment.add(new Tabbable("tab", tabs));
				} else {
					fragment = new Fragment(id, "editSavedQueriesFrag", SavedQueriesPanel.this);
					fragment.add(newCommonQueriesEditor("content", modal));
				}
				
				fragment.add(new AjaxLink<Void>("close") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				return fragment;
			}
			
		});
		
		add(new ListView<T>("personalQueries", new LoadableDetachableModel<List<T>>() {

			@Override
			protected List<T> load() {
				return getQueryPersonalization().getQueries();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<T> item) {
				T namedQuery = item.getModelObject();
				Link<Void> link = newQueryLink("link", namedQuery);
				link.add(new Label("label", namedQuery.getName()));
				item.add(link);
				
				String personalName = NamedQuery.PERSONAL_NAME_PREFIX + namedQuery.getName();
				
				item.add(new WatchStatusLink("watchStatus") {
					
					@Override
					protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
						target.add(this);
						QueryPersonalization<T> personalization = getQueryPersonalization();
						personalization.getQueryWatchSupport().setWatchStatus(personalName, watchStatus);
						personalization.onUpdated();
					}
					
					@Override
					protected WatchStatus getWatchStatus() {
						return SavedQueriesPanel.this.getWatchStatus(personalName);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getQueryPersonalization().getQueryWatchSupport() != null);
					}
					
				});
				
				item.add(new SubscriptionStatusLink("subscriptionStatus") {
					
					@Override
					protected void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus) {
						target.add(this);
						QueryPersonalization<T> personalization = getQueryPersonalization();
						if (subscriptionStatus)
							personalization.getQuerySubscriptionSupport().getQuerySubscriptions().add(personalName);
						else
							personalization.getQuerySubscriptionSupport().getQuerySubscriptions().remove(personalName);
						personalization.onUpdated();
					}
					
					@Override
					protected boolean isSubscribed() {
						return getSubscriptionStatus(personalName);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getQueryPersonalization().getQuerySubscriptionSupport() != null);
					}
					
				});
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getUser() != null && getQueryPersonalization() != null);
			}

		});
		
		add(new ListView<T>("commonQueries", new LoadableDetachableModel<List<T>>() {

			@Override
			protected List<T> load() {
				List<T> namedQueries = new ArrayList<>();
				for (T namedQuery: getCommonQueries()!=null?getCommonQueries():getInheritedCommonQueries())
					namedQueries.add(namedQuery);
				return namedQueries;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<T> item) {
				T namedQuery = item.getModelObject();
				Link<Void> link = newQueryLink("link", namedQuery);
				link.add(new Label("label", namedQuery.getName()));
				item.add(link);
				
				String commonName = NamedQuery.COMMON_NAME_PREFIX + namedQuery.getName();
				
				item.add(new WatchStatusLink("watchStatus") {
					
					@Override
					protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
						target.add(this);

						QueryPersonalization<T> personalization = getQueryPersonalization();
						personalization.getQueryWatchSupport().setWatchStatus(commonName, watchStatus);
						personalization.onUpdated();
					}
					
					@Override
					protected WatchStatus getWatchStatus() {
						return SavedQueriesPanel.this.getWatchStatus(commonName);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.getUser() != null 
								&& getQueryPersonalization() != null 
								&& getQueryPersonalization().getQueryWatchSupport() != null);
					}
					
				});
				
				item.add(new SubscriptionStatusLink("subscriptionStatus") {
					
					@Override
					protected void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus) {
						target.add(this);

						QueryPersonalization<T> personalization = getQueryPersonalization();
						if (subscriptionStatus)
							personalization.getQuerySubscriptionSupport().getQuerySubscriptions().add(commonName);
						else
							personalization.getQuerySubscriptionSupport().getQuerySubscriptions().remove(commonName);
						personalization.onUpdated();
					}
					
					@Override
					protected boolean isSubscribed() {
						return getSubscriptionStatus(commonName);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.getUser() != null 
								&& getQueryPersonalization() != null
								&& getQueryPersonalization().getQuerySubscriptionSupport() != null);
					}
					
				});
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});		
		
		setOutputMarkupPlaceholderTag(true);
	}
	
	private abstract class NamedQueriesEditor extends Fragment {

		private final NamedQueriesBean<T> bean;
		
		private final UseDefaultListener useDefaultListener;
		
		public NamedQueriesEditor(String id, ArrayList<T> queries, @Nullable UseDefaultListener useDefaultListener) {
			super(id, "editSavedQueriesContentFrag", SavedQueriesPanel.this);
			bean = newNamedQueriesBean();
			bean.getQueries().addAll(queries);
			this.useDefaultListener = useDefaultListener;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			Form<?> form = new Form<Void>("form");
			form.setOutputMarkupId(true);
			
			form.add(new FencedFeedbackPanel("feedback", form));
			form.add(BeanContext.edit("editor", bean));
			form.add(new AjaxButton("save") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					
					Set<String> names = new HashSet<>();
					for (NamedQuery namedQuery: bean.getQueries()) {
						if (names.contains(namedQuery.getName())) {
							form.error("Duplicate name found: " + namedQuery.getName());
							return;
						} else {
							names.add(namedQuery.getName());
						}
					}
					onSave(target, (ArrayList<T>)bean.getQueries());
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(form);
				}
				
			});
			form.add(new AjaxLink<Void>("useDefault") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					String message = "This will discard all project specific queries, do you want to continue?";
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					useDefaultListener.onUseDefault(target);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(useDefaultListener != null);
				}
				
			});
			
			form.add(new AjaxLink<Void>("cancel") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
				
			});
			add(form);
			setOutputMarkupId(true);
		}
		
		protected abstract void onSave(AjaxRequestTarget target, ArrayList<T> queries);
		
		protected abstract void onCancel(AjaxRequestTarget target);
		
	}
	
	protected abstract NamedQueriesBean<T> newNamedQueriesBean();
	
	protected abstract Link<Void> newQueryLink(String componentId, T namedQuery);
	
	@Nullable
	protected abstract QueryPersonalization<T> getQueryPersonalization();
	
	@Nullable
	protected abstract ArrayList<T> getCommonQueries();

	protected abstract void onSaveCommonQueries(ArrayList<T> queries);
	
	@Nullable
	protected ArrayList<T> getInheritedCommonQueries() {
		return null;
	}
	
}
