package io.onedev.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CommitMessageTransform;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.utils.Transformer;

public class CommitMessageTransformer implements Transformer<String> {

	private static final Logger logger = LoggerFactory.getLogger(CommitMessageTransformer.class);
	
	private static final Pattern PULL_REQUEST_PATTERN = Pattern.compile("(.*pull\\s*request\\s+)([\\w\\.-]*)#(\\d+)($|\\W+?)");
	
	private static final Pattern ISSUE_PATTERN = Pattern.compile("(^|\\W+)([\\w\\.-]*)#(\\d+)($|\\W+?)");
	
	private final Project project;
	
	private final String commitUrl;
	
	public CommitMessageTransformer(Project project, @Nullable String commitUrl) {
		this.project = project;
		this.commitUrl = commitUrl;
	}
	
	@Override
	public String transform(String commitMessage) {
		commitMessage = HtmlEscape.escapeHtml5(commitMessage);
		try {
			for (CommitMessageTransform transform: project.getCommitMessageTransforms())
				commitMessage = commitMessage.replaceAll(transform.getSearchFor(), transform.getReplaceWith());
		} catch (Exception e) {
			logger.error("Error transforming commit message", e);
		}
		
		commitMessage = linkEntities(project, commitMessage, new EntityInfoProvider() {

			@Override
			public AbstractEntity find(Project project, long entityNumber) {
				return OneDev.getInstance(PullRequestManager.class).find(project, entityNumber);
			}

			@Override
			public String getUrl(Project project, AbstractEntity entity) {
				PageParameters params = PullRequestActivitiesPage.paramsOf((PullRequest)entity, null);
				return RequestCycle.get().urlFor(PullRequestActivitiesPage.class, params).toString();
			}

			@Override
			public Pattern getPattern() {
				return PULL_REQUEST_PATTERN;
			}
			
		});

		commitMessage = linkEntities(project, commitMessage, new EntityInfoProvider() {

			@Override
			public AbstractEntity find(Project project, long entityNumber) {
				return OneDev.getInstance(IssueManager.class).find(project, entityNumber);
			}

			@Override
			public String getUrl(Project project, AbstractEntity entity) {
				PageParameters params = IssueActivitiesPage.paramsOf((Issue)entity, null);
				return RequestCycle.get().urlFor(IssueActivitiesPage.class, params).toString();
			}

			@Override
			public Pattern getPattern() {
				return ISSUE_PATTERN;
			}
			
		});
		
		if (commitUrl != null) {
			Element body = Jsoup.parseBodyFragment(commitMessage).body();
			for (int i=0; i<body.childNodeSize(); i++) {
				if (body.childNode(i) instanceof TextNode) {
					TextNode textNode = (TextNode) body.childNode(i);
					Element element = new Element(Tag.valueOf("a"), "");
					element.addClass("commit");
					element.attr("href", commitUrl);
					element.appendText(textNode.getWholeText());
					textNode.replaceWith(element);
				}
			}
			return body.html();
		} else {
			return commitMessage;
		}
	}
	
	private String linkEntities(Project project, String commitMessage, EntityInfoProvider entityInfoProvider) {
		Element body = Jsoup.parseBodyFragment(commitMessage).body();
		ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
		StringBuffer html = new StringBuffer();
		for (int i=0; i<body.childNodeSize(); i++) {
			Node node = body.childNode(i);
			if (node instanceof TextNode) {
				TextNode textNode = (TextNode) body.childNode(i);
			    Matcher matcher = entityInfoProvider.getPattern().matcher(textNode.getWholeText());
			    StringBuffer buffer = new StringBuffer();
			    while (matcher.find()) {
			    	String referencedProjectName = matcher.group(2);
			    	if (referencedProjectName.length() != 0) {
			    		Project referencedProject = projectManager.find(referencedProjectName);
			    		if (referencedProject == null) {
			    			matcher.appendReplacement(buffer, matcher.group());
			    		} else {
			    			AbstractEntity entity = entityInfoProvider.find(referencedProject, Long.parseLong(matcher.group(3)));
			    			if (entity != null) {
			    				String link = String.format("<a href=\"%s\">%s#%s</a>", entityInfoProvider.getUrl(referencedProject, entity), referencedProjectName, matcher.group(3));
			    				matcher.appendReplacement(buffer, matcher.group(1) + link + matcher.group(4));
			    			} else {
			    				matcher.appendReplacement(buffer, matcher.group());
			    			}
			    		}
			    	} else {
		    			AbstractEntity entity = entityInfoProvider.find(project, Long.parseLong(matcher.group(3)));
		    			if (entity != null) {
		    				String link = String.format("<a href=\"%s\">#%s</a>", entityInfoProvider.getUrl(project, entity), matcher.group(3));
		    				matcher.appendReplacement(buffer, matcher.group(1) + link + matcher.group(4));
		    			} else {
		    				matcher.appendReplacement(buffer, matcher.group());			    		
		    			}
			    	}
			    }
			    matcher.appendTail(buffer);
			    html.append(buffer);
			} else {
				html.append(node.toString());
			}
		}	
		return html.toString();
	}

	private static interface EntityInfoProvider {
		
		Pattern getPattern();
		
		@Nullable
		AbstractEntity find(Project project, long entityNumber);
		
		String getUrl(Project project, AbstractEntity entity);
		
	}
}
