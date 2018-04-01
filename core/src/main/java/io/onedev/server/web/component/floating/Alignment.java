package io.onedev.server.web.component.floating;

import java.io.Serializable;

public class Alignment implements Serializable {

	private static final long serialVersionUID = 1L;

	private final AlignTarget target;
	
	private final AlignPlacement placement;
	
	public Alignment(AlignTarget target, AlignPlacement placement) {
		this.target = target;
		this.placement = placement;
	}

	public AlignTarget getTarget() {
		return target;
	}

	public AlignPlacement getPlacement() {
		return placement;
	}
	
}
