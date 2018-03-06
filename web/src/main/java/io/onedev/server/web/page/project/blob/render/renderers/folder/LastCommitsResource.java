package io.onedev.server.web.page.project.blob.render.renderers.folder;

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

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.commit.CommitDetailPage;
import io.onedev.server.web.page.user.UserProfilePage;
import io.onedev.server.web.util.DateUtils;
import io.onedev.server.web.util.avatar.AvatarManager;

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
				Project repo = OneDev.getInstance(Dao.class).load(Project.class, repoId);
				
				if (!SecurityUtils.canRead(repo))
					throw new UnauthorizedException();
				
				LastCommitsOfChildren lastCommits = repo.getLastCommitsOfChildren(revision, path);
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				AvatarManager avatarManager = OneDev.getInstance(AvatarManager.class);
				
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
						info.authorAvatarUrl = avatarManager.getAvatarUrl(user.getFacade());
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
					json = OneDev.getInstance(ObjectMapper.class).writeValueAsString(map);
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
