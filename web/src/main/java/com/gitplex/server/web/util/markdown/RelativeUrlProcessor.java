package com.gitplex.server.web.util.markdown;

import java.io.IOException;
import java.net.URLDecoder;

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

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Project;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.markdown.MarkdownProcessor;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext.Mode;
import com.gitplex.utils.PathUtils;
import com.google.common.base.Charsets;

public class RelativeUrlProcessor implements MarkdownProcessor {

	@Override
	public void process(Document rendered, Object context) {
		if (context instanceof BlobRenderContext) {
			BlobRenderContext blobRenderContext = (BlobRenderContext) context;

			Project project = blobRenderContext.getProject();
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
						if (url != null) {
							url = url.trim();
							if (isRelative(url)) {
								Element element = (Element) node;
								element.attr("href", resolveUrl(blobRenderContext.getBaseUrl(), url));
								try {
									String path = URLDecoder.decode(url, Charsets.UTF_8.name());
									String basePath = blobRenderContext.getBasePath();
									String referencedPath = PathUtils.resolve(basePath, path);
									referencedPath = GitUtils.normalizePath(referencedPath);
									if (referencedPath == null || TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null) {
											element.after("<span class='missing'>!!missing!!</span>");
											Element missingElement = element.nextElementSibling();
											BlobIdent blobIdent = blobRenderContext.getBlobIdent();
											Mode mode = blobRenderContext.getMode();
											if (referencedPath != null && mode != Mode.ADD && mode != Mode.EDIT 
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
						if (url != null) {
							url = url.trim();
							if (isRelative(url)) {
								Element element = (Element) node;
								element.attr("src", resolveUrl(blobRenderContext.getBaseUrl(), url));
								try {
									String basePath = blobRenderContext.getBasePath();
									String referencedPath = PathUtils.resolve(basePath, URLDecoder.decode(url, Charsets.UTF_8.name()));
									referencedPath = GitUtils.normalizePath(referencedPath);
									if (referencedPath == null || TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null) {
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

	private boolean isRelative(String url) {
    	return !url.contains(":") && !url.startsWith("/") && !url.startsWith("#");
	}
	
	private String resolveUrl(String baseUrl, String urlToResolve) {
        return PathUtils.normalizeDots(PathUtils.resolve(baseUrl, urlToResolve));
    }

}
