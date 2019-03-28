package io.onedev.server.web.component.user.choice;

import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.util.avatar.AvatarManager;

public abstract class AbstractUserChoiceProvider extends ChoiceProvider<UserFacade> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void toJson(UserFacade choice, JSONWriter writer) throws JSONException {
		writer.key("id").value(choice.getId()).key("name").value(HtmlEscape.escapeHtml5(choice.getDisplayName()));
		String avatarUrl = OneDev.getInstance(AvatarManager.class).getAvatarUrl(UserIdent.of(choice));
		writer.key("avatar").value(avatarUrl);
	}

	@Override
	public Collection<UserFacade> toChoices(Collection<String> ids) {
		List<UserFacade> users = Lists.newArrayList();
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		for (String each : ids) {
			Long id = Long.valueOf(each);
			users.add(cacheManager.getUser(id));
		}

		return users;
	}

}