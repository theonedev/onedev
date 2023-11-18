package io.onedev.server.event.project.pack;

import io.onedev.server.model.Pack;

public class PackCreated extends PackEvent {

	private static final long serialVersionUID = 1L;

	public PackCreated(Pack pack) {
		super(null, pack.getCreateDate(), pack);
	}
	
	@Override
	public String getActivity() {
		return "created";
	}

}
