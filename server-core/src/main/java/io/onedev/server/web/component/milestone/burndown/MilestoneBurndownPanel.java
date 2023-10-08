package io.onedev.server.web.component.milestone.burndown;

import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.infomanager.IssueInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.WorkingPeriodField;
import io.onedev.server.web.component.chart.line.Line;
import io.onedev.server.web.component.chart.line.LineChartPanel;
import io.onedev.server.web.component.chart.line.LineSeries;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.time.LocalDate;
import java.util.*;

import static io.onedev.server.util.DateUtils.toLocalDate;
import static java.util.stream.Collectors.toList;

public class MilestoneBurndownPanel extends GenericPanel<Milestone> {

	private static final int MAX_DAYS = 365;
	
	public static final String FIELD_COUNT = "<$Count$>";

	public static final String FIELD_REMAINING_TIME = "<$Remaining Time>";

	public static final String FIELD_ESTIMATED_TIME = "<$Estimated Time>";
	
	private String field;
	
	public MilestoneBurndownPanel(String id, IModel<Milestone> milestoneModel) {
		super(id, milestoneModel);
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String message = null;
		if (getMilestone().getStartDate() != null && getMilestone().getDueDate() != null) {
			if (getMilestone().getStartDate().before(getMilestone().getDueDate())) {
				long startDay = toLocalDate(getMilestone().getStartDate()).toEpochDay();
				long dueDay = toLocalDate(getMilestone().getDueDate()).toEpochDay();
				if (dueDay - startDay >= MAX_DAYS) 
					message = "Milestone spans too long to show burndown chart";						
			} else {
				message = "Milestone start date should be before due date";
			}
		} else {
			message = "Milestone start and due date should be specified to show burndown chart";
		}
		if (message != null) {
			String messageCssClasses = "alert alert-notice alert-light-warning";
			add(new Label("content", message).add(AttributeAppender.append("class", messageCssClasses)));
		} else {
			Fragment fragment = new Fragment("content", "chartFrag", this);

			var choices = new ArrayList<String>();
			if (getMilestone().getProject().isTimeTracking() && WicketUtils.isSubscriptionActive()) {
				choices.add(FIELD_REMAINING_TIME);
				choices.add(FIELD_ESTIMATED_TIME);
			}
			choices.add(FIELD_COUNT);

			choices.addAll(getIssueSetting().getFieldSpecs().stream()
				.filter(it -> it.getType().equals(InputSpec.INTEGER) || it.getType().equals(InputSpec.WORKING_PERIOD))
				.map(FieldSpec::getName).collect(toList()));
			
			DropDownChoice<String> dropDownChoice = new DropDownChoice<>("by", new IModel<>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return getField();
				}

				@Override
				public void setObject(String object) {
					field = object;
				}

			}, choices, new IChoiceRenderer<>() {

				@Override
				public Object getDisplayValue(String object) {
					switch (object) {
						case FIELD_COUNT:
							return "Issue Count";
						case FIELD_REMAINING_TIME:
							return "Remaining Time";
						case FIELD_ESTIMATED_TIME:
							return "Estimated Time";
						default:
							return object;
					}
				}

				@Override
				public String getIdValue(String object, int index) {
					return object;
				}

				@Override
				public String getObject(String id, IModel<? extends List<? extends String>> choices) {
					return id;
				}

			});
			dropDownChoice.add(new OnChangeAjaxBehavior() {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(fragment);
				}
				
			});
			dropDownChoice.setRequired(true);
			
			fragment.add(dropDownChoice);
			
			fragment.add(new LineChartPanel("chart", new LoadableDetachableModel<>() {

				private String getXAxisValue(LocalDate date) {
					return String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
				}

				@Override
				protected LineSeries load() {
					long startDay = toLocalDate(getMilestone().getStartDate()).toEpochDay();					
					long dueDay = toLocalDate(getMilestone().getDueDate()).toEpochDay();

					Map<Long, Map<String, Integer>> dailyStateMetrics = new LinkedHashMap<>();
					for (IssueSchedule schedule : getMilestone().getSchedules()) {
						Issue issue = schedule.getIssue();
						long scheduleDay = toLocalDate(schedule.getDate()).toEpochDay();

						Map<Long, Integer> dailyMetrics = new HashMap<>();
						var issueInfoManager = OneDev.getInstance(IssueInfoManager.class);
						var fromDay = Math.max(startDay, scheduleDay);
						var dailyStates = issueInfoManager.getDailyStates(issue, fromDay, dueDay);
						if (getField().equals(FIELD_REMAINING_TIME)) {
							var estimatedTime = issue.getOwnEstimatedTime();
							for (var entry: issueInfoManager.getDailySpentTimes(issue, fromDay, dueDay).entrySet()) 
								dailyMetrics.put(entry.getKey(), estimatedTime - entry.getValue());
						} else {
							int metric = getFieldValue(issue);
							for (var entry: dailyStates.entrySet())
								dailyMetrics.put(entry.getKey(), metric);
						}
						for (var entry : dailyStates.entrySet()) {
							var day = entry.getKey();
							var state = entry.getValue();
							Map<String, Integer> stateMetrics = dailyStateMetrics.computeIfAbsent(day, k -> new HashMap<>());
							if (state != null) {
								int metric = stateMetrics.getOrDefault(state, 0);
								metric += dailyMetrics.getOrDefault(day, 0);
								stateMetrics.put(state, metric);
							}
						}
					}

					List<String> xAxisValues = new ArrayList<>();
					for (Long day : dailyStateMetrics.keySet())
						xAxisValues.add(getXAxisValue(LocalDate.ofEpochDay(day)));

					List<Line> lines = new ArrayList<>();

					int initialIssueMetric = 0;
					long today = LocalDate.now().toEpochDay();
					for (StateSpec spec : OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpecs()) {
						Map<String, Integer> yAxisValues = new HashMap<>();
						for (Map.Entry<Long, Map<String, Integer>> entry : dailyStateMetrics.entrySet()) {
							if (entry.getKey() <= today) {
								var date = LocalDate.ofEpochDay(entry.getKey());
								int metric = entry.getValue().getOrDefault(spec.getName(), 0);
								yAxisValues.put(getXAxisValue(date), metric);
							}
						}
						Integer stateMetric = yAxisValues.get(getXAxisValue(LocalDate.ofEpochDay(startDay)));
						if (stateMetric == null)
							stateMetric = 0;
						initialIssueMetric += stateMetric;
						lines.add(new Line(spec.getName(), yAxisValues, spec.getColor(), "States", null));
					}

					Map<String, Integer> guidelineYAxisValues = new HashMap<>();
					if (!xAxisValues.isEmpty()) {
						guidelineYAxisValues.put(xAxisValues.get(0), initialIssueMetric);
						guidelineYAxisValues.put(xAxisValues.get(xAxisValues.size() - 1), 0);
					}

					String color = ((BasePage) getPage()).isDarkMode() ? "white" : "black";
					lines.add(new Line("Guide Line", guidelineYAxisValues, color, null, "dashed"));

					String yAxisValueFormatter;
					if (getField().equals(FIELD_REMAINING_TIME) 
							|| getField().equals(FIELD_ESTIMATED_TIME) 
							|| getIssueSetting().getFieldSpec(getField()) instanceof WorkingPeriodField) {
						yAxisValueFormatter = ""
								+ "function(value) {"
								+ "  if (value != undefined) "
								+ "    return onedev.server.util.formatWorkingPeriod(value);"
								+ "  else "
								+ "    return '-';"
								+ "}";
					} else {
						yAxisValueFormatter = null;
					}
					return new LineSeries(null, xAxisValues, lines, yAxisValueFormatter, null, null);
				}

			}));

			var aggregationLink = OneDev.getInstance(SettingManager.class).getIssueSetting()
					.getTimeTrackingSetting().getAggregationLink();
			fragment.add(new Label("message", new AbstractReadOnlyModel<String>() {
				@Override
				public String getObject() {
					if (aggregationLink != null && (getField().equals(FIELD_ESTIMATED_TIME) || getField().equals(FIELD_REMAINING_TIME)))
						return"To avoid duplication, estimated/remaining time showing here does not include those aggregated from '" + aggregationLink + "'";
					else
						return null;
				}
			}) {
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(getDefaultModelObject() != null);
				}
			});
			
			fragment.setOutputMarkupId(true);
			add(fragment);
		}
	}
	
	private String getField() {
		if (field != null)
			return field;
		else if (getMilestone().getProject().isTimeTracking() && WicketUtils.isSubscriptionActive())
			return FIELD_REMAINING_TIME;
		else
			return FIELD_COUNT;
	}
	
	private int getFieldValue(Issue issue) {
		if (getField().equals(FIELD_ESTIMATED_TIME)) {
			return issue.getOwnEstimatedTime();
		} else if (getField().equals(FIELD_COUNT)) {
			return 1;
		} else {
			Object value = issue.getFieldValue(getField());
			if (value != null) {
				if (value instanceof Integer)
					return (int) value;
				else
					return 0;
			} else {
				return 0;
			}
		}
	}
	
	private Milestone getMilestone() {
		return getModelObject();
	}
	
}
