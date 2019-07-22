package io.onedev.server.web.component.project.choice;

import java.util.Collection;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.select2.ChoiceProvider;

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
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			list.add(cacheManager.getProject(id));
		}
		
		return list;
	}

}
