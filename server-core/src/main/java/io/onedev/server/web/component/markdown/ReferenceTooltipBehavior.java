package io.onedev.server.web.component.markdown;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.entityreference.PullRequestReference;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.ColorUtils;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

public class ReferenceTooltipBehavior extends AbstractPostAjaxBehavior {

	private final EntityReference reference;

	public ReferenceTooltipBehavior(EntityReference reference) {
		this.reference = reference;
	}

	@Override
	protected void respond(AjaxRequestTarget target) {
		renderTooltip(target, reference);
	}

	static void renderTooltip(AjaxRequestTarget target, EntityReference reference) {
		if (reference instanceof IssueReference) {
			var issue = OneDev.getInstance(IssueService.class).find(reference.getProject(), reference.getNumber());
			if (issue != null && SecurityUtils.canAccessIssue(issue)) {
				String color = OneDev.getInstance(SettingService.class).getIssueSetting()
						.getStateSpec(issue.getState()).getColor();
				String script = String.format("onedev.server.markdown.renderIssueTooltip('%s', '%s', '%s', '%s');",
						Emojis.getInstance().apply(JavaScriptEscape.escapeJavaScript(issue.getTitle())),
						JavaScriptEscape.escapeJavaScript(issue.getState()),
						ColorUtils.isLight(color) ? "black" : "white", color);
				target.appendJavaScript(script);
			} else {
				target.appendJavaScript("onedev.server.markdown.renderIssueTooltip();");
			}
		} else if (reference instanceof PullRequestReference) {
			var request = OneDev.getInstance(PullRequestService.class).find(reference.getProject(), reference.getNumber());
			if (request != null && SecurityUtils.canReadCode(request.getTargetProject())) {
				String status = request.getStatus().toString();
				String statusCss;
				switch (request.getStatus()) {
				case OPEN:
					statusCss = "badge-warning";
					break;
				case MERGED:
					statusCss = "badge-success";
					break;
				default:
					statusCss = "badge-danger";
				}
				String script = String.format("onedev.server.markdown.renderPullRequestTooltip('%s', '%s', '%s');",
						Emojis.getInstance().apply(JavaScriptEscape.escapeJavaScript(request.getTitle())),
						status, statusCss);
				target.appendJavaScript(script);
			} else {
				target.appendJavaScript("onedev.server.markdown.renderPullRequestTooltip();");
			}
		} else {
			throw new IllegalArgumentException("Unsupported tooltip reference: " + reference.getType());
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		String script = String.format("onedev.server.markdown.initReferenceTooltips($('#%s'), %s, %s);",
				component.getMarkupId(true), getCallbackFunction(), JavascriptTranslations.get());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
