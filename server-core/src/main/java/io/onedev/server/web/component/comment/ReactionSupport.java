package io.onedev.server.web.component.comment;

import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.support.EntityReaction;

public interface ReactionSupport {

    Collection<? extends EntityReaction> getReactions();
	
	void onToggleEmoji(AjaxRequestTarget target, String emoji);

}
