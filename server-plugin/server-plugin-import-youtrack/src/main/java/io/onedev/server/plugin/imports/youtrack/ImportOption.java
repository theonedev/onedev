package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable
public class ImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<IssueStateMapping> issueStateMappings = new ArrayList<>();
	
	private List<IssueFieldMapping> issueFieldMappings = new ArrayList<>();
	
	private List<IssueTagMapping> issueTagMappings = new ArrayList<>();
	
	private List<IssueLinkMapping> issueLinkMappings = new ArrayList<>();
	
	@Editable(order=200, description="Specify how to map YouTrack issue state to OneDev issue state. "
			+ "Unmapped states will use the initial state in OneDev.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue states in case there is no appropriate option here")
	public List<IssueStateMapping> getIssueStateMappings() {
		return issueStateMappings;
	}

	public void setIssueStateMappings(List<IssueStateMapping> issueStateMappings) {
		this.issueStateMappings = issueStateMappings;
	}

	@Editable(order=300, description="Specify how to map YouTrack issue fields to OneDev. Unmapped fields will "
			+ "be reflected in issue description.<br>"
			+ "<b>Note: </b>"
			+ "<ul>"
			+ "<li>Enum field needs to be mapped in form of <tt>&lt;Field Name&gt;::&lt;Field Value&gt;</tt>, "
			+ "for instance <tt>Priority::Critical</tt>"
			+ "<li>You may customize OneDev issue fields in case there is no appropriate option here"
			+ "</ul>")
	public List<IssueFieldMapping> getIssueFieldMappings() {
		return issueFieldMappings;
	}

	public void setIssueFieldMappings(List<IssueFieldMapping> issueFieldMappings) {
		this.issueFieldMappings = issueFieldMappings;
	}

	@Editable(order=300, description="Specify how to map YouTrack issue tags to OneDev issue custom "
			+ "fields.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue fields in case there is no appropriate option here")
	public List<IssueTagMapping> getIssueTagMappings() {
		return issueTagMappings;
	}

	public void setIssueTagMappings(List<IssueTagMapping> issueTagMappings) {
		this.issueTagMappings = issueTagMappings;
	}

	@Editable(order=400, description="Specify how to map YouTrack issue links to OneDev issue links.<br>"
			+ "<b>NOTE: </b> You may customize OneDev issue links in case there is no appropriate option here")
	public List<IssueLinkMapping> getIssueLinkMappings() {
		return issueLinkMappings;
	}

	public void setIssueLinkMappings(List<IssueLinkMapping> issueLinkMappings) {
		this.issueLinkMappings = issueLinkMappings;
	}

}
