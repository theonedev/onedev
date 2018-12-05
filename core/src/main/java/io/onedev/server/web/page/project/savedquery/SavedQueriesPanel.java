package io.onedev.server.web.page.project.savedquery;

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
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.watch.WatchStatus;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.subscriptionstatus.SubscriptionStatusLink;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.watchstatus.WatchStatusLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

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
	
	private ArrayList<T> getUserQueries() {
		QuerySetting<T> querySetting = getQuerySetting();
		if (querySetting != null)
			return querySetting.getUserQueries();
		else
			return new ArrayList<>();
	}	
	
	private WatchStatus getProjectWatchStatus(T namedQuery) {
		QuerySetting<T> querySetting = getQuerySetting();
		if (querySetting != null)
			return querySetting.getQueryWatchSupport().getProjectWatchStatus(namedQuery);
		else
			return WatchStatus.DEFAULT;
	}
	
	private WatchStatus getUserWatchStatus(T namedQuery) {
		QuerySetting<T> querySetting = getQuerySetting();
		if (querySetting != null)
			return querySetting.getQueryWatchSupport().getUserWatchStatus(namedQuery);
		else
			return WatchStatus.DEFAULT;
	}
	
	private boolean getProjectSubscriptionStatus(T namedQuery) {
		QuerySetting<T> querySetting = getQuerySetting();
		if (querySetting != null)
			return querySetting.getQuerySubscriptionSupport().getProjectQuerySubscriptions().contains(namedQuery.getName());
		else
			return false;
	}
	
	private boolean getUserSubscriptionStatus(T namedQuery) {
		QuerySetting<T> querySetting = getQuerySetting();
		if (querySetting != null)
			return querySetting.getQuerySubscriptionSupport().getUserQuerySubscriptions().contains(namedQuery.getName());
		else
			return false;
	}
	
	private Project getProject() {
		ProjectPage page = (ProjectPage) getPage();
		return page.getProject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SavedQueriesCssResourceReference()));
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!closed);
	}

	private void toggle(AjaxRequestTarget target) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		Cookie cookie = new Cookie(getCookieName(), closed?"no":"yes");
		cookie.setPath("/");
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		closed = !closed;
		target.add(this);
	}
	
	public void show(AjaxRequestTarget target) {
		if (closed)
			toggle(target);
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
			}
			
		});
		
		add(new ModalLink("edit") {

			private static final String TAB_PANEL_ID = "tabPanel";
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getUser() != null);
			}

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}

			private Component newUserQueriesEditor(String componentId, ModalPanel modal, ArrayList<T> userQueries) {
				return new NamedQueriesEditor(componentId, userQueries, null) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, ArrayList<T> queries) {
						target.add(SavedQueriesPanel.this);
						modal.close();
						
						QuerySetting<T> querySetting = getQuerySetting();
						querySetting.setUserQueries(queries);
						onSaveQuerySetting(querySetting);
					}
					
					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
			private Component newProjectQueriesEditor(String componentId, ModalPanel modal, ArrayList<T> projectQueries, 
					@Nullable UseDefaultListener useDefaultListener) {
				return new NamedQueriesEditor(componentId, projectQueries, useDefaultListener) {
					
					@Override
					protected void onSave(AjaxRequestTarget target, ArrayList<T> queries) {
						target.add(SavedQueriesPanel.this);
						modal.close();
						onSaveProjectQueries(queries);
					}
					
					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "editSavedQueriesFrag", SavedQueriesPanel.this);
				List<Tab> tabs = new ArrayList<>();

				ArrayList<T> userQueries = getUserQueries();
				tabs.add(new AjaxActionTab(Model.of("For Mine")) {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						Component editor = newUserQueriesEditor(TAB_PANEL_ID, modal, userQueries);
						fragment.replace(editor);
						target.add(editor);
					}
					
				});
				fragment.add(newUserQueriesEditor(TAB_PANEL_ID, modal, userQueries));
				
				if (SecurityUtils.canAdministrate(getProject().getFacade())) {
					tabs.add(new AjaxActionTab(Model.of("For All Users")) {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
						}

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							ArrayList<T> projectQueries = getProjectQueries();
							UseDefaultListener useDefaultListener;
							if (projectQueries == null) {
								projectQueries = new ArrayList<>(getDefaultProjectQueries());
								useDefaultListener = null;
							} else if (getDefaultProjectQueries() != null) {
								useDefaultListener = new UseDefaultListener() {
									
									@Override
									public void onUseDefault(AjaxRequestTarget target) {
										target.add(SavedQueriesPanel.this);
										modal.close();
										onSaveProjectQueries(null);
									}
									
								};
							} else {
								useDefaultListener = null;
							}
							Component editor = newProjectQueriesEditor(TAB_PANEL_ID, modal, projectQueries, useDefaultListener);
							fragment.replace(editor);
							target.add(editor);
						}
						
					});
				}
				
				fragment.add(new Tabbable("tab", tabs));
				
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
		
		add(new ListView<T>("userQueries", new LoadableDetachableModel<List<T>>() {

			@Override
			protected List<T> load() {
				return getUserQueries();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<T> item) {
				T namedQuery = item.getModelObject();
				Link<Void> link = newQueryLink("link", namedQuery);
				link.add(new Label("label", namedQuery.getName()));
				item.add(link);
				
				item.add(new WatchStatusLink("watchStatus") {
					
					@Override
					protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
						target.add(this);
						QuerySetting<T> querySetting = getQuerySetting();
						querySetting.getQueryWatchSupport().setUserWatchStatus(namedQuery, watchStatus);
						onSaveQuerySetting(querySetting);
						
					}
					
					@Override
					protected WatchStatus getWatchStatus() {
						return getUserWatchStatus(namedQuery);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getQuerySetting().getQueryWatchSupport() != null);
					}
					
				});
				
				item.add(new SubscriptionStatusLink("subscriptionStatus") {
					
					@Override
					protected void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus) {
						target.add(this);
						QuerySetting<T> querySetting = getQuerySetting();
						if (subscriptionStatus)
							querySetting.getQuerySubscriptionSupport().getUserQuerySubscriptions().add(namedQuery.getName());
						else
							querySetting.getQuerySubscriptionSupport().getUserQuerySubscriptions().remove(namedQuery.getName());
							
						onSaveQuerySetting(querySetting);
						
					}
					
					@Override
					protected boolean isSubscribed() {
						return getUserSubscriptionStatus(namedQuery);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getQuerySetting().getQuerySubscriptionSupport() != null);
					}
					
				});
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}

		});
		
		add(new ListView<T>("projectQueries", new LoadableDetachableModel<List<T>>() {

			@Override
			protected List<T> load() {
				List<T> namedQueries = new ArrayList<>();
				for (T namedQuery: getProjectQueries()!=null?getProjectQueries():getDefaultProjectQueries()) {
					try {
						if (SecurityUtils.getUser() != null || !needsLogin(namedQuery))
							namedQueries.add(namedQuery);
					} catch (Exception e) {
						namedQueries.add(namedQuery);
					}
				}
				return namedQueries;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<T> item) {
				T namedQuery = item.getModelObject();
				Link<Void> link = newQueryLink("link", namedQuery);
				link.add(new Label("label", namedQuery.getName()));
				item.add(link);
				
				item.add(new WatchStatusLink("watchStatus") {
					
					@Override
					protected void onWatchStatusChange(AjaxRequestTarget target, WatchStatus watchStatus) {
						target.add(this);

						QuerySetting<T> querySetting = getQuerySetting();
						querySetting.getQueryWatchSupport().setProjectWatchStatus(namedQuery, watchStatus);
						onSaveQuerySetting(querySetting);
					}
					
					@Override
					protected WatchStatus getWatchStatus() {
						return getProjectWatchStatus(namedQuery);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.getUser() != null && getQuerySetting().getQueryWatchSupport() != null);
					}
					
				});
				
				item.add(new SubscriptionStatusLink("subscriptionStatus") {
					
					@Override
					protected void onSubscriptionStatusChange(AjaxRequestTarget target, boolean subscriptionStatus) {
						target.add(this);

						QuerySetting<T> querySetting = getQuerySetting();
						if (subscriptionStatus)
							querySetting.getQuerySubscriptionSupport().getProjectQuerySubscriptions().add(namedQuery.getName());
						else
							querySetting.getQuerySubscriptionSupport().getProjectQuerySubscriptions().remove(namedQuery.getName());
						onSaveQuerySetting(querySetting);
					}
					
					@Override
					protected boolean isSubscribed() {
						return getProjectSubscriptionStatus(namedQuery);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.getUser() != null && getQuerySetting().getQuerySubscriptionSupport() != null);
					}
					
				});
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});		
		
		add(new WebMarkupContainer("watchHint") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuerySetting() != null && getQuerySetting().getQueryWatchSupport() != null);
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
			
			form.add(new NotificationPanel("feedback", form));
			form.add(BeanContext.editBean("editor", bean));
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
					attributes.getAjaxCallListeners().add(new ConfirmListener(message));
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
	
	protected abstract boolean needsLogin(T namedQuery);
	
	protected abstract Link<Void> newQueryLink(String componentId, T namedQuery);
	
	@Nullable
	protected abstract QuerySetting<T> getQuerySetting();
	
	@Nullable
	protected abstract ArrayList<T> getProjectQueries();

	protected abstract void onSaveProjectQueries(ArrayList<T> projectQueries);
	
	protected abstract void onSaveQuerySetting(QuerySetting<T> querySetting);
	
	@Nullable
	protected ArrayList<T> getDefaultProjectQueries() {
		return null;
	}
	
}
