package io.onedev.server.model.support.issue;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.util.usage.Usage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Editable
public class TimeTrackingSetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String aggregationLink;
	
	@Editable(order=500, placeholder = "No aggregation", description = "If specified, total estimated/spent time " +
			"of an issue will also include linked issues of this type")
	@ChoiceProvider("getLinkChoices")
	public String getAggregationLink() {
		return aggregationLink;
	}

	public void setAggregationLink(String aggregationLink) {
		this.aggregationLink = aggregationLink;
	}
	
	private static List<String> getLinkChoices() {
		var choices = new LinkedHashSet<String>();
		for (var linkSpec: OneDev.getInstance(LinkSpecManager.class).query()) {
			if (linkSpec.getOpposite() != null) {
				choices.add(linkSpec.getName());
				choices.add(linkSpec.getOpposite().getName());
			}
		}
		return new ArrayList<>(choices);
	}

	private static GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}

	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		if (linkName.equals(aggregationLink))
			usage.add("time aggregation link");
		return usage;
	}

	public void onRenameLink(String oldName, String newName) {
		if (oldName.equals(aggregationLink))
			aggregationLink = newName;
	}
	
}
