package io.onedev.server.web.component.groupchoice;

import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.web.component.select2.ChoiceProvider;

public abstract class AbstractGroupChoiceProvider extends ChoiceProvider<GroupFacade> {

	private static final long serialVersionUID = 1L;

	@Override
	public void toJson(GroupFacade choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(choice.getName());
	}

	@Override
	public Collection<GroupFacade> toChoices(Collection<String> ids) {
		List<GroupFacade> groups = Lists.newArrayList();
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			groups.add(cacheManager.getGroup(id));
		}

		return groups;
	}

}