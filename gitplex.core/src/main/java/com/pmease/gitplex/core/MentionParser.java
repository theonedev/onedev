package com.pmease.gitplex.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;
import com.pmease.commons.util.JsoupUtils;
import com.pmease.commons.util.TextNodeVisitor;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;

public class MentionParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)@(\\S+)(?=($|\\s+))");

	public Collection<User> parseMentions(String html) {
		return parseMentions(Jsoup.parseBodyFragment(html).body());		
	}
	
	public Collection<User> parseMentions(Element body) {
		Collection<User> mentions = new HashSet<>();
		
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
					userTag = toHtml(user);
				} else {
					userTag = "@" + userName;
				}
				matcher.appendReplacement(buffer, matcher.group(1) + userTag + matcher.group(3));
			}
			matcher.appendTail(buffer);
			
			DataNode newNode = new DataNode(buffer.toString(), node.baseUri());
			node.replaceWith(newNode);
		}

		return mentions;
	}

	protected String toHtml(User user) {
		return "@" + user.getName();
	}
	
}
