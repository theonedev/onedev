package io.onedev.server.event.build2;

import java.util.Date;

import io.onedev.server.model.Build2;
import io.onedev.server.model.User;

public class BuildFinished extends BuildEvent {

	public BuildFinished(User user, Date date, Build2 build) {
		super(user, date, build);
	}

}
