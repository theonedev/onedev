package io.onedev.server.job.requirement;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.onedev.commons.codeassist.AntlrUtils;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.job.requirement.JobRequirementParser.AndCriteriaContext;
import io.onedev.server.job.requirement.JobRequirementParser.CriteriaContext;
import io.onedev.server.job.requirement.JobRequirementParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.job.requirement.JobRequirementParser.JobRequirementContext;
import io.onedev.server.job.requirement.JobRequirementParser.NotCriteriaContext;
import io.onedev.server.job.requirement.JobRequirementParser.OperatorValueCriteriaContext;
import io.onedev.server.job.requirement.JobRequirementParser.OrCriteriaContext;
import io.onedev.server.job.requirement.JobRequirementParser.ParensCriteriaContext;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;

public class JobRequirement extends Criteria<ProjectAndBranch> {

	private static final long serialVersionUID = 1L;
	
	private final Criteria<ProjectAndBranch> criteria;
	
	public JobRequirement(Criteria<ProjectAndBranch> criteria) {
		this.criteria = criteria;
	}

	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public static JobRequirement parse(String jobRequirementString) {
		CharStream is = CharStreams.fromString(jobRequirementString); 
		JobRequirementLexer lexer = new JobRequirementLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed job requirement", e);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JobRequirementParser parser = new JobRequirementParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		JobRequirementContext jobRequirementContext = parser.jobRequirement();

		Criteria<ProjectAndBranch> criteria = new JobRequirementBaseVisitor<Criteria<ProjectAndBranch>>() {

			@Override
			public Criteria<ProjectAndBranch> visitParensCriteria(ParensCriteriaContext ctx) {
				return visit(ctx.criteria()).withParens(true);
			}

			@Override
			public Criteria<ProjectAndBranch> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
				String fieldName = getValue(ctx.Quoted(0).getText());
				String fieldValue = getValue(ctx.Quoted(1).getText());
				int operator = ctx.operator.getType();
				checkField(fieldName, operator);
				
				switch (fieldName) {
				case Build.NAME_PROJECT:
					return new ProjectCriteria(fieldValue);
				default:
					throw new RuntimeException("Unknown job requirement field: " + fieldName);
				}
			}
			
			@Override
			public Criteria<ProjectAndBranch> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
				String fieldValue = getValue(ctx.Quoted().getText());
				return new BranchCriteria(fieldValue);
			}
			
			@Override
			public Criteria<ProjectAndBranch> visitOrCriteria(OrCriteriaContext ctx) {
				List<Criteria<ProjectAndBranch>> childCriterias = new ArrayList<>();
				for (CriteriaContext childCtx: ctx.criteria())
					childCriterias.add(visit(childCtx));
				return new OrCriteria<ProjectAndBranch>(childCriterias);
			}

			@Override
			public Criteria<ProjectAndBranch> visitAndCriteria(AndCriteriaContext ctx) {
				List<Criteria<ProjectAndBranch>> childCriterias = new ArrayList<>();
				for (CriteriaContext childCtx: ctx.criteria())
					childCriterias.add(visit(childCtx));
				return new AndCriteria<ProjectAndBranch>(childCriterias);
			}

			@Override
			public Criteria<ProjectAndBranch> visitNotCriteria(NotCriteriaContext ctx) {
				return new NotCriteria<ProjectAndBranch>(visit(ctx.criteria()));
			}

		}.visit(jobRequirementContext.criteria());
		
		return new JobRequirement(criteria);
	}
	
	public static void checkField(String fieldName, int operator) {
		if (fieldName.equals(Build.NAME_PROJECT) || fieldName.equals(Build.NAME_JOB)) {
			if (operator != JobRequirementLexer.Is)
				throw newOperatorException(fieldName, operator);
		} else {
			throw new ExplicitException("Invalid field: " + fieldName);
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" 
				+ AntlrUtils.getLexerRuleName(JobRequirementLexer.ruleNames, operator) + "'");
	}

	@Override
	public boolean matches(ProjectAndBranch projectAndBranch) {
		return criteria.matches(projectAndBranch);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<ProjectAndBranch, ProjectAndBranch> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void onRenameUser(String oldName, String newName) {
		criteria.onRenameUser(oldName, newName);
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
		criteria.onMoveProject(oldPath, newPath);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return criteria.isUsingUser(userName);
	}

	@Override
	public boolean isUsingProject(String projectPath) {
		return criteria.isUsingProject(projectPath);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(JobRequirementLexer.ruleNames, rule);
	}
	
	@Override
	public String toStringWithoutParens() {
		return criteria.toString();
	}

}
