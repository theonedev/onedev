package io.onedev.server.web.component.milestone.burndown;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.infomanager.IssueInfoManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.field.spec.WorkingPeriodField;
import io.onedev.server.util.Day;
import io.onedev.server.web.component.chart.line.Line;
import io.onedev.server.web.component.chart.line.LineChartPanel;
import io.onedev.server.web.component.chart.line.LineSeries;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class MilestoneBurndownPanel extends GenericPanel<Milestone> {

	private static final int MAX_DAYS = 365;
	
	public static final String NULL_FIELD = "<$Null Field$>";

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
				int currentDay = new Day(getMilestone().getStartDate()).getValue();
				int toDay = new Day(getMilestone().getDueDate()).getValue();
				
				int dayCount = 0;
				while (currentDay <= toDay) {
					currentDay = new Day(new Day(currentDay).getDate().plusDays(1)).getValue();
					if (++dayCount > MAX_DAYS) {
						message = "Milestone spans too long to show burndown chart";						
						break;
					}
				}
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

			List<String> choices = getIssueSetting().getFieldSpecs().stream()
				.filter(it -> it.getType().equals(InputSpec.INTEGER) || it.getType().equals(InputSpec.WORKING_PERIOD))
				.map(it->it.getName())
				.collect(Collectors.toList());
			
			choices.add(0, NULL_FIELD);
			
			DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("by", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					if (field != null)
						return field;
					else
						return NULL_FIELD;
				}

				@Override
				public void setObject(String object) {
					if (!object.equals(NULL_FIELD))
						field = object;
					else
						field = null;
				}
				
			}, choices, new IChoiceRenderer<String>() {

				@Override
				public Object getDisplayValue(String object) {
					if (!object.equals(NULL_FIELD))
						return object;
					else
						return "Issue Count";
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
					target.add(fragment.get("chart"));
				}
				
			});
			dropDownChoice.setRequired(true);
			
			fragment.add(dropDownChoice);
			
			fragment.add(new LineChartPanel("chart", new LoadableDetachableModel<LineSeries>() {

				private String getXAxisValue(Day day) {
					return String.format("%02d-%02d", day.getMonthOfYear()+1, day.getDayOfMonth());
				}
				
				@Override
				protected LineSeries load() {
					int startDayValue = new Day(getMilestone().getStartDate()).getValue();
					int dueDayValue = new Day(getMilestone().getDueDate()).getValue();
					
					Map<Integer, Map<String, Integer>> dailyStateWeights = new LinkedHashMap<>();
					for (IssueSchedule schedule: getMilestone().getSchedules()) {
						Issue issue = schedule.getIssue();
						int issueWeight = getFieldValue(issue);
						int scheduleDayValue = new Day(schedule.getDate()).getValue();
						Map<Integer, String> dailyStates = OneDev.getInstance(IssueInfoManager.class)
								.getDailyStates(issue, Math.max(startDayValue, scheduleDayValue), dueDayValue);
						for (Map.Entry<Integer, String> entry: dailyStates.entrySet()) {
							Map<String, Integer> stateWeights = dailyStateWeights.get(entry.getKey());
							if (stateWeights == null) {
								stateWeights = new HashMap<>();
								dailyStateWeights.put(entry.getKey(), stateWeights);
							}
							if (entry.getValue() != null) {
								Integer weight = stateWeights.get(entry.getValue());
								if (weight == null)
									weight = 0;
								weight += issueWeight;
								stateWeights.put(entry.getValue(), weight);
							}
						}
					}

					List<String> xAxisValues = new ArrayList<>();
					for (Integer dayValue: dailyStateWeights.keySet()) 
						xAxisValues.add(getXAxisValue(new Day(dayValue)));
					
					List<Line> lines = new ArrayList<>();
					
					int initialIssueWeight = 0;
					int todayValue = new Day(new Date()).getValue();
					for (StateSpec spec: OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpecs()) {
						Map<String, Integer> yAxisValues = new HashMap<>();
						for (Map.Entry<Integer, Map<String, Integer>> entry: dailyStateWeights.entrySet()) {
							if (entry.getKey() <= todayValue) {
								Day day = new Day(entry.getKey());
								Integer weight = entry.getValue().get(spec.getName());
								if (weight == null)
									weight = 0;
								yAxisValues.put(getXAxisValue(day), weight);
							}
						}
						Integer stateWeight = yAxisValues.get(getXAxisValue(new Day(startDayValue)));
						if (stateWeight == null)
							stateWeight = 0;
						initialIssueWeight += stateWeight;
						lines.add(new Line(spec.getName(), yAxisValues, spec.getColor(), "States", null));
					}

					Map<String, Integer> guidelineYAxisValues = new HashMap<>();
					if (!xAxisValues.isEmpty()) {
						guidelineYAxisValues.put(xAxisValues.get(0), initialIssueWeight);
						guidelineYAxisValues.put(xAxisValues.get(xAxisValues.size()-1), 0);
					}
					
					String color = ((BasePage)getPage()).isDarkMode()?"white":"black";
					lines.add(new Line("Guide Line", guidelineYAxisValues, color, null, "dashed"));
					
					String yAxisValueFormatter;
					if (field != null && getIssueSetting().getFieldSpec(field) instanceof WorkingPeriodField) {
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
					return new LineSeries(null, xAxisValues, lines, yAxisValueFormatter, 0, null);
				}
				
			}).setOutputMarkupId(true));
			
			add(fragment);
		}
	}
	
	private int getFieldValue(Issue issue) {
		if (field != null) {
			Object value = issue.getFieldValue(field);
			if (value != null) {
				if (value instanceof Integer)
					return (int) value;
				else
					return 0;
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}
	
	private Milestone getMilestone() {
		return getModelObject();
	}
	
}
