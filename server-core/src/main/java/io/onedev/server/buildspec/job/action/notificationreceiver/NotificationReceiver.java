package io.onedev.server.buildspec.job.action.notificationreceiver;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.action.notificationreceiver.NotificationReceiverParser.CriteriaContext;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;

public class NotificationReceiver {
	
	private final Collection<String> emails;
	
	public NotificationReceiver(Collection<String> emails) {
		this.emails = emails;
	}
	
	public static NotificationReceiver parse(String receiverString, @Nullable Build build) {
		Collection<String> emails = new HashSet<>();
		
		CharStream is = CharStreams.fromString(receiverString); 
		NotificationReceiverLexer lexer = new NotificationReceiverLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed notification receiver");
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		NotificationReceiverParser parser = new NotificationReceiverParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		
		for (CriteriaContext criteria: parser.receiver().criteria()) {
			if (criteria.userCriteria() != null) {
				String userName = getValue(criteria.userCriteria().Value());
				User user = OneDev.getInstance(UserManager.class).findByName(userName);
				if (user != null) 
					emails.add(user.getEmail());
				else 
					throw new ExplicitException("Unable to find user '" + userName + "'");
			} else if (criteria.groupCriteria() != null) {
				String groupName = getValue(criteria.groupCriteria().Value());
				Group group = OneDev.getInstance(GroupManager.class).find(groupName);
				if (group != null) 
					emails.addAll(group.getMembers().stream().map(it->it.getEmail()).collect(Collectors.toList()));
				else 
					throw new ExplicitException("Unable to find group '" + groupName + "'");
			} else if (criteria.Committers() != null) {
				if (build != null) {
					for (RevCommit commit: build.getCommits(null)) {
						PersonIdent committer = commit.getCommitterIdent();
						if (committer != null && committer.getEmailAddress() != null) 
							emails.add(committer.getEmailAddress());
					}
				}
			} else if (criteria.Authors() != null) {
				if (build != null) {
					for (RevCommit commit: build.getCommits(null)) {
						PersonIdent author = commit.getAuthorIdent();
						if (author != null && author.getEmailAddress() != null) 
							emails.add(author.getEmailAddress());
					}
				}
			} else if (criteria.CommittersSincePreviousSuccessful() != null) {
				if (build != null) {
					for (RevCommit commit: build.getCommits(Build.Status.SUCCESSFUL)) {
						PersonIdent committer = commit.getCommitterIdent();
						if (committer != null && committer.getEmailAddress() != null) 
							emails.add(committer.getEmailAddress());
					}
				}
			} else if (criteria.AuthorsSincePreviousSuccessful() != null) {
				if (build != null) {
					for (RevCommit commit: build.getCommits(Build.Status.SUCCESSFUL)) {
						PersonIdent author = commit.getAuthorIdent();
						if (author != null && author.getEmailAddress() != null) 
							emails.add(author.getEmailAddress());
					}
				}
			} else if (criteria.Submitter() != null) {
				if (build != null && build.getSubmitter() != null)
					emails.add(build.getSubmitter().getEmail());
			} else {
				throw new RuntimeException("Unexpected notification receiver criteria");
			}
		}			
		
		return new NotificationReceiver(emails);
	}

	private static String getValue(TerminalNode terminal) {
		return StringUtils.unescape(FenceAware.unfence(terminal.getText()));
	}
	
	public Collection<String> getEmails() {
		return emails;
	}

}
