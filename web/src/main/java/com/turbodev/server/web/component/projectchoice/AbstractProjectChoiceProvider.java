package com.turbodev.server.web.component.projectchoice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.util.facade.ProjectFacade;
import com.turbodev.server.web.component.select2.ChoiceProvider;

@SuppressWarnings("serial")
public abstract class AbstractProjectChoiceProvider extends ChoiceProvider<ProjectFacade> {

	@Override
	public void toJson(ProjectFacade choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId());
		writer.key("name");
		writer.value(StringEscapeUtils.escapeHtml4(choice.getName()));
	}
	
	@Override
	public Collection<ProjectFacade> toChoices(Collection<String> ids) {
		List<ProjectFacade> list = Lists.newArrayList();
		CacheManager cacheManager = TurboDev.getInstance(CacheManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(cacheManager.getProject(id));
		}
		
		return list;
	}

}
