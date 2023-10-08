package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;

public class MilestoneParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public MilestoneParam(boolean optional) {
		super(MilestoneDetailPage.PARAM_MILESTONE, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		for (Milestone milestone: ParsedUrl.getProject(paramValues).getSortedHierarchyMilestones()) {
			if (milestone.getName().toLowerCase().contains(matchWith)) {
				suggestions.put(milestone.getName(), String.valueOf(milestone.getId()));
				if (--count == 0)
					break;
			}
		}
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		try {
			Long milestoneId = Long.valueOf(matchWith);
			if (OneDev.getInstance(MilestoneManager.class).get(milestoneId) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
