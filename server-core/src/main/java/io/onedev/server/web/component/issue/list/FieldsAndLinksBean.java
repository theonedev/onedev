package io.onedev.server.web.component.issue.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class FieldsAndLinksBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> fields;
	
	private List<String> links;

	@Editable(order=100, name="Display Fields", description="Specify fields to be displayed in the issue list")
	@ChoiceProvider("getFieldChoices")
	@NameOfEmptyValue("Not displaying any fields")
	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> choices = new ArrayList<>();
		choices.add(Issue.NAME_STATE);
		for (String fieldName: OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldNames())
			choices.add(fieldName);
		return choices;
	}
	
	@Editable(order=200, name="Display Links", description="Specify links to be displayed in the issue list")
	@ChoiceProvider("getLinkChoices")
	@NameOfEmptyValue("Not displaying any links")
	public List<String> getLinks() {
		return links;
	}

	public void setLinks(List<String> links) {
		this.links = links;
	}

	@SuppressWarnings("unused")
	private static List<String> getLinkChoices() {
		List<String> choices = new ArrayList<>();
		for (LinkSpec linkSpec: OneDev.getInstance(LinkSpecManager.class).queryAndSort()) {
			choices.add(linkSpec.getName());
			if (linkSpec.getOpposite() != null)
				choices.add(linkSpec.getOpposite().getName());
		}
		return choices;
	}
	
}
