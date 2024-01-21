package io.onedev.server.event.project.pack;

import io.onedev.server.model.Pack;

public class PackPublished extends PackEvent {

	private static final long serialVersionUID = 1L;

	public PackPublished(Pack pack) {
		super(null, pack.getPublishDate(), pack);
	}
	
	@Override
	public String getActivity() {
		return "published";
	}

}
