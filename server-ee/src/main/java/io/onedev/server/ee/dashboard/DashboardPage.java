package io.onedev.server.ee.dashboard;

import io.onedev.server.OneDev;
import io.onedev.server.ee.dashboard.widgets.ProjectListWidget;
import io.onedev.server.entitymanager.DashboardManager;
import io.onedev.server.entitymanager.DashboardVisitManager;
import io.onedev.server.model.*;
import io.onedev.server.model.support.Widget;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.typeselect.TypeSelectPanel;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.layout.LayoutPage;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {
	
	private static final String PARAM_DASHBOARD = "dashboard";
	
	private static final int HORIZONTAL_CELL_COUNT = 16;
	
	private final IModel<List<Dashboard>> dashboardsModel = new LoadableDetachableModel<>() {

		@Override
		protected List<Dashboard> load() {
			User user = getLoginUser();
			List<Dashboard> dashboards = getDashboardManager().queryAccessible(user);
			if (!dashboards.isEmpty()) {
				Map<Dashboard, Date> dates = new HashMap<>();
				if (user != null) {
					for (DashboardVisit visit : user.getDashboardVisits())
						dates.put(visit.getDashboard(), visit.getDate());
				}
				dashboards.sort((o1, o2) -> {
					Date date1 = dates.get(o1);
					Date date2 = dates.get(o2);
					if (date1 != null) {
						if (date2 != null)
							return date2.compareTo(date1);
						else
							return -1;
					} else {
						if (date2 != null)
							return 1;
						else
							return o2.getId().compareTo(o1.getId());
					}
				});
			}
			return dashboards;
		}

	};
	
	private final IModel<Dashboard> activeDashboardModel;
	
	private Dashboard editingDashboard;
	
	private boolean failsafe;
	
	public DashboardPage(PageParameters params) {
		super(params);
		
		Long activeDashboardId = params.get(PARAM_DASHBOARD).toOptionalLong();
		activeDashboardModel = new LoadableDetachableModel<Dashboard>() {

			@Override
			protected Dashboard load() {
				List<Dashboard> dashboards = getDashboards();  
				if (activeDashboardId != null) {
					Dashboard activeDashboard = getDashboardManager().load(activeDashboardId);
					if (dashboards.contains(activeDashboard))
						return activeDashboard;
					else
						throw new UnauthorizedException();
				} else if (!dashboards.isEmpty()) {
					return dashboards.iterator().next();
				} else {
					return newDefaultDashboard();
				}
			}
			
		};		
		
		failsafe = params.get(HomePage.PARAM_FAILSAFE).toBoolean(false);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		editingDashboard = getActiveDashboard();
		add(newDashboardViewer());
	}
	
	private Component newDashboardViewer() {
		Fragment fragment = new Fragment("content", "dashboardViewFrag", this) {
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(OnLoadHeaderItem.forScript(String.format(
						"onedev.server.dashboard.onLoad(%d); $(window).resize();", 
						HORIZONTAL_CELL_COUNT)));
			}
			
		};
		fragment.add(AttributeAppender.append("class", "dashboard-viewer"));
		Dashboard activeDashboard = getActiveDashboard(); 
		WebMarkupContainer dashboardSelector;
		if (activeDashboard.isNew()) {
			dashboardSelector = new WebMarkupContainer("dashboardSelector") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
		} else {
			dashboardSelector = new DropdownLink("dashboardSelector") {

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment fragment = new Fragment(id, "dashboardSelectorFrag", DashboardPage.this);
					fragment.add(new ListView<Dashboard>("dashboards", new AbstractReadOnlyModel<List<Dashboard>>() {

						@Override
						public List<Dashboard> getObject() {
							return getDashboards();
						}
						
					}) {

						@Override
						protected void populateItem(ListItem<Dashboard> item) {
							Dashboard dashboard = item.getModelObject();
							Link<Void> link = new Link<Void>("link") {

								@Override
								public void onClick() {
									Dashboard dashboard = item.getModelObject();
									if (getLoginUser() != null)
										visit(dashboard);
									setResponsePage(DashboardPage.class, paramsOf(dashboard, failsafe));
								}
								
							};
							
							if (dashboard.equals(getActiveDashboard()))
								link.add(new SpriteImage("icon", "tick"));
							else
								link.add(new WebMarkupContainer("icon"));
							link.add(new Label("label", dashboard.getName()));

							if (getLoginUser() != null && !dashboard.getOwner().equals(getLoginUser()))
								link.add(new Label("note", "Shared by " + dashboard.getOwner().getDisplayName()));
							else
								link.add(new WebMarkupContainer("note").setVisible(false));
							
							item.add(link);
						}
						
					});
					return fragment;
				}
				
			};
		}
		dashboardSelector.add(new Label("name", activeDashboard.getName()));
		
		String note;
		if (getLoginUser() != null && !activeDashboard.getOwner().equals(getLoginUser()))
			note = "shared by " + activeDashboard.getOwner().getDisplayName();
		else
			note = null;
		
		if (note != null)
			fragment.add(new Label("dashboardNote", note));
		else
			fragment.add(new WebMarkupContainer("dashboardNote").setVisible(false));
		
		fragment.add(dashboardSelector);
		
		fragment.add(new AjaxLink<Void>("editDashboard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingDashboard = getActiveDashboard();
				Component editor = newDashboardEditor();
				DashboardPage.this.replace(editor);
				target.add(editor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null && getActiveDashboard().getOwner().equals(getLoginUser()));
			}
			
		});
		fragment.add(new AjaxLink<Void>("copyDashboard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingDashboard = SerializationUtils.clone(getActiveDashboard());
				editingDashboard.setId(null);
				Component editor = newDashboardEditor();
				DashboardPage.this.replace(editor);
				target.add(editor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null && !getActiveDashboard().isNew());
			}
			
		});
		fragment.add(new AjaxLink<Void>("shareDashboard") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null 
						&& getActiveDashboard().getOwner().equals(getLoginUser()) 
						&& !getActiveDashboard().isNew());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				DashboardShareBean bean = new DashboardShareBean();
				for (DashboardGroupShare share: getActiveDashboard().getGroupShares()) 
					bean.getShareGroups().add(share.getGroup().getName());
				for (DashboardUserShare share: getActiveDashboard().getUserShares())
					bean.getShareUsers().add(share.getUser().getName());
				
				bean.setForEveryone(getActiveDashboard().isForEveryone());
					
				Set<String> excludeProperties = new HashSet<>();
				if (!SecurityUtils.isAdministrator())
					excludeProperties.add(DashboardShareBean.PROP_FOR_EVERYONE);
				new BeanEditModalPanel<DashboardShareBean>(target, bean, excludeProperties, 
						true, "Share Dashboard") {
					
					@Override
					protected void onSave(AjaxRequestTarget target, DashboardShareBean bean) {
						Dashboard dashboard = getActiveDashboard();
						
						dashboard.setForEveryone(bean.isForEveryone());
						getDashboardManager().update(dashboard);
						
						getDashboardManager().syncShares(dashboard, bean.isForEveryone(), 
								bean.getShareGroups(), bean.getShareUsers());
						close();
					}
					
				};
				
			}
			
		});
		fragment.add(new AjaxLink<Void>("deleteDashboard") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				Dashboard dashboard = getActiveDashboard();
				if (dashboard.isForEveryone() 
						|| !dashboard.getGroupShares().isEmpty() 
						|| !dashboard.getUserShares().isEmpty()) {
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(""
							+ "This dashboard is currently being shared with others, "
							+ "do you really want to delete it?"));
				} else {
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(""
							+ "Do you really want to delete this dashboard?"));
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getDashboardManager().delete(getActiveDashboard());
				setResponsePage(DashboardPage.class);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null 
						&& getActiveDashboard().getOwner().equals(getLoginUser())
						&& !getActiveDashboard().isNew());
			}
			
		});
		fragment.add(new AjaxLink<Void>("addDashboard") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				editingDashboard = new Dashboard();
				Component editor = newDashboardEditor();
				DashboardPage.this.replace(editor);
				target.add(editor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null);
			}
			
		});
		
		RepeatingView widgetsView = new RepeatingView("widgets");
		for (Widget widget: activeDashboard.getWidgets()) 
			widgetsView.add(new WidgetPanel(widgetsView.newChildId(), widget, failsafe, null));
		
		fragment.add(widgetsView);
		
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private void visit(Dashboard dashboard) {
		DashboardVisit visit = getLoginUser().getDashboardVisit(dashboard);
		if (visit == null) {
			visit = new DashboardVisit();
			visit.setDashboard(dashboard);
			visit.setUser(getLoginUser());
			getLoginUser().getDashboardVisits().add(visit);
		}
		visit.setDate(new Date());
		if (visit.isNew())
			getDashboardVisitManager().create(visit);
		else
			getDashboardVisitManager().update(visit);			
	}
	
	private DashboardManager getDashboardManager() {
		return OneDev.getInstance(DashboardManager.class);
	}
	
	private DashboardVisitManager getDashboardVisitManager() {
		return OneDev.getInstance(DashboardVisitManager.class);
	}
	
	private void markFormDirty(AjaxRequestTarget target, Form<?> form) {
		String script = String.format("onedev.server.form.markDirty($('#%s'));", form.getMarkupId());		
		target.appendJavaScript(script);
	}
	
	private WidgetPanel newWidgetPanel(Form<?> form, RepeatingView widgetsView, Widget widget) {
		return new WidgetPanel(widgetsView.newChildId(), widget, failsafe, new WidgetEditCallback() {

			@Override
			public void onSave(AjaxRequestTarget target, WidgetPanel widgetPanel) {
				markFormDirty(target, form);
			}

			@Override
			public void onDelete(AjaxRequestTarget target, WidgetPanel widgetPanel) {
				editingDashboard.getWidgets().remove(widget);
				markFormDirty(target, form);
				widgetsView.remove(widgetPanel);
				target.appendJavaScript(String.format("$('#%s').remove();", widgetPanel.getMarkupId()));
			}

			@Override
			public void onCopy(AjaxRequestTarget target, Widget widget) {
				onWidgetAdded(target, form, widgetsView, widget);
			}
			
		});
	}
	
	private Component newDashboardEditor() {
		Fragment fragment = new Fragment("content", "dashboardEditFrag", this) {
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(OnLoadHeaderItem.forScript(String.format(
						"onedev.server.dashboard.onLoad(%d); $(window).resize();", 
						HORIZONTAL_CELL_COUNT)));
			}
			
		};
		fragment.add(AttributeAppender.append("class", "dashboard-editor"));
		fragment.setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form");
		IModel<String> nameModel = new PropertyModel<String>(editingDashboard, "name");
		form.add(new TextField<String>("name", nameModel));

		RepeatingView widgetsView = new RepeatingView("widgets");
		for (Widget widget: editingDashboard.getWidgets()) 
			widgetsView.add(newWidgetPanel(form, widgetsView, widget));
		
		fragment.add(widgetsView);
		
		form.add(new DropdownLink("addWidget") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new TypeSelectPanel<Widget>(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends Widget> type) {
						dropdown.close();
						
						Widget widget;
						try {
							widget = type.getDeclaredConstructor().newInstance();
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							throw new RuntimeException(e);
						}
						new BeanEditModalPanel<Widget>(target, widget) {

							@Override
							protected void onSave(AjaxRequestTarget target, Widget bean) {
								onWidgetAdded(target, form, widgetsView, bean);
								close();
							}
							
						};
					}
					
				};
			}
			
		});
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (editingDashboard.getName() == null) {
					error("Name is required");
				} else {
					Dashboard dashboardWithSameName = getDashboardManager().find(getLoginUser(), editingDashboard.getName());
					if (dashboardWithSameName != null 
							&& (editingDashboard.isNew() || !dashboardWithSameName.equals(editingDashboard))) { 
						error("This name is already been used by another dashboard under your account");
					}
				}

				if (!hasErrorMessage()) {
					if (editingDashboard.isNew()) {
						editingDashboard.setOwner(getLoginUser());
						getDashboardManager().create(editingDashboard);
						visit(editingDashboard);
						setResponsePage(DashboardPage.class, paramsOf(editingDashboard, failsafe));
					} else {
						Dashboard dashboard = getActiveDashboard();
						dashboard.setName(editingDashboard.getName());
						dashboard.setWidgets(editingDashboard.getWidgets());
						getDashboardManager().update(dashboard);
						visit(dashboard);
						setResponsePage(DashboardPage.class, paramsOf(dashboard, failsafe));
					}
				} else {
					target.add(form);
				}
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
				editingDashboard = new Dashboard();
				Component viewer = newDashboardViewer();
				DashboardPage.this.replace(viewer);
				target.add(viewer);
			}
			
		});
		form.add(new FencedFeedbackPanel("feedback", form));
		form.setOutputMarkupId(true);
		
		fragment.add(form);
		
		return fragment;
	}
	
	private void initWidgetPosition(Widget widget) {
		int top = 0;
		var defaultWidth = widget.getDefaultWidth();
		if (defaultWidth > HORIZONTAL_CELL_COUNT)
			defaultWidth = HORIZONTAL_CELL_COUNT;
		while (true) {
			for (int left = 0; left <= HORIZONTAL_CELL_COUNT - defaultWidth; left++) {
				widget.setLeft(left);
				widget.setTop(top);
				widget.setRight(left + defaultWidth);
				widget.setBottom(top + widget.getDefaultHeight());
				if (editingDashboard.getWidgets().stream().noneMatch(it->it.isIntersectedWith(widget)))
					return;
			}
			top++;
		}
	}
	
	private void onWidgetAdded(AjaxRequestTarget target, Form<?> form, RepeatingView widgetsView, Widget widget) {
		initWidgetPosition(widget);
		editingDashboard.getWidgets().add(widget);
		markFormDirty(target, form);
		WidgetPanel widgetPanel = newWidgetPanel(form, widgetsView, widget);
		widgetsView.add(widgetPanel);
		target.prependJavaScript(String.format(
				"$('.dashboard>.body>.content').append('<div id=\"%s\"></div>');", 
				widgetPanel.getMarkupId()));
		target.add(widgetPanel);
		target.appendJavaScript(String.format("onedev.server.dashboard.onWidgetAdded('%s');",
				widgetPanel.getMarkupId()));
	}
	
	private Dashboard newDefaultDashboard() {
		Dashboard dashboard = new Dashboard();
		ProjectListWidget widget = new ProjectListWidget();
		widget.setTitle("Projects");
		widget.setLeft(0);
		widget.setRight(HORIZONTAL_CELL_COUNT);
		widget.setTop(0);
		widget.setBottom(16);
		dashboard.getWidgets().add(widget);
		dashboard.setName("Default");
		dashboard.setOwner(getLoginUser());
		return dashboard;
	}
	
	private List<Dashboard> getDashboards() {
		return dashboardsModel.getObject();
	}
	
	private Dashboard getActiveDashboard() {
		return activeDashboardModel.getObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new DashboardResourceReference()));
	}

	@Override
	protected void onDetach() {
		dashboardsModel.detach();
		activeDashboardModel.detach();
		super.onDetach();
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Dashboards");
	}

	public static PageParameters paramsOf(@Nullable Dashboard dashboard, boolean failsafe) {
		PageParameters params = new PageParameters();
		if (dashboard != null)
			params.add(PARAM_DASHBOARD, dashboard.getId());
		if (failsafe)
			params.add(HomePage.PARAM_FAILSAFE, true);
		return params;
	}
	
}
