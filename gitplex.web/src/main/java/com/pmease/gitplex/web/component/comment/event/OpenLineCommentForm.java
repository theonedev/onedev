package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class OpenLineCommentForm extends AbstractLineCommentEvent {

	public OpenLineCommentForm(AjaxRequestTarget target, int position,
			String lineId) {
		super(target, position, lineId);
	}

}
