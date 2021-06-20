package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class YouTrackIssueImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<IssueStateMapping> issueStateMappings = new ArrayList<>();
	
	private List<IssueFieldMapping> issueFieldMappings = new ArrayList<>();
	
	private List<IssueTagMapping> issueTagMappings = new ArrayList<>();
	
	@Editable(order=200, description="Specify how to map YouTrack issue state to OneDev issue state. "
			+ "Unmapped states will use the initial state in OneDev")
	public List<IssueStateMapping> getIssueStateMappings() {
		return issueStateMappings;
	}

	public void setIssueStateMappings(List<IssueStateMapping> issueStateMappings) {
		this.issueStateMappings = issueStateMappings;
	}

	@Editable(order=300, description="Specify how to map YouTrack issue custom fields to OneDev issue "
			+ "custom fields. Unmapped fields will be reflected in issue description.<br>"
			+ "<b>Note: </b>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, "
			+ "for instance <tt>Priority::Critical</tt>")
	public List<IssueFieldMapping> getIssueFieldMappings() {
		return issueFieldMappings;
	}

	public void setIssueFieldMappings(List<IssueFieldMapping> issueFieldMappings) {
		this.issueFieldMappings = issueFieldMappings;
	}

	@Editable(order=300, description="Specify how to map YouTrack issue tags to OneDev issue custom "
			+ "fields. Only multi-valued enum field can be used here. Unmapped tags will be "
			+ "reflected in issue description")
	public List<IssueTagMapping> getIssueTagMappings() {
		return issueTagMappings;
	}

	public void setIssueTagMappings(List<IssueTagMapping> issueTagMappings) {
		this.issueTagMappings = issueTagMappings;
	}

}
