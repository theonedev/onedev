package io.onedev.server.ee.timetracking;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.*;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.project.ProjectAvatar;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import org.apache.commons.codec.binary.Hex;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

import static io.onedev.server.model.Issue.NAME_PROJECT;
import static io.onedev.server.model.support.issue.TimesheetSetting.DateRangeType.MONTH;
import static io.onedev.server.model.support.issue.TimesheetSetting.DateRangeType.WEEK;
import static io.onedev.server.model.support.issue.TimesheetSetting.RowType.ISSUES;
import static io.onedev.server.model.support.issue.TimesheetSetting.RowType.USERS;
import static io.onedev.server.util.DateUtils.formatWorkingPeriod;
import static io.onedev.server.web.component.user.ident.Mode.AVATAR_AND_NAME;
import static java.util.stream.Collectors.toList;

public abstract class TimesheetPanel extends Panel {
	
	private final IModel<LocalDate> fromDateModel = new LoadableDetachableModel<>() {
		@Override
		protected LocalDate load() {
			var fromDate = getBaseDate();
			if (fromDate == null)
				fromDate = LocalDate.now();
			if (getSetting().getDateRangeType() == WEEK) {
				var firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
				fromDate = fromDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
			} else {
				fromDate = fromDate.with(TemporalAdjusters.firstDayOfMonth());
			}
			return fromDate;
		}
	};

