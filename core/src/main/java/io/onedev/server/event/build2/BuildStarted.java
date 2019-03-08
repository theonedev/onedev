package io.onedev.server.event.build2;

import java.util.Date;

import io.onedev.server.model.Build2;
import io.onedev.server.model.User;

public class BuildStarted extends BuildEvent {

	public BuildStarted(User user, Date date, Build2 build) {
		super(user, date, build);
	}

}
