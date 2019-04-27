package io.onedev.server.event.build;

import java.util.Date;

import io.onedev.server.model.Build;
import io.onedev.server.model.User;

public class BuildDeleted extends BuildEvent {

	public BuildDeleted(User user, Build build) {
		super(user, new Date(), build);
	}

}