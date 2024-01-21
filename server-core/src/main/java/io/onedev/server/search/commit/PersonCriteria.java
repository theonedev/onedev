package io.onedev.server.search.commit;

import com.google.common.base.Preconditions;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.match.WildcardUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import java.util.ArrayList;
import java.util.List;

public abstract class PersonCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public PersonCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}

	private boolean matches(String value, PersonIdent person) {
		String formatted = String.format("%s <%s>", person.getName(), person.getEmailAddress());
		return WildcardUtils.matchString(value, formatted);
	}

	private static UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	protected void fill(Project project, List<String> persons) {
		for (String value: values) {
			if (value == null) { // authored by me
				User user = SecurityUtils.getUser();
				if (user != null) {
					user.getEmailAddresses().stream().filter(it->it.isVerified()).forEach(it-> {
						persons.add("<" + it.getValue() + ">");
					});
				} else {
					throw new ExplicitException("Please login to perform this query");
				}
			} else if (value.startsWith("@")) {
				String userName = value.substring(1);
				User user = getUserManager().findByName(userName);
				if (user != null) {
					for (EmailAddress emailAddress: user.getEmailAddresses()) {
						if (emailAddress.isVerified())
							persons.add("<" + emailAddress.getValue() + ">");
					}
				} else {
					persons.add(StringUtils.replace(value, "*", ".*"));
				}
			} else {
				persons.add(StringUtils.replace(value, "*", ".*"));
			}
		}
	}

	protected boolean matches(PersonIdent person) {
		String personEmail = person.getEmailAddress();
		for (String value: values) {
			if (value == null) { // authored by me
				User user = User.get();
				if (user == null) {
					throw new ExplicitException("Please login to perform this query");
				} else if (user.getEmailAddresses().stream()
						.anyMatch(it-> it.isVerified() && it.getValue().equalsIgnoreCase(personEmail))) { 
					return true;
				}
			} else if (value.startsWith("@")) {
				String userName = value.substring(1);
				User user = getUserManager().findByName(userName);
				if (user != null) {
					if (user.getEmailAddresses().stream()
							.anyMatch(it-> it.isVerified() && it.getValue().equalsIgnoreCase(personEmail))) {
						return true;
					}
				} else if (matches("*" + value + "*", person)) {
					return true;
				}
			} else if (matches("*" + value + "*", person)) {
				return true;
			}
		}
		return false;
	}

	protected String toString(int personRule, int currentPersonRule) {
		List<String> parts = new ArrayList<>();
		for (String value: values) {
			if (value != null)
				parts.add(getRuleName(personRule) + parens(value));
			else
				parts.add(getRuleName(currentPersonRule));
		}
		return StringUtils.join(parts, " ");
	}
	
}
