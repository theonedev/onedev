package io.onedev.server.plugin.report.jest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;

@SuppressWarnings("serial")
abstract class TestMessagePanel extends Label {

	private static final Pattern LOCATION = Pattern.compile("\\((.*):(\\d+):(\\d+)\\)", Pattern.MULTILINE); 
	
	public TestMessagePanel(String id, String message) {
		super(id);
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (SecurityUtils.canReadCode(getBuild().getProject())) {
					StringBuffer buffer = new StringBuffer();
					int lastPos = 0;
					Matcher matcher = LOCATION.matcher(message);
					while (matcher.find()) {
						buffer.append(HtmlEscape.escapeHtml5(message.substring(lastPos, matcher.start())));
						lastPos = matcher.end();
						
						String file = matcher.group(1);
						int line = Integer.parseInt(matcher.group(2));
						int col = Integer.parseInt(matcher.group(3));
						
						if (getBuild().getJobWorkspace() != null && file.startsWith(getBuild().getJobWorkspace())) 
							file = file.substring(getBuild().getJobWorkspace().length()+1);
						BlobIdent blobIdent = new BlobIdent(getBuild().getCommitHash(), file, FileMode.REGULAR_FILE.getBits());
						if (getBuild().getProject().getBlob(blobIdent, false) != null) {
							ProjectBlobPage.State state = new ProjectBlobPage.State();
							state.blobIdent = blobIdent;
							PlanarRange range = new PlanarRange(line-1, col-1, line-1, col); 
							state.position = SourceRendererProvider.getPosition(range);
							PageParameters params = ProjectBlobPage.paramsOf(getBuild().getProject(), state);
							String url = urlFor(ProjectBlobPage.class, params).toString();
							buffer.append(String.format("(<a onclick='onedev.server.viewState.getFromViewAndSetToHistory();' href='%s'>%s:%d:%d</a>)", 
									url, HtmlEscape.escapeHtml5(file), line, col));
						} else {
							buffer.append("(" + HtmlEscape.escapeHtml5(file) + ":" + line + ":" + col + ")");
						}
					}
					buffer.append(HtmlEscape.escapeHtml5(message.substring(lastPos)));
					return buffer.toString();
				} else {
					return HtmlEscape.escapeHtml5(message);
				}
			}
			
		});
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}

	protected abstract Build getBuild();
}
