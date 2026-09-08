package io.onedev.server.markdown;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;
import org.jsoup.nodes.Document;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.PathUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class UrlProcessor implements HtmlProcessor {
	
	@Override
	public void process(Document document, Project project,
						@Nullable BlobRenderContext blobRenderContext,
						@Nullable SuggestionSupport suggestionSupport,
						boolean forExternal) {
		if (RequestCycle.get() != null && blobRenderContext != null && project != null) {
			GitService gitService = OneDev.getInstance(GitService.class);
			ObjectId revId;
			if (blobRenderContext.getBlobIdent().revision != null) {
				revId = gitService.resolve(project, blobRenderContext.getBlobIdent().revision, true);
				if (revId == null)
					throw new ExplicitException("Revision not found: " + blobRenderContext.getBlobIdent().revision);
			} else {
				revId = null;
			}
			
			LinkUtils.resolveRelativeLinks(document, blobRenderContext.getDirectory(),
					(path, url) -> resolveUrl(blobRenderContext.getDirectoryUrl(), url),
					(path, url) -> blobRenderContext.appendRaw(resolveUrl(blobRenderContext.getDirectoryUrl(), url)),
					path -> revId == null || gitService.getMode(project, revId, path) == 0, path -> {
				BlobIdent blobIdent = blobRenderContext.getBlobIdent();
				Mode mode = blobRenderContext.getMode();
				if (mode != Mode.ADD && mode != Mode.EDIT
						&& SecurityUtils.canModifyFile(project, blobIdent.revision, path)) {
					ProjectBlobPage.State state = new ProjectBlobPage.State();
					state.blobIdent = blobIdent;
					state.mode = Mode.ADD;
					state.initialNewPath = PathUtils.relativize(blobRenderContext.getDirectory(), path);
					return RequestCycle.get().urlFor(ProjectBlobPage.class,
							ProjectBlobPage.paramsOf(project, state)).toString();
				}
				return null;
			}, SpriteImage.getVersionedHref(IconScope.class, "plus"));
		}
	}

	private String resolveUrl(String baseUrl, String urlToResolve) {
		return PathUtils.normalizeDots(PathUtils.resolve(baseUrl, urlToResolve));
	}

}
