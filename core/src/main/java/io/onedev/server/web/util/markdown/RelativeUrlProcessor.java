package io.onedev.server.web.util.markdown;

import java.io.IOException;
import java.net.URLDecoder;

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

import com.google.common.base.Charsets;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.markdown.MarkdownProcessor;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.utils.PathUtils;
import io.onedev.utils.UrlUtils;

public class RelativeUrlProcessor implements MarkdownProcessor {

	@Override
	public void process(Project project, Document rendered, Object context) {
		if (context instanceof BlobRenderContext && project != null) {
			BlobRenderContext blobRenderContext = (BlobRenderContext) context;
			
			Repository repository = project.getRepository();
			RevCommit commit;
			try (RevWalk revWalk = new RevWalk(repository)) {
				commit = revWalk.parseCommit(repository.resolve(blobRenderContext.getBlobIdent().revision));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			new NodeTraversor(new NodeVisitor() {

				@Override
				public void head(Node node, int depth) {
				}

				@Override
				public void tail(Node node, int depth) {
					if (node.nodeName().equals("a")) {
						String url = node.attr("href");
						if (StringUtils.isNotBlank(url)) {
							url = url.trim();
							if (UrlUtils.isRelative(url) && !url.startsWith("#")) {
								Element element = (Element) node;
								element.attr("href", resolveUrl(blobRenderContext.getDirectoryUrl(), url));
								try {
									String path = UrlUtils.trimHashAndQuery(URLDecoder.decode(url, Charsets.UTF_8.name()));
									String directory = blobRenderContext.getDirectory();
									String referencedPath = PathUtils.resolve(directory, path);
									referencedPath = GitUtils.normalizePath(referencedPath);
									if (referencedPath != null && TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null) {
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
														"<a href='%s' title='Add this file' class='add-missing'><i class='fa fa-plus'></i></a>", 
														urlToAddFile.toString());
												missingElement.after(htmlToAddFile);
											}
										}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						}
					} else if (node.nodeName().equals("img")) {
						String url = node.attr("src");
						if (StringUtils.isNotBlank(url)) {
							url = url.trim();
							if (UrlUtils.isRelative(url) && !url.startsWith("#")) {
								Element element = (Element) node;
								element.attr("src", resolveUrl(blobRenderContext.getDirectoryUrl(), url));
								try {
									String basePath = blobRenderContext.getDirectory();
									String referencedPath = PathUtils.resolve(basePath, UrlUtils.trimHashAndQuery(URLDecoder.decode(url, Charsets.UTF_8.name())));
									referencedPath = GitUtils.normalizePath(referencedPath);
									if (referencedPath != null && TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null) {
										element.after("<span class='missing'>!!missing!!</span>");
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
				}
			}).traverse(rendered);
			
		}
	}

	private String resolveUrl(String baseUrl, String urlToResolve) {
        return PathUtils.normalizeDots(PathUtils.resolve(baseUrl, urlToResolve));
    }

}
