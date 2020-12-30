package io.onedev.server.util.jobmatch;

import java.util.ArrayList;
import java.util.List;

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
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.NotCriteria;
import io.onedev.server.util.criteria.OrCriteria;
import io.onedev.server.util.jobmatch.JobMatchParser.AndCriteriaContext;
import io.onedev.server.util.jobmatch.JobMatchParser.CriteriaContext;
import io.onedev.server.util.jobmatch.JobMatchParser.FieldOperatorValueCriteriaContext;
import io.onedev.server.util.jobmatch.JobMatchParser.JobMatchContext;
import io.onedev.server.util.jobmatch.JobMatchParser.NotCriteriaContext;
import io.onedev.server.util.jobmatch.JobMatchParser.OperatorValueCriteriaContext;
import io.onedev.server.util.jobmatch.JobMatchParser.OrCriteriaContext;
import io.onedev.server.util.jobmatch.JobMatchParser.ParensCriteriaContext;

public class JobMatch extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final Criteria<Build> criteria;
	
	public JobMatch(Criteria<Build> criteria) {
		this.criteria = criteria;
	}

	public static String getValue(String token) {
		return StringUtils.unescape(FenceAware.unfence(token));
	}
	
	public static JobMatch parse(String jobMatchString) {
		CharStream is = CharStreams.fromString(jobMatchString); 
		JobMatchLexer lexer = new JobMatchLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new BaseErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new RuntimeException("Malformed job match", e);
			}
			
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JobMatchParser parser = new JobMatchParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		JobMatchContext jobMatchContext = parser.jobMatch();

		Criteria<Build> criteria;
		
		if (jobMatchContext.All() != null) {
			criteria = new AlwaysCriteria();
		} else {
			criteria = new JobMatchBaseVisitor<Criteria<Build>>() {
	
				@Override
				public Criteria<Build> visitParensCriteria(ParensCriteriaContext ctx) {
					return visit(ctx.criteria()).withParens(true);
				}
	
				@Override
				public Criteria<Build> visitFieldOperatorValueCriteria(FieldOperatorValueCriteriaContext ctx) {
					String fieldName = getValue(ctx.Quoted(0).getText());
					String fieldValue = getValue(ctx.Quoted(1).getText());
					int operator = ctx.operator.getType();
					checkField(fieldName, operator);
					
					switch (fieldName) {
					case Build.NAME_PROJECT:
						return new ProjectCriteria(fieldValue);
					case Build.NAME_JOB:
						return new NameCriteria(fieldValue);
					default:
						return new ImageCriteria(fieldValue);
					}
				}
				
				@Override
				public Criteria<Build> visitOperatorValueCriteria(OperatorValueCriteriaContext ctx) {
					String fieldValue = getValue(ctx.Quoted().getText());
					return new BranchCriteria(fieldValue);
				}
				
				@Override
				public Criteria<Build> visitOrCriteria(OrCriteriaContext ctx) {
					List<Criteria<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new OrCriteria<Build>(childCriterias);
				}
	
				@Override
				public Criteria<Build> visitAndCriteria(AndCriteriaContext ctx) {
					List<Criteria<Build>> childCriterias = new ArrayList<>();
					for (CriteriaContext childCtx: ctx.criteria())
						childCriterias.add(visit(childCtx));
					return new AndCriteria<Build>(childCriterias);
				}
	
				@Override
				public Criteria<Build> visitNotCriteria(NotCriteriaContext ctx) {
					return new NotCriteria<Build>(visit(ctx.criteria()));
				}
	
			}.visit(jobMatchContext.criteria());
		}
		return new JobMatch(criteria);
	}
	
	public static void checkField(String fieldName, int operator) {
		if (fieldName.equals(Build.NAME_PROJECT) 
				|| fieldName.equals(Build.NAME_JOB)
				|| fieldName.equals(Build.NAME_IMAGE)) {
			if (operator != JobMatchLexer.Is)
				throw newOperatorException(fieldName, operator);
		} else {
			throw new ExplicitException("Invalid field: " + fieldName);
		}
	}
	
	private static ExplicitException newOperatorException(String fieldName, int operator) {
		return new ExplicitException("Field '" + fieldName + "' is not applicable for operator '" 
				+ AntlrUtils.getLexerRuleName(JobMatchLexer.ruleNames, operator) + "'");
	}

	@Override
	public boolean matches(Build build) {
		return criteria.matches(build);
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		criteria.onRenameUser(oldName, newName);
	}

	@Override
	public void onRenameProject(String oldName, String newName) {
		criteria.onRenameProject(oldName, newName);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return criteria.isUsingUser(userName);
	}

	@Override
	public boolean isUsingProject(String projectName) {
		return criteria.isUsingProject(projectName);
	}
	
	public static String getRuleName(int rule) {
		return AntlrUtils.getLexerRuleName(JobMatchLexer.ruleNames, rule);
	}
	
	@Override
	public String toStringWithoutParens() {
		return criteria.toString();
	}

}
