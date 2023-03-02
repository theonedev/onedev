package io.onedev.server.model.support.pullrequest.changedata;

import io.onedev.server.notification.ActivityDetail;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PullRequestDescriptionChangeData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldDescription;
	
	private final String newDescription;
	
	public PullRequestDescriptionChangeData(@Nullable String oldDescription, @Nullable String newDescription) {
		if (oldDescription == null)
			oldDescription = "";
		this.oldDescription = oldDescription;
		if (newDescription == null)
			newDescription = "";
		this.newDescription = newDescription;
	}
	
	@Override
	public String getActivity() {
		return "changed description";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldFieldValues = new HashMap<>();
		oldFieldValues.put("Description", oldDescription);
		Map<String, String> newFieldValues = new HashMap<>();
		newFieldValues.put("Description", newDescription);
		return ActivityDetail.compare(oldFieldValues, newFieldValues, true);
	}
	
}
