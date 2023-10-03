package io.onedev.server.ee.timetracking;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.ProjectIssuesPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class TimesheetsPage extends ProjectIssuesPage {
	
	private static final String PARAM_TIMESHEET = "timesheet";
	
	private static final String PARAM_BASE_DATE = "base-date";
	
	private State state = new State();
	
	private Map<String, TimesheetSetting> timesheetSettings;
	
	private Map<String, TimesheetSetting> hierarchyTimesheetSettings;
	
	private TimesheetPanel timesheet;
	
	public TimesheetsPage(PageParameters params) {
		super(params);
		
		if (!isSubscriptionActive())
			throw new UnsupportedOperationException();
		
		timesheetSettings = getProject().getIssueSetting().getTimesheetSettings();
		hierarchyTimesheetSettings = getProject().getHierarchyTimesheetSettings();
		
		state.timesheetName = params.get(PARAM_TIMESHEET).toOptionalString();
		if (state.timesheetName != null) {
			if (!hierarchyTimesheetSettings.containsKey(state.timesheetName))			
				throw new ExplicitException("Unable to find timesheet: " + state.timesheetName);
		} else if (!hierarchyTimesheetSettings.isEmpty()) {
			state.timesheetName = hierarchyTimesheetSettings.entrySet().iterator().next().getKey();
		}
		
		String baseDate = params.get(PARAM_BASE_DATE).toOptionalString();
		if (baseDate != null)
			state.baseDate = LocalDate.parse(baseDate);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!hierarchyTimesheetSettings.isEmpty()) {
			var fragment = new Fragment("content", "hasTimesheetsFrag", this);
			fragment.add(new DropdownLink("timesheetChoice") {
				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("name", new AbstractReadOnlyModel<String>() {
						@Override
						public String getObject() {
							return state.timesheetName;
						}
					}));
				}

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					var choiceFragment = new Fragment(id, "timesheetChoiceFrag", TimesheetsPage.this);
					choiceFragment.add(new ListView<>("timesheets", new LoadableDetachableModel<List<Map.Entry<String, TimesheetSetting>>>() {
						@Override
						protected List<Map.Entry<String, TimesheetSetting>> load() {
							return new ArrayList<>(getProject().getHierarchyTimesheetSettings().entrySet());
						}
					}) {

						@Override
						protected void populateItem(ListItem<Map.Entry<String, TimesheetSetting>> item) {
							var entry = item.getModelObject();
							
							var dragIndicator = new WebMarkupContainer("dragIndicator").setVisible(SecurityUtils.canManageIssues(getProject()));
							item.add(dragIndicator);
							if (timesheetSettings.containsKey(entry.getKey()))
								item.add(AttributeAppender.append("class", "not-inherited"));
							else 
								dragIndicator.add(AttributeAppender.append("class", "opacity-25")).add(AttributeAppender.append("title", "Inherited timesheets can not be sorted"));
							
							Link<Void> link = new BookmarkablePageLink<Void>("select", TimesheetsPage.class, paramsOf(getProject(), entry.getKey(), state.baseDate));
							link.add(new Label("name", entry.getKey()));
							item.add(link);

							item.add(new WebMarkupContainer("default").setVisible(item.getIndex() == 0));
							item.add(new WebMarkupContainer("inherited").setVisible(!timesheetSettings.containsKey(entry.getKey())));

							WebMarkupContainer actions = new WebMarkupContainer("actions") {

								@Override
								protected void onConfigure() {
									super.onConfigure();
									setVisible(SecurityUtils.canManageIssues(getProject()));
								}

							};
							item.add(actions);
							actions.add(new TimesheetEditLink("edit", entry.getKey()) {
								
								@Override
								public void onClick(AjaxRequestTarget target) {
									dropdown.close();
									super.onClick(target);
								}
								
								@Override
								protected void onSave(AjaxRequestTarget target, ModalPanel modal, String name, TimesheetSetting setting) {
									state.timesheetName = name;
									target.add(fragment);
									resizeWindow(target);
									modal.close();
									CharSequence url = urlFor(TimesheetsPage.class, paramsOf(getProject(), state.timesheetName, state.baseDate));
									pushState(target, url.toString(), state);
								}
								
							});

							actions.add(new Link<Void>("delete") {

								@Override
								public void onClick() {
									var entry = item.getModelObject();
									timesheetSettings.remove(entry.getKey());
									getProject().getIssueSetting().setTimesheetSettings(timesheetSettings);
									getProjectManager().update(getProject());

									PageParameters params;
									if (entry.getKey().equals(state.timesheetName))
										params = paramsOf(getProject(), null, state.baseDate);
									else
										params = paramsOf(getProject(), state.timesheetName, state.baseDate);
									setResponsePage(TimesheetsPage.class, params);
								}

							}.add(new ConfirmClickModifier("Do you really want to delete timesheet '" + entry.getKey() + "'?")).setVisible(timesheetSettings.containsKey(entry.getKey())));
						}
					});
					choiceFragment.add(new TimesheetEditLink("newTimesheet", null) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							dropdown.close();
							super.onClick(target);
						}

						@Override
						protected void onSave(AjaxRequestTarget target, ModalPanel modal, String name, TimesheetSetting setting) {
							setResponsePage(TimesheetsPage.class, paramsOf(getProject(), name, state.baseDate));
						}
						
						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(SecurityUtils.canManageIssues(getProject()));
						}
						
					});

					if (SecurityUtils.canManage(getProject())) {
						choiceFragment.add(new SortBehavior() {

							@Override
							protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
								var timesheetNames = new HashMap<TimesheetSetting, String>();
								for (var entry: timesheetSettings.entrySet())
									timesheetNames.put(entry.getValue(), entry.getKey());
								var timesheetSettingList = new ArrayList<>(timesheetSettings.values());
								CollectionUtils.move(timesheetSettingList, from.getItemIndex(), to.getItemIndex());
								
								timesheetSettings.clear();
								for (var timesheetSetting: timesheetSettingList) 
									timesheetSettings.put(timesheetNames.get(timesheetSetting), timesheetSetting);
								getProject().getIssueSetting().setTimesheetSettings(timesheetSettings);
								getProjectManager().update(getProject());
								target.add(choiceFragment);
							}

						}.items(".timesheet.not-inherited"));
					}
					choiceFragment.setOutputMarkupId(true);
					return choiceFragment;
				}
			});
			fragment.add(new DateRangeNavigator("dateRangeNav") {

				@Override
				protected LocalDate getBaseDate() {
					return state.baseDate;
				}

				@Override
				protected void onBaseDateUpdate(AjaxRequestTarget target, LocalDate baseDate) {
					state.baseDate = baseDate;
					target.add(timesheet);
					var url = urlFor(TimesheetsPage.class, paramsOf(getProject(), state.timesheetName, state.baseDate));
					pushState(target, url.toString(), state);
				}

				@Override
				protected TimesheetSetting getTimesheetSetting() {
					return TimesheetsPage.this.getTimesheetSetting();
				}
			});
			fragment.add(new ResourceLink<Void>("export", new TimesheetXlsxResource() {

				@Override
				protected String getTitle() {
					return state.timesheetName;
				}

				@Override
				protected TimesheetPanel getTimesheet() {
					return timesheet;
				}
			}));
			
			fragment.add(timesheet = new TimesheetPanel("body") {
				
				@Override
				protected Project getProject() {
					return TimesheetsPage.this.getProject();
				}

				@Override
				protected TimesheetSetting getSetting() {
					return getTimesheetSetting();
				}

				@Override
				protected LocalDate getBaseDate() {
					return state.baseDate != null? state.baseDate: LocalDate.now();
				}
				
			});
			
			fragment.setOutputMarkupId(true);
			add(fragment);
		} else {
			var fragment = new Fragment("content", "noTimesheetsFrag", this);
			fragment.add(new TimesheetEditLink("newTimesheet", null) {
				
				@Override
				protected void onSave(AjaxRequestTarget target, ModalPanel modal, String name, TimesheetSetting setting) {
					setResponsePage(TimesheetsPage.class, paramsOf(getProject(), name, state.baseDate));
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(SecurityUtils.canManageIssues(getProject()));
				}
				
			});
			fragment.setOutputMarkupId(true);
			add(fragment);
		}
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		state = (State) data;
		target.add(get("content"));
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement() && project.isTimeTracking())
			return new ViewStateAwarePageLink<>(componentId, TimesheetsPage.class, TimesheetsPage.paramsOf(project, null, state.baseDate));
		else
			return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Timesheets");
	}

	private TimesheetSetting getTimesheetSetting() {
		return hierarchyTimesheetSettings.get(state.timesheetName);
	}
	
	public static PageParameters paramsOf(Project project, @Nullable String timesheetName, @Nullable LocalDate baseDate) {
		PageParameters params = paramsOf(project);
		if (timesheetName != null)
			params.add(PARAM_TIMESHEET, timesheetName);
		if (baseDate != null)
			params.add(PARAM_BASE_DATE, baseDate.toString());
		return params;
	}

	private abstract class TimesheetEditLink extends AjaxLink<Void> {
		
		private final TimesheetEditBean bean = new TimesheetEditBean();
		
		public TimesheetEditLink(String id, @Nullable String name) {
			super(id);
			
			bean.setOldName(name);
			TimesheetSetting setting;
			if (name != null)
				setting = hierarchyTimesheetSettings.get(name);
			else 
				setting = new TimesheetSetting();
			new BeanDescriptor(TimesheetSetting.class).copyProperties(setting, bean);
			bean.setName(name);
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			new BeanEditModalPanel<>(target, bean, bean.getOldName() != null? "Edit Timesheet": "Add Timesheet") {

				@Override
				protected void onSave(AjaxRequestTarget target, TimesheetEditBean bean) {
					var setting = new TimesheetSetting();
					new BeanDescriptor(TimesheetSetting.class).copyProperties(bean, setting);
					var newTimesheetSettings = new LinkedHashMap<String, TimesheetSetting>();
					for (var entry: timesheetSettings.entrySet()) {
						if (entry.getKey().equals(bean.getOldName()))
							newTimesheetSettings.put(bean.getName(), setting);
						else
							newTimesheetSettings.put(entry.getKey(), entry.getValue());
					}
					if (!newTimesheetSettings.containsKey(bean.getName()))
						newTimesheetSettings.put(bean.getName(), setting);
					timesheetSettings = newTimesheetSettings;
					getProject().getIssueSetting().setTimesheetSettings(timesheetSettings);
					getProjectManager().update(getProject());
					hierarchyTimesheetSettings = getProject().getHierarchyTimesheetSettings();
					TimesheetEditLink.this.onSave(target, this, bean.getName(), setting);
				}
			};
		}

		protected abstract void onSave(AjaxRequestTarget target, ModalPanel modal, String name, TimesheetSetting setting);
	}
	
	private static class State implements Serializable {
		String timesheetName;
		
		LocalDate baseDate;
		
	}
}
