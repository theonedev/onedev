package io.onedev.server.event.project.pack;

import java.io.Serial;

import io.onedev.server.model.Pack;

public class PackPublished extends PackEvent {

	@Serial
	private static final long serialVersionUID = 1L;

	public PackPublished(Pack pack) {
		super(pack.getUser(), pack.getPublishDate(), pack);
	}
	
	@Override
	public String getActivity() {
		return "published";
	}

}