	private final IModel<LocalDate> toDateModel = new LoadableDetachableModel<>() {
		@Override
		protected LocalDate load() {
			var toDate = getBaseDate();
			if (toDate == null)
				toDate = LocalDate.now();
			if (getSetting().getDateRangeType() == WEEK) {
				var firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
				var lastDayOfWeekValue = firstDayOfWeek.getValue() - 1;
				if (lastDayOfWeekValue < 1)
					lastDayOfWeekValue = 7;
				toDate = toDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(lastDayOfWeekValue)));
			} else {
				toDate = toDate.with(TemporalAdjusters.lastDayOfMonth());
			}
			return toDate;
		}
	};
	
	private final IModel<List<LocalDate>> datesModel = new LoadableDetachableModel<>() {
		@Override
		protected List<LocalDate> load() {
			var dates = new ArrayList<LocalDate>();
			for (long epochDay = getFromDate().toEpochDay(); epochDay <= getToDate().toEpochDay(); epochDay++)
				dates.add(LocalDate.ofEpochDay(epochDay));
			return dates;
		}
	};
	
	private final IModel<List<Row>> rowsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<Row> load() {
			var works = queryWorks(getFromDate().toEpochDay(), getToDate().toEpochDay());
			var rows = new ArrayList<Row>();
			var totalRow = new TotalRow();
			rows.add(totalRow);
			var groupRows = new HashMap<String, GroupRow>();
			var itemRows = new HashMap<String, ItemRow>();
			
			for (var work: works) {
				totalRow.spentTimes.merge(work.getDay(), work.getMinutes(), Integer::sum);
				
				if (getSetting().getGroupBy() != null) {
					String group;
					if (getSetting().getGroupBy().equals(NAME_PROJECT)) {
						group = work.getIssue().getProject().getId().toString();
					} else {
						var fieldValue = work.getIssue().getFieldValue(getSetting().getGroupBy());
						if (fieldValue instanceof String)
							group = (String) fieldValue;
						else
							group = null;
					}
					if (group != null) {
						var groupRow = groupRows.get(group);
						if (groupRow == null) {
							groupRow = new GroupRow();
							groupRow.group = group;
							groupRows.put(group, groupRow);
							rows.add(groupRow);
						}
						groupRow.spentTimes.merge(work.getDay(), work.getMinutes(), Integer::sum);

						var item = getSetting().getRowType() == ISSUES? work.getIssue().getId(): work.getUser().getId();
						var itemKey = Hex.encodeHexString(group.getBytes(StandardCharsets.UTF_8)) + ":" + item;
						var itemRow = itemRows.get(itemKey);
						if (itemRow == null) {
							itemRow = new ItemRow();
							itemRow.item = item;
							itemRows.put(itemKey, itemRow);
							for (var index = rows.indexOf(groupRow) + 1; index < rows.size(); index++) {
								if (rows.get(index) instanceof GroupRow) {
									rows.add(index, itemRow);
									break;
								}
							}
							if (!rows.contains(itemRow))
								rows.add(itemRow);
						}
						itemRow.spentTimes.merge(work.getDay(), work.getMinutes(), Integer::sum);
					}
				} else {
					var item = getSetting().getRowType() == ISSUES? work.getIssue().getId(): work.getUser().getId();
					var itemKey = item.toString();
					var itemRow = itemRows.get(itemKey);
					if (itemRow == null) {
						itemRow = new ItemRow();
						itemRow.item = item;
						itemRows.put(itemKey, itemRow);
						rows.add(itemRow);
					}
					itemRow.spentTimes.merge(work.getDay(), work.getMinutes(), Integer::sum);
				}
			}
			if (NAME_PROJECT.equals(getSetting().getGroupBy()) 
					&& groupRows.size() == 1
					&& getProject() != null
					&& groupRows.keySet().iterator().next().equals(getProject().getId().toString())) {
				rows.removeIf(it -> it instanceof GroupRow);				
			}
			return rows;
		}
		
	};

	public TimesheetPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("dateRange", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return getFromDate().toString() + " &rarr; " + getToDate().toString();
			}
		}).setEscapeModelStrings(false));
		
		add(new ListView<>("days", datesModel) {

			@Override
			protected void populateItem(ListItem<LocalDate> item) {
				var date = item.getModelObject();
				item.add(new Label("day", formatWeekDay(date)));
				if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
					item.add(AttributeAppender.append("class", " weekend"));
			}
		});
		
		add(new ListView<Row>("rows", rowsModel) {

			@Override
			protected void populateItem(ListItem<Row> item) {
				var row = item.getModelObject();
				item.add(row.renderSummary("summary"));
				item.add(new ListView<>("days", datesModel) {

					@Override
					protected void populateItem(ListItem<LocalDate> item) {
						var date = item.getModelObject();
						item.add(row.renderSpentTime("spentTime", date.toEpochDay()));
						if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
							item.add(AttributeAppender.append("class", " weekend"));
					}
				});
			}
		});
		
		var aggregationLink = OneDev.getInstance(SettingManager.class).getIssueSetting()
				.getTimeTrackingSetting().getAggregationLink();
		if (aggregationLink != null)
			add(new Label("message", "To avoid duplication, spent time showing here does not include those aggregated from '" + aggregationLink + "'"));
		else 
			add(new WebMarkupContainer("message").setVisible(false));
		setOutputMarkupId(true);
	}
	
	private String formatWeekDay(LocalDate date) {
		var weekDay = date.getDayOfWeek().toString();
		weekDay = weekDay.substring(0, 1) + weekDay.substring(1, 3).toLowerCase();
		return String.format("%d %s", date.getDayOfMonth(), weekDay);
	}
	
	public void exportAsXlsx(String title, OutputStream os) {
		var version = StringUtils.substringBeforeLast(OneDev.getInstance().getVersion(), ".");
		try (
				var workBook = new Workbook(os, "OneDev", version); 
			 	var worksheet = workBook.newWorksheet(title)) {
			worksheet.range(0, 0, 0, 2).merge();
			worksheet.value(0, 0, getFromDate().toString() + " -> " + getToDate().toString());
	
			var colIndex = 3;
			for (var date : datesModel.getObject())
				worksheet.value(0, colIndex++, formatWeekDay(date));

			var rowIndex = 1;
			for (var row: rowsModel.getObject()) 
				row.fill(worksheet, rowIndex++);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onDetach() {
		fromDateModel.detach();
		toDateModel.detach();
		datesModel.detach();
		rowsModel.detach();
		super.onDetach();
	}
	
	private List<IssueWork> queryWorks(long fromDay, long toDay) {
		IssueQueryParseOption option = new IssueQueryParseOption();
		if (getProject() != null)
			option.withCurrentProjectCriteria(true);
		return OneDev.getInstance(IssueWorkManager.class).query(
						getProject() != null? new ProjectScope(getProject(), false, true): null,
						IssueQuery.parse(getProject(), getSetting().getIssueQuery(), option, true),
						fromDay, toDay);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TimesheeCssResourceReference()));
	}

	@Nullable
	protected abstract Project getProject();
	
	protected abstract TimesheetSetting getSetting();
	
	protected abstract LocalDate getBaseDate();
	
	private LocalDate getFromDate() {
		return fromDateModel.getObject();
	}
	
	private LocalDate getToDate() {
		return toDateModel.getObject();
	}

	private abstract class Row implements Serializable {
		
		Map<Long, Integer> spentTimes = new HashMap<>();
		
		int getToalSpentTime() {
			return spentTimes.values().stream().reduce(0, Integer::sum);
		}
		
		void fillSpentTimes(Worksheet worksheet, int rowIndex, boolean bold) {
			for (var entry: spentTimes.entrySet()) {
				var colIndex = (int)(entry.getKey() - getFromDate().toEpochDay()) + 3;
				worksheet.value(rowIndex, colIndex, formatWorkingPeriod(entry.getValue()));
				if (bold)
					worksheet.style(rowIndex, colIndex).bold().set();
			}
		}
		
		abstract Component renderSummary(String componentId);
		
		abstract Component renderSpentTime(String componentId, long day);
		
		abstract void fill(Worksheet worksheet, int rowIndex);
	}
	
	private class TotalRow extends Row {

		@Override
		Component renderSummary(String componentId) {
			var fragment = new Fragment(componentId, "totalSummaryFrag", TimesheetPanel.this);
			fragment.add(new Label("spentTime", formatWorkingPeriod(getToalSpentTime())));
			return fragment;
		}

		@Override
		Component renderSpentTime(String componentId, long day) {
			var spentTime = spentTimes.getOrDefault(day, 0);
			if (spentTime != 0) {
				return new Label(componentId, formatWorkingPeriod(spentTime))
						.add(AttributeAppender.append("class", "font-weight-bold"));
			} else {
				return new WebMarkupContainer(componentId);
			}
		}

		@Override
		void fill(Worksheet worksheet, int rowIndex) {
			worksheet.range(rowIndex, 0, rowIndex, 1).merge();
			worksheet.value(rowIndex, 0, "Total time");
			worksheet.style(rowIndex, 0).bold().set();
			worksheet.value(rowIndex, 2, formatWorkingPeriod(getToalSpentTime()));
			worksheet.style(rowIndex, 2).bold().set();
			fillSpentTimes(worksheet, rowIndex, true);
		}

	}
	
	private class GroupRow extends Row {

		String group;
		
		@Override
		Component renderSummary(String componentId) {
			Fragment fragment;
			if (getSetting().getGroupBy().equals(NAME_PROJECT)) {
				var projectId = Long.valueOf(group);
				fragment = new Fragment(componentId, "projectSummaryFrag", TimesheetPanel.this);
				var link = new BookmarkablePageLink<Void>("project", ProjectBlobPage.class, ProjectBlobPage.paramsOf(projectId));
				link.add(new ProjectAvatar("avatar", projectId));
				
				var project = OneDev.getInstance(ProjectManager.class).load(projectId);
				var path = StringUtils.abbreviate(project.getPath(), getMaxLen());
				link.add(new Label("path", path));
				if (!path.equals(project.getPath()))
					link.add(AttributeAppender.append("title", project.getPath()));
				fragment.add(link);
			} else {
				fragment = new Fragment(componentId, "fieldValueSummaryFrag", TimesheetPanel.this);
				fragment.add(new Label("fieldValue", group));
			}
			fragment.add(new Label("spentTime", formatWorkingPeriod(getToalSpentTime())));
			return fragment;
		}

		@Override
		Component renderSpentTime(String componentId, long day) {
			var spentTime = spentTimes.getOrDefault(day, 0);
			if (spentTime != 0) {
				return new Label(componentId, formatWorkingPeriod(spentTime))
						.add(AttributeAppender.append("class", "font-weight-bold"));
			} else {
				return new WebMarkupContainer(componentId);
			}
		}

		@Override
		void fill(Worksheet worksheet, int rowIndex) {
			worksheet.range(rowIndex, 0, rowIndex, 1).merge();
			if (getSetting().getGroupBy().equals(NAME_PROJECT)) {
				var projectId = Long.valueOf(group);
				var project = OneDev.getInstance(ProjectManager.class).load(projectId);
				worksheet.value(rowIndex, 0, project.getPath());
			} else {
				worksheet.value(rowIndex, 0, group);
			}
			worksheet.style(rowIndex, 0).bold().set();
			worksheet.value(rowIndex, 2, formatWorkingPeriod(getToalSpentTime()));
			worksheet.style(rowIndex, 2).bold().set();
			fillSpentTimes(worksheet, rowIndex, true);
		}

	}

	private class ItemRow extends Row {

		Long item;
		
		@Override
		Component renderSummary(String componentId) {
			Fragment fragment;
			if (getSetting().getRowType() == ISSUES) {
				fragment = new Fragment(componentId, "issueSummaryFrag", TimesheetPanel.this);
				var issue = OneDev.getInstance(IssueManager.class).load(item);
				var link = new BookmarkablePageLink<Void>("issue", IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue));
				link.add(new Label("number", getIssueNumber(issue, getMaxLen() / 2)));
				var title = StringUtils.abbreviate(issue.getTitle(), getMaxLen());
				link.add(new Label("title", title));
				if (!title.equals(issue.getTitle()))
					link.add(AttributeAppender.append("title", issue.getTitle()));
				fragment.add(link);
			} else {
				fragment = new Fragment(componentId, "userSummaryFrag", TimesheetPanel.this);
				fragment.add(new UserIdentPanel("user", OneDev.getInstance(UserManager.class).load(item), AVATAR_AND_NAME));
			}
			fragment.add(new Label("spentTime", formatWorkingPeriod(getToalSpentTime())));
			return fragment;
		}

		private Component renderNote(String componentId, IssueWork work) {
			if (work.getNote() != null)
				return new WebMarkupContainer(componentId).add(AttributeAppender.append("title", work.getNote()));
			else
				return new WebMarkupContainer(componentId).setVisible(false);
		}
		
		@Override
		Component renderSpentTime(String componentId, long day) {
			var spentTime = spentTimes.getOrDefault(day, 0);
			if (spentTime != 0) {
				var fragment = new Fragment(componentId, "itemSpentTimeFrag", TimesheetPanel.this);
				var link = new DropdownLink("link") {
					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						if (getSetting().getRowType() == ISSUES) {
							var fragment = new Fragment(id, "issueWorksFrag", TimesheetPanel.this);
							fragment.add(new ListView<IssueWork>("works", new LoadableDetachableModel<>() {
								@Override
								protected List<IssueWork> load() {
									return queryWorks(day, day).stream()
											.filter(it -> it.getIssue().getId().equals(item))
											.collect(toList());
								}

							}) {

								@Override
								protected void populateItem(ListItem<IssueWork> item) {
									var work = item.getModelObject();
									item.add(new UserIdentPanel("user", work.getUser(), AVATAR_AND_NAME));
									item.add(new Label("spentTime", formatWorkingPeriod(work.getMinutes())));
									item.add(renderNote("note", work));
								}

							});
							return fragment;
						} else {
							var fragment = new Fragment(id, "userWorksFrag", TimesheetPanel.this);
							fragment.add(new ListView<IssueWork>("works", new LoadableDetachableModel<>() {
								@Override
								protected List<IssueWork> load() {
									return queryWorks(day, day).stream()
											.filter(it -> it.getUser().getId().equals(item))
											.collect(toList());
								}

							}) {

								@Override
								protected void populateItem(ListItem<IssueWork> item) {
									var work = item.getModelObject();
									var issue = work.getIssue();
									var link = new BookmarkablePageLink<Void>("issue", IssueActivitiesPage.class,
											IssueActivitiesPage.paramsOf(issue));
									link.add(new Label("number", getIssueNumber(issue, Integer.MAX_VALUE)));
									link.add(new Label("title", issue.getTitle()));
									item.add(link);
									item.add(new Label("spentTime", formatWorkingPeriod(work.getMinutes())));
									item.add(renderNote("note", work));
								}

							});
							return fragment;
						}
					}
				};
				link.add(new Label("label", formatWorkingPeriod(spentTime)));
				fragment.add(link);

				return fragment;
			} else {
				return new WebMarkupContainer(componentId);
			}
		}

		@Override
		void fill(Worksheet worksheet, int rowIndex) {
			if (getSetting().getRowType() == USERS) {
				var user = OneDev.getInstance(UserManager.class).load(item);
				worksheet.value(rowIndex, 1, user.getDisplayName());
			} else {
				var issue = OneDev.getInstance(IssueManager.class).load(item);
				worksheet.value(rowIndex, 1, getIssueNumber(issue, Integer.MAX_VALUE) + " " + issue.getTitle());
			}
			worksheet.value(rowIndex, 2, formatWorkingPeriod(getToalSpentTime()));
			fillSpentTimes(worksheet, rowIndex, false);
		}

		private String getIssueNumber(Issue issue, int maxLength) {
			if (NAME_PROJECT.equals(getSetting().getGroupBy()) || issue.getProject().equals(getProject()))
				return "#" + issue.getNumber();
			else 
				return StringUtils.abbreviate(issue.getProject().getPath(), maxLength) + "#" + issue.getNumber();
		}
	}
	
	private int getMaxLen() {
		if (getSetting().getDateRangeType() == MONTH)
			return 50;
		else
			return 100;
	}
	
}
