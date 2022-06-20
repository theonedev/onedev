package io.onedev.server.markdown;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class UrlProcessor implements MarkdownProcessor {

	private static final Logger logger = LoggerFactory.getLogger(UrlProcessor.class);
	
	@Override
	public void process(Document rendered, Project project, 
			@Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport) {
		if (RequestCycle.get() != null && blobRenderContext != null && project != null) {
			Repository repository = project.getRepository();
			RevCommit commit;
			if (blobRenderContext.getBlobIdent().revision != null) {
				try (RevWalk revWalk = new RevWalk(repository)) {
					commit = revWalk.parseCommit(repository.resolve(blobRenderContext.getBlobIdent().revision));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				commit = null;
			}
			
			NodeTraversor.traverse(new NodeVisitor() {

				@Override
				public void head(Node node, int depth) {
				}

				@Override
				public void tail(Node node, int depth) {
					try {
						if (node.nodeName().equals("a")) {
							String url = node.attr("href");
							if (StringUtils.isNotBlank(url)) {
								url = url.trim();
								if (UrlUtils.isRelative(url) && !url.startsWith("#")) {
									Element element = (Element) node;
									element.attr("href", resolveUrl(blobRenderContext.getDirectoryUrl(), url));
									String path = UrlUtils.decodePath(UrlUtils.trimHashAndQuery(url));
									String directory = blobRenderContext.getDirectory();
									String referencedPath = PathUtils.resolve(directory, path);
									referencedPath = GitUtils.normalizePath(referencedPath);
									if (referencedPath != null && (commit == null || TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null)) {
										element.after("<span class='missing'>!!missing!!</span>");
										Element missingElement = element.nextElementSibling();
										BlobIdent blobIdent = blobRenderContext.getBlobIdent();
										Mode mode = blobRenderContext.getMode();
										if (mode != Mode.ADD && mode != Mode.EDIT 
												&& SecurityUtils.canModify(project, blobIdent.revision, referencedPath)) {
											ProjectBlobPage.State state = new ProjectBlobPage.State();
											state.blobIdent = blobRenderContext.getBlobIdent();
											state.mode = Mode.ADD;
											state.initialNewPath = path;
											CharSequence urlToAddFile = RequestCycle.get().urlFor(ProjectBlobPage.class, 
													ProjectBlobPage.paramsOf(project, state));
											String htmlToAddFile = String.format(
													"<a href='%s' title='Add this file' class='add-missing'><svg class='icon'><use xlink:href='%s'/></svg></a>", 
													urlToAddFile.toString(), SpriteImage.getVersionedHref(IconScope.class, "plus"));
											missingElement.after(htmlToAddFile);
										}
									}
								}
							}
						} else if (node.nodeName().equals("img")) {
							String url = node.attr("src");
							if (StringUtils.isNotBlank(url)) {
								url = url.trim();
								if (UrlUtils.isRelative(url) && !url.startsWith("#")) {
									Element element = (Element) node;
									element.attr("src", blobRenderContext.appendRaw(resolveUrl(blobRenderContext.getDirectoryUrl(), url)));
									String basePath = blobRenderContext.getDirectory();
									String referencedPath = PathUtils.resolve(basePath, UrlUtils.decodePath(UrlUtils.trimHashAndQuery(url)));
									referencedPath = GitUtils.normalizePath(referencedPath);
									if (referencedPath != null && (commit == null || TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null)) {
										element.after("<span class='missing'>!!missing!!</span>");
									}
								}
							}
						}
					} catch (Exception e) {
						if (ExceptionUtils.find(e, URISyntaxException.class) != null)
							logger.error("Error parsing url", e);
						else
							throw ExceptionUtils.unchecked(e);
					}
				}
			}, rendered);
		}
	}

	private String resolveUrl(String baseUrl, String urlToResolve) {
        return PathUtils.normalizeDots(PathUtils.resolve(baseUrl, urlToResolve));
    }

}
