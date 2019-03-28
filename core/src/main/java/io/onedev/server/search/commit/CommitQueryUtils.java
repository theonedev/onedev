package io.onedev.server.search.commit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.stringmatch.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.exception.OneException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.commit.CommitQueryParser.CriteriaContext;
import io.onedev.server.search.commit.CommitQueryParser.QueryContext;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;

public class CommitQueryUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(CommitQueryUtils.class);
	
	@Nullable
	public static QueryContext parse(@Nullable String queryString) {
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			CommitQueryLexer lexer = new CommitQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					if (e != null) {
						logger.error("Error lexing commit query", e);
					} else if (msg != null) {
						logger.error("Error lexing commit query: " + msg);
					}
					throw new RuntimeException("Malformed commit query");
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CommitQueryParser parser = new CommitQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			return parser.query();
		} else {
			return null;
		}
	}

	public static boolean needsLogin(@Nullable String queryString) {
		QueryContext query = parse(queryString);
		if (query != null) {
			for (CriteriaContext criteria: query.criteria()) {
				if (criteria.authorCriteria() != null && criteria.authorCriteria().AuthoredByMe() != null 
						|| criteria.committerCriteria() != null && criteria.committerCriteria().CommittedByMe() != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static String removeParens(String value) {
		if (value.startsWith("("))
			value = value.substring(1);
		if (value.endsWith(")"))
			value = value.substring(0, value.length()-1);
		return value;
	}
	
	public static List<String> getMessages(QueryContext query) {
		List<String> messages = new ArrayList<>();
		for (CriteriaContext criteria: query.criteria()) {
			if (criteria.messageCriteria() != null) {
				String message = unescape(removeParens(criteria.messageCriteria().Value().getText()));
				messages.add(StringUtils.replace(message, "*", ".*"));
			} 
		}
		return messages;
	}
	
	private static String unescape(String text) {
		return text.replace("\\(", "(").replace("\\)", ")").replace("\\\\", "\\");
	}
	
	private static List<Revision> getRevisions(Project project, QueryContext query) {
		List<Revision> revisions = new ArrayList<>();
		for (CriteriaContext criteria: query.criteria()) {
			if (criteria.revisionCriteria() != null) {
				Revision revision = new Revision();
				if (criteria.revisionCriteria().DefaultBranch() != null)
					revision.value = project.getDefaultBranch();
				else
					revision.value = unescape(removeParens(criteria.revisionCriteria().Value().getText()));
				if (criteria.revisionCriteria().BUILD() != null) {
					Build build = OneDev.getInstance(BuildManager.class).findByFQN(project, revision.value);
					if (build == null)
						throw new OneException("Unable to find build with FQN: " + revision.value);
					else
						revision.value = build.getCommitHash();
				}
				if (criteria.revisionCriteria().SINCE() != null)
					revision.since = true;
				else if (criteria.revisionCriteria().UNTIL() != null)
					revision.until = true;
				revisions.add(revision);
			} 
		}
		return revisions;
	}
	
	private static boolean matches(String value, PersonIdent person) {
		String formatted = String.format("%s <%s>", person.getName(), person.getEmailAddress());
		return WildcardUtils.matchString(value, formatted);
	}

	public static boolean matches(RefUpdated event, User user, @Nullable String queryString) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId())) {
			Project project = event.getProject();
			RevCommit commit = project.getRevCommit(event.getNewCommitId());
			
			QueryContext query = parse(queryString);
			if (query != null && query.ALL() == null) { 
				boolean matches = false;
				boolean matchAtLeastOnce = false;
				for (CriteriaContext criteria: query.criteria()) {
					if (criteria.authorCriteria() != null) {
						matchAtLeastOnce = true;
						if (criteria.authorCriteria().AuthoredByMe() != null) {
							if (user.getEmail().equals(commit.getAuthorIdent().getEmailAddress())) {
								matches = true;
								break;
							}
						} else {
							String value = unescape(removeParens(criteria.authorCriteria().Value().getText()));
							if (matches("*" + value + "*", commit.getAuthorIdent())) {
								matches = true;
								break;
							}
						}
					}
				}
				if (!matches && matchAtLeastOnce)
					return false;
				
				matches = matchAtLeastOnce = false;
				for (CriteriaContext criteria: query.criteria()) {
					if (criteria.committerCriteria() != null) {
						matchAtLeastOnce = true;
						if (criteria.committerCriteria().CommittedByMe() != null) {
							if (user.getEmail().equals(commit.getCommitterIdent().getEmailAddress())) {
								matches = true;
								break;
							}
						} else {
							String value = unescape(removeParens(criteria.committerCriteria().Value().getText()));
							if (matches("*" + value + "*", commit.getCommitterIdent())) {
								matches = true;
								break;
							}
						}
					}
				}
				if (!matches && matchAtLeastOnce)
					return false;
				
				matches = matchAtLeastOnce = false;
				for (CriteriaContext criteria: query.criteria()) {
					if (criteria.messageCriteria() != null) {
						matchAtLeastOnce = true;
						String value = unescape(removeParens(criteria.messageCriteria().Value().getText()));
						if (WildcardUtils.matchString("*" + value + "*", commit.getFullMessage())) {
							matches = true;
							break;
						}
					}
				}
				if (!matches && matchAtLeastOnce)
					return false;
				
				for (CriteriaContext criteria: query.criteria()) {
					if (criteria.beforeCriteria() != null) {
						String value = unescape(removeParens(criteria.beforeCriteria().Value().getText()));
						if (!commit.getCommitterIdent().getWhen().before(DateUtils.parseRelaxed(value)))
							return false;
					}
				}
				
				for (CriteriaContext criteria: query.criteria()) {
					if (criteria.afterCriteria() != null) {
						String value = unescape(removeParens(criteria.afterCriteria().Value().getText()));
						if (!commit.getCommitterIdent().getWhen().after(DateUtils.parseRelaxed(value)))
							return false;
					}
				}
				
				matches = matchAtLeastOnce = false;
				Collection<String> changedFiles;
				if (!event.getOldCommitId().equals(ObjectId.zeroId()))
					changedFiles = GitUtils.getChangedFiles(project.getRepository(), event.getOldCommitId(), event.getNewCommitId());
				else if (commit.getParentCount() != 0)
					changedFiles = GitUtils.getChangedFiles(project.getRepository(), commit.getParent(0), event.getNewCommitId());
				else
					changedFiles = new HashSet<>();
				
				for (CriteriaContext criteria: query.criteria()) {
					if (criteria.pathCriteria() != null) {
						matchAtLeastOnce = true;
						String value = unescape(removeParens(criteria.pathCriteria().Value().getText()));
						for (String changedFile: changedFiles) {
							if (WildcardUtils.matchString(value, changedFile)) {
								matches = true;
								break;
							}
						}
						if (matches)
							break;
					}
				}
				if (!matches && matchAtLeastOnce)
					return false;
				
				matches = matchAtLeastOnce = false;
				for (Revision revision: getRevisions(project, query)) {
					if (!revision.since) {
						matchAtLeastOnce = true;
						try {
							Ref ref = project.getRepository().findRef(revision.value);
							if (ref != null && ref.getName().equals(event.getRefName())) { 
								matches = true;
								break;
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				if (!matches && matchAtLeastOnce)
					return false;
			}
			return true;			
		} else {
			return false;
		}
	}
	
	public static void fill(Project project, @Nullable QueryContext query, RevListCommand command) {
		if (query != null && query.ALL() == null) {
			for (CriteriaContext criteria: query.criteria()) {
				if (criteria.authorCriteria() != null) {
					if (criteria.authorCriteria().AuthoredByMe() != null) {
						if (SecurityUtils.getUser() != null)
							command.authors().add("<" + SecurityUtils.getUser().getEmail() + ">");
						else
							throw new OneException("Please login to perform this query");
					} else {
						String value = criteria.authorCriteria().Value().getText();
						value = StringUtils.replace(unescape(removeParens(value)), "*", ".*");
						command.authors().add(value);
					}
				} else if (criteria.committerCriteria() != null) {
					if (criteria.committerCriteria().CommittedByMe() != null) {
						if (SecurityUtils.getUser() != null)
							command.committers().add("<" + SecurityUtils.getUser().getEmail() + ">");
						else
							throw new OneException("Please login to perform this query");
					} else {
						String value = criteria.committerCriteria().Value().getText();
						value = StringUtils.replace(unescape(removeParens(value)), "*", ".*");
						command.committers().add(value);
					}
				} else if (criteria.pathCriteria() != null) {
					command.paths().add(unescape(removeParens(criteria.pathCriteria().Value().getText())));
				} else if (criteria.beforeCriteria() != null) {
					command.before(removeParens(criteria.beforeCriteria().Value().getText()));
				} else if (criteria.afterCriteria() != null) {
					command.after(removeParens(criteria.afterCriteria().Value().getText()));
				} 
			}
			command.messages(getMessages(query));
			
			boolean ranged = false;
			for (Revision revision: getRevisions(project, query)) {
				if (revision.since) {
					command.revisions().add("^" + revision.value);
					ranged = true;
				} else if (revision.until) {
					command.revisions().add(revision.value);
					ranged = true;
				} else if (project.getBranchRef(revision.value) != null) {
					ranged = true;
					command.revisions().add(revision.value);
				} else {
					command.revisions().add(revision.value);
				}
			}
			if (command.revisions().size() == 1 && !ranged) {
				command.count(1);
			}
		}
	}
}
