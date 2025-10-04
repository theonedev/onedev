package io.onedev.server.web.page.project.blob.render.renderers.gitlink;

import java.nio.file.Paths;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.Submodule;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

public class GitLinkPanel extends BlobViewPanel {

	public GitLinkPanel(String id, BlobRenderContext context) {
		super(id, context);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
		Submodule submodule = Submodule.fromString(blob.getText().getContent()); 
		WebMarkupContainer link = null;
		SettingService settingService = OneDev.getInstance(SettingService.class);
		ProjectService projectService = OneDev.getInstance(ProjectService.class);
		String rootUrl = settingService.getSystemSetting().getServerUrl() + "/";
		Project project = null;
		if (submodule.getUrl().startsWith(rootUrl)) {
			link = new WebMarkupContainer("link");
			String projectPath = submodule.getUrl().substring(rootUrl.length());
			project = projectService.findByPath(projectPath);
		} else if (!submodule.getUrl().startsWith("http:") 
				&& !submodule.getUrl().startsWith("https:") 
				&& !submodule.getUrl().startsWith("ssh:")) { // relative url
			String projectPath = Paths.get(context.getProject().getPath())
					.resolve(submodule.getUrl()).normalize().toString();
			project = projectService.findByPath(projectPath);
		}
		if (project != null) {
			BlobIdent blobIdent = new BlobIdent();
			blobIdent.revision = submodule.getCommitId();
			link = new BookmarkablePageLink<Void>("link", 
					ProjectBlobPage.class, ProjectBlobPage.paramsOf(project, blobIdent));
		} else {
			link = new WebMarkupContainer("link") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			};
		}
		link.add(new Label("label", submodule));
		add(link);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new GitLinkResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return false;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return false;
	}

}
