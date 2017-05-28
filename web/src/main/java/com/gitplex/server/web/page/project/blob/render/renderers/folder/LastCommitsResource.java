package com.gitplex.server.web.page.project.blob.render.renderers.folder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.page.user.UserProfilePage;
import com.gitplex.server.web.util.DateUtils;
import com.gitplex.server.web.util.avatar.AvatarManager;

/**
 * Loading commits of children may take some time, and we do this via resource loading to avoid blocking 
 * other Wicket based ajax requests.
 * 
 * @author robin
 *
 */
class LastCommitsResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_REPO = "repo";
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		final Long repoId = params.get(PARAM_REPO).toLong();
		final String revision = params.get(PARAM_REVISION).toString();
		final String path = params.get(PARAM_PATH).toOptionalString();

		ResourceResponse response = new ResourceResponse();
		response.setContentType("application/json");
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				Project repo = GitPlex.getInstance(Dao.class).load(Project.class, repoId);
				
				if (!SecurityUtils.canRead(repo))
					throw new UnauthorizedException();
				
				LastCommitsOfChildren lastCommits = repo.getLastCommitsOfChildren(revision, path);
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				AvatarManager avatarManager = GitPlex.getInstance(AvatarManager.class);
				
				Map<String, LastCommitInfo> map = new HashMap<>();
				for (Map.Entry<String, LastCommitsOfChildren.Value> entry: lastCommits.entrySet()) {
					LastCommitInfo info = new LastCommitInfo();

					LastCommitsOfChildren.Value value = entry.getValue();
					PageParameters params = CommitDetailPage.paramsOf(repo, value.getId().name());
					info.url = RequestCycle.get().urlFor(CommitDetailPage.class, params).toString();
					info.summary = StringEscapeUtils.escapeHtml4(value.getSummary());
					info.when = DateUtils.formatAge(value.getCommitDate());
					
					PersonIdent author = value.getAuthor();
					User user = userManager.find(author);
					if (user != null) {
						info.authorName = HtmlEscape.escapeHtml5(user.getDisplayName());
						info.authorAvatarUrl = avatarManager.getAvatarUrl(user);
						params = UserProfilePage.paramsOf(user);
						info.authorUrl = RequestCycle.get().urlFor(UserProfilePage.class, params).toString();
					} else {
						info.authorName = HtmlEscape.escapeHtml5(author.getName());
						info.authorAvatarUrl = avatarManager.getAvatarUrl(author);
					}
					
					map.put(entry.getKey(), info);
				}
				String json;
				try {
					json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(map);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				attributes.getResponse().write(json);
			}
			
		});

		return response;
	}

	public static PageParameters paramsOf(Project project, String revision, @Nullable String path) {
		PageParameters params = new PageParameters();
		params.set(PARAM_REPO, project.getId());
		params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		
		return params;
	}
	
	@SuppressWarnings("unused")
	private static class LastCommitInfo {
		String url;
		
		String summary;
		
		String when;
		
		String authorAvatarUrl;
		
		String authorName;
		
		@Nullable
		String authorUrl;
	}
	
}
