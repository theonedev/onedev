package com.pmease.gitplex.web.page.depot.commit;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.unbescape.java.JavaEscape;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryBaseListener;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.AfterContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.AuthorContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.BeforeContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.CommitterContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.MessageContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.PathContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.RevisionContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.RevisionExclusionContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.RevisionRangeContext;
import com.pmease.gitplex.web.page.repository.commit.CommitQueryParser.SingleRevisionContext;

public class LogCommandFiller extends CommitQueryBaseListener {

	private final LogCommand logCommand;
	
	private ParseTreeProperty<String> value = new ParseTreeProperty<>();

	public LogCommandFiller(LogCommand logCommand) {
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
		String value = JavaEscape.unescapeJava(getValue(ctx.Value()));
		value = StringUtils.replace(value, "*", ".*");
		logCommand.committers().add(value);
	}
	
	@Override
	public void exitAuthor(AuthorContext ctx) {
		String value = JavaEscape.unescapeJava(getValue(ctx.Value()));
		value = StringUtils.replace(value, "*", ".*");
		logCommand.authors().add(value);
	}
	
	@Override
	public void exitPath(PathContext ctx) {
		logCommand.paths().add(getValue(ctx.Value()));
	}
	
	@Override
	public void exitMessage(MessageContext ctx) {
		logCommand.messages().add(JavaEscape.unescapeJava(getValue(ctx.Value())));
	}
	
}
