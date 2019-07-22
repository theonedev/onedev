package io.onedev.server.web.behavior.blamemessage;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.revwalk.RevCommit;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.model.Project;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

public abstract class BlameMessageBehavior extends AbstractPostAjaxBehavior {

	private static final long serialVersionUID = 1L;

	@Override
	protected void respond(AjaxRequestTarget target) {
		IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
		
		String tooltipId = params.getParameterValue("tooltip").toString();
		String commitHash = params.getParameterValue("commit").toString();
		RevCommit commit = getProject().getRevCommit(commitHash, true);
		String authoring;
		if (commit.getAuthorIdent() != null) {
			authoring = commit.getAuthorIdent().getName();
			if (commit.getCommitterIdent() != null)
				authoring += " " + DateUtils.formatAge(commit.getCommitterIdent().getWhen());
			authoring = "'" + JavaScriptEscape.escapeJavaScript(authoring) + "'";
		} else {
			authoring = "undefined";
		}
		String message = JavaScriptEscape.escapeJavaScript(commit.getFullMessage());
		String script = String.format("onedev.server.blameMessage.show('%s', %s, '%s');", tooltipId, authoring, message); 
		target.appendJavaScript(script);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new BlameMessageResourceReference()));
	}

	protected abstract Project getProject();
	
	public String getCallback() {
		return getCallbackFunction(explicit("tooltip"), explicit("commit")).toString();
	}
	
}
