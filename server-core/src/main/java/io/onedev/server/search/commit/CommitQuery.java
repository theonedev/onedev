package io.onedev.server.search.commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQueryParser.CriteriaContext;

public class CommitQuery implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final List<CommitCriteria> criterias;
	
	public CommitQuery(List<CommitCriteria> criterias) {
		this.criterias = criterias;
	}
	
	public static CommitQuery parse(Project project, @Nullable String queryString) {
		List<CommitCriteria> criterias = new ArrayList<>();
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			CommitQueryLexer lexer = new CommitQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed commit query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CommitQueryParser parser = new CommitQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			
			List<String> authorValues = new ArrayList<>();
			List<String> committerValues = new ArrayList<>();
			List<String> beforeValues = new ArrayList<>();
			List<String> afterValues = new ArrayList<>();
			List<String> pathValues = new ArrayList<>();
			List<String> messageValues = new ArrayList<>();
			List<Revision> revisions = new ArrayList<>();
			
			for (CriteriaContext criteria: parser.query().criteria()) {
				if (criteria.authorCriteria() != null) {
					if (criteria.authorCriteria().AuthoredByMe() != null)
						authorValues.add(null);
					else
						authorValues.add(getValue(criteria.authorCriteria().Value()));
				} else if (criteria.committerCriteria() != null) {
					if (criteria.committerCriteria().CommittedByMe() != null)
						committerValues.add(null);
					else
						committerValues.add(getValue(criteria.committerCriteria().Value()));
				} else if (criteria.messageCriteria() != null) { 
					messageValues.add(getValue(criteria.messageCriteria().Value()));
				} else if (criteria.pathCriteria() != null) {
					pathValues.add(getValue(criteria.pathCriteria().Value()));
				} else if (criteria.beforeCriteria() != null) {
					beforeValues.add(getValue(criteria.beforeCriteria().Value()));
				} else if (criteria.afterCriteria() != null) {
					afterValues.add(getValue(criteria.afterCriteria().Value()));
				} else if (criteria.revisionCriteria() != null) {
					String value;
					Revision.Scope scope;
					if (criteria.revisionCriteria().DefaultBranch() != null) 
						value = project.getDefaultBranch();
					else 
						value = getValue(criteria.revisionCriteria().Value());
					if (criteria.revisionCriteria().BUILD() != null) {
						String numberStr = value;
						if (numberStr.startsWith("#"))
							numberStr = numberStr.substring(1);
						if (NumberUtils.isDigits(numberStr)) {
							Build build = OneDev.getInstance(BuildManager.class).find(project, Long.parseLong(numberStr));
							if (build == null)
								throw new ExplicitException("Unable to find build: " + value);
							else
								value = build.getCommitHash();
						} else {
							throw new ExplicitException("Invalid build number: " + numberStr);
						}
					}
					if (criteria.revisionCriteria().SINCE() != null)
						scope = Revision.Scope.SINCE;
					else if (criteria.revisionCriteria().UNTIL() != null)
						scope = Revision.Scope.UNTIL;
					else
						scope = null;
					if (value != null)
						revisions.add(new Revision(value, scope, criteria.revisionCriteria().getText()));
				}
			}
			
			if (!authorValues.isEmpty())
				criterias.add(new AuthorCriteria(authorValues));
			if (!committerValues.isEmpty())
				criterias.add(new CommitterCriteria(committerValues));
			if (!pathValues.isEmpty())
				criterias.add(new PathCriteria(pathValues));
			if (!messageValues.isEmpty())
				criterias.add(new MessageCriteria(messageValues));
			if (!beforeValues.isEmpty())
				criterias.add(new BeforeCriteria(beforeValues));
			if (!afterValues.isEmpty())
				criterias.add(new AfterCriteria(afterValues));
			if (!revisions.isEmpty())
				criterias.add(new RevisionCriteria(revisions));
		}
		
		return new CommitQuery(criterias);
	}
	
	private static String getValue(TerminalNode valueNode) {
		return StringUtils.unescape(FenceAware.unfence(valueNode.getText())); 
	}
	
	public boolean matches(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId())) 
			return criterias.stream().allMatch(it->it.matches(event));
		else 
			return false;
	}
	
	public void fill(Project project, RevListCommand command) {
		criterias.stream().forEach(it->it.fill(project, command));
	}
	
	public List<CommitCriteria> getCriterias() {
		return criterias;
	}

	public static CommitQuery merge(CommitQuery query1, CommitQuery query2) {
		List<CommitCriteria> criterias = new ArrayList<>();
		criterias.addAll(query1.getCriterias());
		criterias.addAll(query2.getCriterias());
		return new CommitQuery(criterias);
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (CommitCriteria criteria: criterias)
			parts.add(criteria.toString());
		return StringUtils.join(parts, " ");
	}
	
}
