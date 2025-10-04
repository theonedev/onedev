package io.onedev.server.web.page.security;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.UserChoice;
import io.onedev.server.service.UserService;
import io.onedev.server.model.User;

@Editable
public class LinkUserBean implements Serializable {
    
    private String userName;

    private String password;

    @Editable(order=100, name="User", description="Only users able to authenticate via password can be linked")
    @UserChoice("getLinkableUsers")
    @NotEmpty
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Editable(order=200, description="Password of the user")
    @Password(autoComplete="current-password")
    @NotEmpty
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	@SuppressWarnings("unused")
	private static List<User> getLinkableUsers() {
		return OneDev.getInstance(UserService.class).query().stream()
                .filter(it -> !it.isServiceAccount() && !it.isDisabled())
                .sorted(Comparator.comparing(User::getDisplayName))
                .collect(Collectors.toList());
	}

}
