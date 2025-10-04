package io.onedev.server.event.project.pack;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.service.PackService;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Pack;
import io.onedev.server.model.User;
import io.onedev.server.web.UrlService;

public abstract class PackEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long packId;
	
	public PackEvent(User user, Date date, Pack pack) {
		super(user, date, pack.getProject());
		packId = pack.getId();
	}

	public Pack getPack() {
		return OneDev.getInstance(PackService.class).load(packId);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(getPack(), true);
	}
	
}
