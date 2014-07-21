package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class CloseLineCommentForm extends AbstractLineCommentEvent {

	public CloseLineCommentForm(AjaxRequestTarget target, int position,
			String lineId) {
		super(target, position, lineId);
	}

}
