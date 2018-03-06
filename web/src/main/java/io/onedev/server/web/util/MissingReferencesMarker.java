package io.onedev.server.web.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.utils.PathUtils;

/**
 * This visitor marks missing blob references in the document
 * 
 * @author robin
 *
 */
public class MissingReferencesMarker implements NodeVisitor {

	private final BlobRenderContext context;
	
	private final RevCommit commit;
	
	public MissingReferencesMarker(BlobRenderContext context) {
		this.context = context;
		Repository repository = context.getProject().getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			commit = revWalk.parseCommit(repository.resolve(context.getBlobIdent().revision));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void head(Node node, int depth) {
	}

	@Override
	public void tail(Node node, int depth) {
		if (node.nodeName().equals("a")) {
			String href = node.attr("href");
			if (StringUtils.isNotBlank(href) && "true".equals(node.attr("data-resolved"))) {
				Element link = (Element) node;
				Repository repository = context.getProject().getRepository();
				try {
					String referencedPath = PathUtils.parseRelative(href, context.getRootUrl());
					if (referencedPath != null && referencedPath.startsWith("/")) {
						referencedPath = referencedPath.substring(1);
						if (TreeWalk.forPath(repository, referencedPath, commit.getTree()) == null) {
							link.parent().append("<span class='missing'>!!missing!!</span>");
							if (context.getMode() != Mode.ADD && context.getMode() != Mode.EDIT 
									&& SecurityUtils.canModify(context.getProject(), context.getBlobIdent().revision, referencedPath)) {
								ProjectBlobPage.State state = new ProjectBlobPage.State();
								state.blobIdent = context.getBlobIdent();
								state.mode = Mode.ADD;

								String basePath = context.getBasePath();
								state.initialNewPath = PathUtils.relativize(basePath, referencedPath);
								CharSequence urlToAddFile = RequestCycle.get().urlFor(ProjectBlobPage.class, 
										ProjectBlobPage.paramsOf(context.getProject(), state));
								String htmlToAddFile = String.format(
										"<a href='%s' title='Add this file' class='add-missing'><i class='fa fa-plus'></i></a>", 
										urlToAddFile.toString());
								link.parent().append(htmlToAddFile);
							}
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
