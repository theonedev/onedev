package io.onedev.server.model.support.inputspec.userchoiceinput.defaultvalueprovider;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.UserChoice;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="Literal default value")
	@UserChoice("getValueChoices")
	@NotEmpty
	@OmitName
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getDefaultValue() {
		return getValue();
	}

	@SuppressWarnings("unused")
	private static List<User> getValueChoices() {
		UserCache cache = OneDev.getInstance(UserManager.class).cloneCache();		
		List<User> users = new ArrayList<>(cache.getUsers());
		users.sort(cache.comparingDisplayName(Sets.newHashSet()));
		return users;
	}
	
}
