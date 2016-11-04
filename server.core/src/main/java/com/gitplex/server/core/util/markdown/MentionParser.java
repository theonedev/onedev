package com.gitplex.server.core.util.markdown;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;

import com.google.common.collect.ImmutableSet;
import com.gitplex.commons.util.JsoupUtils;
import com.gitplex.commons.util.TextNodeVisitor;
import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.manager.AccountManager;

public class MentionParser {
	
	private static final Collection<String> IGNORED_TAGS = ImmutableSet.of("pre", "code", "a");
	
	private static final Pattern PATTERN = Pattern.compile("(^|\\s+)@(\\S+)(?=($|\\s+))");

	public Collection<Account> parseMentions(String html) {
		return parseMentions(Jsoup.parseBodyFragment(html).body());		
	}
	
	public Collection<Account> parseMentions(Element body) {
		Collection<Account> mentions = new HashSet<>();
		
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
		
		AccountManager userManager = GitPlex.getInstance(AccountManager.class);
		
		for (TextNode node : visitor.getMatchedNodes()) {
			Matcher matcher = PATTERN.matcher(node.getWholeText());
			while (matcher.find()) {
				String userName = matcher.group(2);
				String userTag;
				Account user = userManager.findByName(userName);
				if (user != null) {
					mentions.add(user);
					userTag = toHtml(user);
				} else {
					userTag = "@" + userName;
				}
				JsoupUtils.appendReplacement(matcher, node, matcher.group(1) + userTag + matcher.group(3));
			}
			JsoupUtils.appendTail(matcher, node);
		}

		return mentions;
	}

	protected String toHtml(Account user) {
		return "@" + user.getName();
	}
	
}
