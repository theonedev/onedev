package com.pmease.gitplex.web.page.repository.commit.query;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.BranchContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.IdContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.RevisionContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.RevisionExclusionContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.RevisionRangeContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.SingleRevisionContext;
import com.pmease.gitplex.web.page.repository.commit.query.CommitQueryParser.TagContext;

public class LogCommandDecorator extends CommitQueryBaseListener {

	private LogCommand logCommand;
	
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
		logCommand.revisions().add(value.get(ctx.revision(0)) + ctx.RANGE().getText() + value.get(ctx.revision(1)));
	}

	@Override
	public void exitRevision(RevisionContext ctx) {
		if (ctx.branch() != null)
			value.put(ctx, value.get(ctx.branch()));
		else if (ctx.tag() != null)
			value.put(ctx, value.get(ctx.tag()));
		else
			value.put(ctx, value.get(ctx.id()));
	}

	@Override
	public void exitTag(TagContext ctx) {
	}

	@Override
	public void exitId(IdContext ctx) {
		// TODO Auto-generated method stub
		super.exitId(ctx);
	}

	@Override
	public void exitBranch(BranchContext ctx) {
		// TODO Auto-generated method stub
		super.exitBranch(ctx);
	}
	
}
