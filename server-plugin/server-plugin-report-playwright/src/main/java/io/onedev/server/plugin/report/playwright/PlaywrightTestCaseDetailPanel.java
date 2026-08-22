package io.onedev.server.plugin.report.playwright;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.playwright.PlaywrightReportParser.AttachmentData;
import io.onedev.server.plugin.report.playwright.PlaywrightReportParser.ResultData;
import io.onedev.server.plugin.report.unittest.ArtifactResource;
import io.onedev.server.plugin.report.unittest.ArtifactResourceReference;

public class PlaywrightTestCaseDetailPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public PlaywrightTestCaseDetailPanel(String id, Build build, String reportName,
			List<ResultData> results) {
		super(id);
		add(new ListView<ResultData>("results", results) {

			@Override
			protected void populateItem(ListItem<ResultData> item) {
				ResultData result = item.getModelObject();
				WebMarkupContainer header = new WebMarkupContainer("header");
				header.setVisible(getList().size() > 1);
				header.add(new Label("attempt",
						result.retry != 0? "Retry " + result.retry: "Initial attempt"));
				header.add(new Label("status", result.status));
				header.add(new Label("duration",
						DurationFormatUtils.formatDuration(result.duration, "s.SSS 's'")));
				item.add(header);

				WebMarkupContainer errorsSection = new WebMarkupContainer("errorsSection");
				errorsSection.setVisible(!result.errors.isEmpty());
				errorsSection.add(new ListView<String>("errors", result.errors) {

					@Override
					protected void populateItem(ListItem<String> item) {
						item.add(new Label("error",
								PlaywrightReportParser.formatDetail(build, item.getModelObject()))
								.setEscapeModelStrings(false));
					}

				});
				item.add(errorsSection);

				WebMarkupContainer stdoutSection = new WebMarkupContainer("stdoutSection");
				stdoutSection.setVisible(StringUtils.isNotEmpty(result.stdout));
				stdoutSection.add(new Label("stdout", PlaywrightReportParser.sanitizeText(
						StringUtils.defaultString(result.stdout))));
				item.add(stdoutSection);

				WebMarkupContainer stderrSection = new WebMarkupContainer("stderrSection");
				stderrSection.setVisible(StringUtils.isNotEmpty(result.stderr));
				stderrSection.add(new Label("stderr", PlaywrightReportParser.sanitizeText(
						StringUtils.defaultString(result.stderr))));
				item.add(stderrSection);

				WebMarkupContainer attachmentsSection = new WebMarkupContainer("attachmentsSection");
				attachmentsSection.setVisible(!result.attachments.isEmpty());
				attachmentsSection.add(new ListView<AttachmentData>("attachments", result.attachments) {

					@Override
					protected void populateItem(ListItem<AttachmentData> item) {
						AttachmentData attachment = item.getModelObject();
						String url = RequestCycle.get().urlFor(
								new ArtifactResourceReference(),
								ArtifactResource.paramsOf(
										build, reportName, attachment.path)).toString();
						ExternalLink link = new ExternalLink("link", url);
						link.add(new ExternalImage("image", url).setVisible(
								isSafeInlineImage(attachment.contentType)));
						String name = StringUtils.defaultIfBlank(attachment.name, attachment.path);
						link.add(new Label("name", name));
						item.add(link);
						item.add(new Label("contentType",
								StringUtils.defaultString(attachment.contentType)));
					}

				});
				item.add(attachmentsSection);
			}

		});
	}

	private static boolean isSafeInlineImage(String contentType) {
		if (contentType == null)
			return false;
		String mediaType = StringUtils.substringBefore(contentType, ";").trim().toLowerCase();
		return mediaType.equals("image/png")
				|| mediaType.equals("image/jpeg")
				|| mediaType.equals("image/jpg")
				|| mediaType.equals("image/gif")
				|| mediaType.equals("image/webp")
				|| mediaType.equals("image/bmp");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PlaywrightReportCssResourceReference()));
	}

}
