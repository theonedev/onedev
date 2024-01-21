package io.onedev.server.model.support.issue;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.RadioChoice;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

import static io.onedev.server.model.Issue.NAME_PROJECT;

@Editable
public class TimesheetSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum DateRangeType {MONTH, WEEK};
	
	public enum RowType {ISSUES, USERS};
	
	private RowType rowType = RowType.ISSUES;
	
	private String issueQuery;
	
	private DateRangeType dateRangeType = DateRangeType.MONTH;
	
	private String groupBy = NAME_PROJECT;

	@Editable(order=100, name="Filter Issues", placeholder = "All issues")
	@IssueQuery(withCurrentProjectCriteria = true)
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(@Nullable String issueQuery) {
		this.issueQuery = issueQuery;
	}

	@Editable(order=150, name="Show Works Of")
	@RadioChoice
	@NotNull
	public RowType getRowType() {
		return rowType;
	}

	public void setRowType(RowType rowType) {
		this.rowType = rowType;
	}

	@Editable(order=200, name="Time Range")
	@RadioChoice
	@NotNull
	public TimesheetSetting.DateRangeType getDateRangeType() {
		return dateRangeType;
	}

	public void setDateRangeType(DateRangeType dateRangeType) {
		this.dateRangeType = dateRangeType;
	}

	@Editable(order=400, name="Group By", placeholder = "No group by")
	@ChoiceProvider("getGroupByChoices")
	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	
	private static List<String> getGroupByChoices() {
		var choices = Lists.newArrayList(NAME_PROJECT);
		for (var fieldSpec: OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpecs()) {
			if (!fieldSpec.isAllowMultiple() && fieldSpec instanceof ChoiceField)
				choices.add(fieldSpec.getName());
		}
		return choices;
	}
}
