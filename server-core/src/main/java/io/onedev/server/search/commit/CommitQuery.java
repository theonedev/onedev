package io.onedev.server.search.commit;

import static io.onedev.commons.codeassist.FenceAware.unfence;
import static io.onedev.commons.utils.StringUtils.unescape;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQueryParser.CriteriaContext;

public class CommitQuery implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private List<CommitCriteria> criterias;
	
	public CommitQuery(List<CommitCriteria> criterias) {
		this.criterias = criterias;
	}
	
	public List<CommitCriteria> getCriterias() {
		return criterias;
	}

	public void setCriterias(List<CommitCriteria> criterias) {
		this.criterias = criterias;
	}

	public static CommitQuery parse(Project project, @Nullable String queryString, boolean withCurrentUserCriteria) {
		List<CommitCriteria> criterias = new ArrayList<>();
		if (queryString != null) {
			CharStream is = CharStreams.fromString(queryString); 
			CommitQueryLexer lexer = new CommitQueryLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new BaseErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new RuntimeException("Malformed query", e);
				}
				
			});
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CommitQueryParser parser = new CommitQueryParser(tokens);
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			
			Map<Class<? extends CommitCriteria>, List<Object>> criteriaValues = new LinkedHashMap<>();
			
			for (CriteriaContext criteria: parser.query().criteria()) {
				if (criteria.authorCriteria() != null) {
					if (criteria.authorCriteria().AuthoredByMe() != null) {
						if (!withCurrentUserCriteria)
							throw new ExplicitException("Criteria '" + criteria.authorCriteria().AuthoredByMe().getText() + "' is not supported here");
							criteriaValues.computeIfAbsent(AuthorCriteria.class, k->new ArrayList<>()).add(null);
					} else {
						for (var value: criteria.authorCriteria().Value())
							criteriaValues.computeIfAbsent(AuthorCriteria.class, k->new ArrayList<>()).add(getValue(value));
					}
				} else if (criteria.committerCriteria() != null) {
					if (criteria.committerCriteria().CommittedByMe() != null) {
						if (!withCurrentUserCriteria)
							throw new ExplicitException("Criteria '" + criteria.committerCriteria().CommittedByMe().getText() + "' is not supported here");
						criteriaValues.computeIfAbsent(CommitterCriteria.class, k->new ArrayList<>()).add(null);
					} else {
						for (var value: criteria.committerCriteria().Value())
							criteriaValues.computeIfAbsent(CommitterCriteria.class, k->new ArrayList<>()).add(getValue(value));
					}
				} else if (criteria.messageCriteria() != null) {
					for (var value: criteria.messageCriteria().Value()) 
						criteriaValues.computeIfAbsent(MessageCriteria.class, k->new ArrayList<>()).add(getValue(value));
				} else if (criteria.fuzzyCriteria() != null) {
					criteriaValues.computeIfAbsent(FuzzyCriteria.class, k->new ArrayList<>()).add(unescape(unfence(criteria.fuzzyCriteria().getText())));
				} else if (criteria.pathCriteria() != null) {
					for (var value: criteria.pathCriteria().Value())
						criteriaValues.computeIfAbsent(PathCriteria.class, k->new ArrayList<>()).add(getValue(value));
				} else if (criteria.beforeCriteria() != null) {
					criteriaValues.computeIfAbsent(BeforeCriteria.class, k->new ArrayList<>()).add(getValue(criteria.beforeCriteria().Value()));
				} else if (criteria.afterCriteria() != null) {
					criteriaValues.computeIfAbsent(AfterCriteria.class, k->new ArrayList<>()).add(getValue(criteria.afterCriteria().Value()));
				} else if (criteria.revisionCriteria() != null) {
					Revision.Type type;
					if (criteria.revisionCriteria().BRANCH() != null || criteria.revisionCriteria().DefaultBranch() != null) 
						type = Revision.Type.BRANCH;
					else if (criteria.revisionCriteria().TAG() != null)
						type = Revision.Type.TAG;
					else if (criteria.revisionCriteria().COMMIT() != null)
						type = Revision.Type.COMMIT;
					else if (criteria.revisionCriteria().BUILD() != null)
						type = Revision.Type.BUILD;
					else
						throw new ExplicitException("Unknown revision type");

					var isSince = criteria.revisionCriteria().SINCE() != null;

					if (criteria.revisionCriteria().DefaultBranch() != null) {
						criteriaValues.computeIfAbsent(RevisionCriteria.class, k->new ArrayList<>()).add(new Revision(type, null, isSince));
					} else {
						for (var valueNode: criteria.revisionCriteria().Value()) {
							criteriaValues.computeIfAbsent(RevisionCriteria.class, k->new ArrayList<>()).add(new Revision(type, getValue(valueNode), isSince));
						}
					}
				}
			}
			
			for (var entry: criteriaValues.entrySet()) {
				Class<? extends CommitCriteria> criteriaClass = entry.getKey();
				List<Object> values = entry.getValue();
				if (!values.isEmpty()) {
					try {
						criterias.add(criteriaClass.getConstructor(List.class).newInstance(values));
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		return new CommitQuery(criterias);
	}
	
	private static String getValue(TerminalNode valueNode) {
		return unescape(unfence(valueNode.getText())); 
	}
	
	public boolean matches(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId())) 
			return criterias.stream().allMatch(it->it.matches(event));
		else 
			return false;
	}
	
	public void fill(Project project, RevListOptions options) {
		criterias.stream().forEach(it->it.fill(project, options));
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
		return join(parts, " ");
	}
	
}
