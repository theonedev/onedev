package io.onedev.server.buildspec.job.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.ValidationException;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.FieldNamesProvider;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;

@Editable(name="Create issue", order=300)
public class CreateIssueAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;
	
	private String issueTitle;
	
	private String issueDescription;
	
	private boolean issueConfidential;
	
	private List<FieldSupply> issueFields = new ArrayList<>();
	
	@Editable(order=1000, name="Title", group="Issue Detail", description="Specify title of the issue")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getIssueTitle() {
		return issueTitle;
	}

	public void setIssueTitle(String issueTitle) {
		this.issueTitle = issueTitle;
	}
	
	@Editable(order=1050, name="Description", group="Issue Detail", description="Optionally specify description of the issue")
	@Multiline
	@Interpolative(variableSuggester="suggestVariables")
	public String getIssueDescription() {
		return issueDescription;
	}

	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}

	@Editable(order=1060, name="Confidential", group="Issue Detail", description="Whether or not the issue should be confidential")
	public boolean isIssueConfidential() {
		return issueConfidential;
	}

	public void setIssueConfidential(boolean issueConfidential) {
		this.issueConfidential = issueConfidential;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, false);
	}
	
	@Editable(order=1100, group="Issue Detail")
	@FieldNamesProvider("getFieldNames")
	@OmitName
	@Valid
	public List<FieldSupply> getIssueFields() {
		return issueFields;
	}

	public void setIssueFields(List<FieldSupply> issueFields) {
		this.issueFields = issueFields;
	}
	
	private static Collection<String> getFieldNames() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames();
	}
	
	@Override
	public void execute(Build build) {
		OneDev.getInstance(TransactionManager.class).run(new Runnable() {

			@Override
			public void run() {
				Issue issue = new Issue();
				issue.setProject(build.getProject());
				issue.setTitle(getIssueTitle());
				issue.setSubmitter(SecurityUtils.getUser());
				issue.setSubmitDate(new Date());
				SettingManager settingManager = OneDev.getInstance(SettingManager.class);
				GlobalIssueSetting issueSetting = settingManager.getIssueSetting();
				issue.setState(issueSetting.getInitialStateSpec().getName());
				
				issue.setDescription(getIssueDescription());
				issue.setConfidential(isIssueConfidential());
				for (FieldSupply supply: getIssueFields()) {
					Object fieldValue = issueSetting.getFieldSpec(supply.getName())
							.convertToObject(supply.getValueProvider().getValue());
					issue.setFieldValue(supply.getName(), fieldValue);
				}
				OneDev.getInstance(IssueManager.class).open(issue);
			}
			
		});
		
	}

	@Override
	public String getDescription() {
		return "Create issue";
	}

	@Override
	public void validateWith(BuildSpec buildSpec, Job job) {
		super.validateWith(buildSpec, job);
		
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		try {
			FieldUtils.validateFields(issueSetting.getFieldSpecMap(getFieldNames()), issueFields);
		} catch (ValidationException e) {
			throw new ValidationException("Error validating issue fields: " + e.getMessage());
		}
		
	}

}
