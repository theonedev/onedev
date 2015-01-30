package com.pmease.gitplex.web.component.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.util.JsoupUtils;
import com.pmease.commons.util.TextNodeVisitor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.extensionpoint.HtmlTransformer;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountHomePage;

public class MentionTransformer implements HtmlTransformer {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)@(\\S+)(?=($|\\s+))");

	private Collection<User> mentions = new HashSet<>();
	
	@Override
	public Element transform(Element body) {
		mentions.clear();
		
		TextNodeVisitor visitor = new TextNodeVisitor() {
			
			@Override
			protected boolean isApplicable(TextNode node) {
				if (JsoupUtils.hasAncestor(node, IGNORED_TAGS))
					return false;
				
				return node.getWholeText().contains("@"); // fast scan here, do pattern match later
			}
		};
		
		NodeTraversor tranversor = new NodeTraversor(visitor);
		tranversor.traverse(body);
		
		UserManager userManager = GitPlex.getInstance(UserManager.class);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			StringBuffer buffer = new StringBuffer();
			Matcher matcher = PATTERN.matcher(node.getWholeText());
			while (matcher.find()) {
				String userName = matcher.group(2);
				String userTag;
				User user = userManager.findByName(userName);
				if (user != null) {
					mentions.add(user);
					RequestCycle requestCycle = RequestCycle.get();
					if (requestCycle != null) {
						CharSequence userUrl = requestCycle.urlFor(AccountHomePage.class, AccountHomePage.paramsOf(user));
						userTag = String.format("<a href='%s' class='mention'>@%s</a>", userUrl, userName);
					} else {
						userTag = "@" + userName;
					}
				} else {
					userTag = "@" + userName;
				}
				matcher.appendReplacement(buffer, matcher.group(1) + userTag + matcher.group(3));
			}
			matcher.appendTail(buffer);
			
			DataNode newNode = new DataNode(buffer.toString(), node.baseUri());
			node.replaceWith(newNode);
		}

		return body;
	}
	
	public Collection<User> getMentions() {
		return mentions;
	}

}
