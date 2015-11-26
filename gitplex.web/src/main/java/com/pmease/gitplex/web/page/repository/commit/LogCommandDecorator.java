package com.pmease.gitplex.web.page.repository.commit;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.AfterContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.AuthorContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.BeforeContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.CommitterContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.MessageContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.PathContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.QueryContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.RevisionContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.RevisionExclusionContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.RevisionRangeContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.SingleRevisionContext;

public class LogCommandDecorator extends CommitQueryBaseListener {

	private final LogCommand logCommand;
	
	private ParseTreeProperty<String> value = new ParseTreeProperty<>();

	public LogCommandDecorator(LogCommand logCommand) {
		this.logCommand = logCommand;
	}

	@Override
	public void exitSingleRevision(SingleRevisionContext ctx) {
		logCommand.revisions().add(value.get(ctx.revision()));
	}

	@Override
	public void exitRevisionExclusion(RevisionExclusionContext ctx) {
		logCommand.revisions().add("^" + value.get(ctx.revision()));
	}

	@Override
	public void exitRevisionRange(RevisionRangeContext ctx) {
		logCommand.revisions().add(value.get(ctx.revision(0)) + ctx.Range().getText() + value.get(ctx.revision(1)));
	}

	@Override
	public void exitRevision(RevisionContext ctx) {
		value.put(ctx, getValue(ctx.Value()));
	}
	
	private String getValue(TerminalNode terminal) {
		String value = terminal.getText().substring(1);
		return value.substring(0, value.length()-1);
	}

	@Override
	public void exitAfter(AfterContext ctx) {
		logCommand.after(getValue(ctx.Value()));
	}

	@Override
	public void exitBefore(BeforeContext ctx) {
		logCommand.before(getValue(ctx.Value()));
	}
	
	@Override
	public void exitCommitter(CommitterContext ctx) {
		logCommand.committers().add(getValue(ctx.Value()));
	}
	
	@Override
	public void exitAuthor(AuthorContext ctx) {
		logCommand.authors().add(getValue(ctx.Value()));
	}
	
	@Override
	public void exitPath(PathContext ctx) {
		logCommand.paths().add(getValue(ctx.Value()));
	}
	
	@Override
	public void exitMessage(MessageContext ctx) {
		logCommand.messages().add(getValue(ctx.Value()));
	}
	
	public static QueryContext parse(String query) {
		ANTLRInputStream is = new ANTLRInputStream(query); 
		CommitQueryLexer lexer = new CommitQueryLexer(is);
		lexer.removeErrorListeners();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CommitQueryParser parser = new CommitQueryParser(tokens);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		return parser.query();
	}
	
}
