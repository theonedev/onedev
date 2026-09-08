package io.onedev.server.web.util;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.FileMode;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.wiki.ProjectWikiPage;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;

/** Routes wiki links and permission-gated creation actions; WikiUtils handles syntax and paths. */
public class WikiLinkResolver {

	private final Project project;
	private final String revision;
	private final String folder;
	private final String currentPath;
	private final String returnPage;

	public WikiLinkResolver(Project project, @Nullable String revision, String folder,
			String currentPath, String returnPage) {
		this.project = project;
		this.revision = revision;
		this.folder = folder;
		this.currentPath = currentPath;
		this.returnPage = returnPage;
	}

	/** Resolve wiki references consistently in repository views and editor previews. */
	public static String resolveWikiLinks(String html, @Nullable BlobRenderContext context) {
		if (context == null)
			return html;

		var project = context.getProject();
		BlobIdent blobIdent = context.getBlobIdent();
		String path = blobIdent.path;
		if (context.getMode() == BlobRenderContext.Mode.ADD || context.getMode() == BlobRenderContext.Mode.EDIT)
			path = context.getNewPath();
		String folder = project.getWikiFolder();
		if (path == null || !path.startsWith(folder + "/") || !path.endsWith(".md"))
			return html;

		String currentPage = path.substring(folder.length() + 1, path.length() - 3);
		return new WikiLinkResolver(project, blobIdent.revision, folder, path, currentPage).resolvePageLinks(html);
	}

	/** Wiki views resolve both ordinary Markdown links and wiki-style references. */
	public String resolve(String html) {
		html = WikiUtils.resolveRelativeLinks(html, currentPath,
				path -> fileUrl(path, false), path -> fileUrl(path, true),
				path -> project.getMode(revision, path) == 0, this::addFileUrl, addIconHref());
		return resolvePageLinks(html);
	}

	/** Repository views already resolve ordinary Markdown links through UrlProcessor. */
	public String resolvePageLinks(String html) {
		return WikiUtils.resolvePageLinks(html,
				destination -> WikiUtils.resolvePagePath(currentPath, destination), this::pageUrl,
				path -> project.getBlob(new BlobIdent(revision, path), false) == null,
				this::addPageUrl, addIconHref());
	}

	private String pageName(String path) {
		return path.substring(folder.length() + 1, path.length() - 3);
	}

	private String pageUrl(String path) {
		if (WikiUtils.isUnderFolder(folder, path))
			return RequestCycle.get().urlFor(ProjectWikiPage.class,
					ProjectWikiPage.paramsOf(project, revision, pageName(path))).toString();
		return repositoryUrl(path, false);
	}

	private String fileUrl(String path, boolean image) {
		if (!WikiUtils.isUnderFolder(folder, path))
			return repositoryUrl(path, image);
		if (!image && path.endsWith(".md"))
			return pageUrl(path);
		return RequestCycle.get().urlFor(new RawBlobResourceReference(),
				RawBlobResource.paramsOf(project, new BlobIdent(revision, path))).toString();
	}

	private String repositoryUrl(String path, boolean image) {
		int mode = project.getMode(revision, path);
		var ident = new BlobIdent(revision, path, mode != 0 ? mode : FileMode.REGULAR_FILE.getBits());
		var params = ProjectBlobPage.paramsOf(project, ident);
		if (image)
			params.add("raw", true);
		var cycle = RequestCycle.get();
		String url = cycle.urlFor(ProjectBlobPage.class, params).toString();
		return UrlUtils.makeRelative(cycle.getUrlRenderer().renderFullUrl(Url.parse(url)));
	}

	private @Nullable String addPageUrl(String path) {
		if (!folder.equals(project.getWikiFolder()) || !WikiUtils.isUnderFolder(folder, path)
				|| !SecurityUtils.canEditWikiPage(project, revision, path))
			return null;
		String destination = pageName(path);
		var params = ProjectWikiPage.paramsOf(project, revision, destination);
		params.add("new", true);
		params.add("initial-name", destination);
		params.add("return-page", returnPage);
		return RequestCycle.get().urlFor(ProjectWikiPage.class, params).toString();
	}

	private @Nullable String addFileUrl(String path) {
		if (!SecurityUtils.canModifyFile(project, revision, path))
			return null;
		var state = new ProjectBlobPage.State(new BlobIdent(revision, currentPath, FileMode.REGULAR_FILE.getBits()));
		state.mode = Mode.ADD;
		int slash = currentPath.lastIndexOf('/');
		state.initialNewPath = PathUtils.relativize(slash >= 0 ? currentPath.substring(0, slash) : null, path);
		return RequestCycle.get().urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project, state)).toString();
	}

	private String addIconHref() {
		return SpriteImage.getVersionedHref(IconScope.class, "plus");
	}
}
