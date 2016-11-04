package com.gitplex.server.web.page.depot.commit;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.unbescape.java.JavaEscape;

import com.gitplex.commons.git.command.RevListCommand;
import com.gitplex.web.page.depot.commit.CommitQueryBaseListener;
import com.gitplex.web.page.depot.commit.CommitQueryParser.AfterContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.AuthorContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.BeforeContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.CommitterContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.MessageContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.PathContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.RevisionContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.RevisionExclusionContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.RevisionRangeContext;
import com.gitplex.web.page.depot.commit.CommitQueryParser.SingleRevisionContext;

public class RevListCommandFiller extends CommitQueryBaseListener {

	private final RevListCommand command;
	
	private ParseTreeProperty<String> value = new ParseTreeProperty<>();

	public RevListCommandFiller(RevListCommand command) {
		this.command = command;
	}

	@Override
	public void exitSingleRevision(SingleRevisionContext ctx) {
		command.revisions().add(value.get(ctx.revision()));
	}

	@Override
	public void exitRevisionExclusion(RevisionExclusionContext ctx) {
		command.revisions().add("^" + value.get(ctx.revision()));
	}

	@Override
	public void exitRevisionRange(RevisionRangeContext ctx) {
		command.revisions().add(value.get(ctx.revision(0)) + ctx.Range().getText() + value.get(ctx.revision(1)));
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
		command.after(getValue(ctx.Value()));
	}

	@Override
	public void exitBefore(BeforeContext ctx) {
		command.before(getValue(ctx.Value()));
	}
	
	@Override
	public void exitCommitter(CommitterContext ctx) {
		String value = JavaEscape.unescapeJava(getValue(ctx.Value()));
		value = StringUtils.replace(value, "*", ".*");
		command.committers().add(value);
	}
	
	@Override
	public void exitAuthor(AuthorContext ctx) {
		String value = JavaEscape.unescapeJava(getValue(ctx.Value()));
		value = StringUtils.replace(value, "*", ".*");
		command.authors().add(value);
	}
	
	@Override
	public void exitPath(PathContext ctx) {
		command.paths().add(getValue(ctx.Value()));
	}
	
	@Override
	public void exitMessage(MessageContext ctx) {
		command.messages().add(JavaEscape.unescapeJava(getValue(ctx.Value())));
	}
	
}
