package com.pmease.gitop.web.git.command;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.execution.Commandline;

public abstract class AbstractDiffCommand<T, S extends AbstractDiffCommand<T, S>> extends GitCommand<T> {

	public static enum RenameType {
		NONE, FIND_RENAMES, FIND_COPIES, FIND_COPIES_HEADER
	}

	public static enum WhitespaceType {
		NONE,
		
		// Ignore changes in whitespace at EOL.
		IGNORE_SPACE_AT_EOL,
		
		// Ignore changes in amount of whitespace. This ignores whitespace at 
		// line end, and considers all other sequences of one or more whitespace 
		// characters to be equivalent.
		IGNORE_SPACE_CHANGE,
		
		// Ignore whitespace when comparing lines. This ignores differences even 
		// if one line has whitespace where the other line has none.
		IGNORE_ALL_SPACE,
		
		// Ignore changes whose lines are all blank.
		IGNORE_BLANK_LINES
	}

	public static enum DiffAlgorithm {
		PATIENCE, MINIMAL, HISTOGRAM, MYERS
	}
	
	protected String rev;
	
	protected Set<String> paths = Sets.newLinkedHashSet();
	
	protected int contextLines;
	
	protected RenameType renameType = RenameType.FIND_RENAMES;
	
	protected int threshold = -1;
	
	protected WhitespaceType whitespaceType = WhitespaceType.NONE;
	
	protected DiffAlgorithm diffAlgorithm = DiffAlgorithm.MYERS; // git default
	
	protected String format;
	
	public AbstractDiffCommand(File repoDir) {
		super(repoDir);
	}

	protected abstract S self();
	
	public S revision(String rev) {
		this.rev = rev;
		return self();
	}
	
	public S path(String path) {
		this.paths.add(path);
		return self();
	}
	
	public S paths(List<String> paths) {
		this.paths.addAll(paths);
		return self();
	}
	
	public S contextLines(int contextLines) {
		this.contextLines = contextLines;
		return self();
	}
	
	public S findCopiesHarder(boolean b) {
		return renameType(b ? RenameType.FIND_COPIES_HEADER : RenameType.NONE);
	}
	
	public S findCopies(boolean b) {
		return findCopies(b, -1);
	}
	
	public S findCopies(boolean b, int threshold) {
		return renameType(b ? RenameType.FIND_COPIES : RenameType.NONE, threshold);
	}
	
	public S findRenames(boolean b) {
		return findRenames(b, -1);
	}
	
	public S findRenames(boolean b, int threshold) {
		return renameType(b ? RenameType.FIND_RENAMES : RenameType.NONE, threshold);
	}
	
	private S renameType(RenameType renameType) {
		return renameType(renameType, -1);
	}
	
	private S renameType(RenameType renameType, int threshold) {
		this.renameType = renameType;
		this.threshold = threshold;
		return self();
	}
	
	private S whitespaceType(WhitespaceType whitespaceType) {
		this.whitespaceType = whitespaceType;
		return self();
	}
	
	public S ignoreSpaceAtEol() {
		return whitespaceType(WhitespaceType.IGNORE_SPACE_AT_EOL);
	}
	
	public S ignoreAllSpace() {
		return whitespaceType(WhitespaceType.IGNORE_ALL_SPACE);
	}
	
	public S ignoreBlankLines() {
		return whitespaceType(WhitespaceType.IGNORE_BLANK_LINES);
	}
	
	public S ignoreSpaceChange() {
		return whitespaceType(WhitespaceType.IGNORE_SPACE_CHANGE);
	}
	
	public S diffAlgorithm(DiffAlgorithm algorithm) {
		this.diffAlgorithm = algorithm;
		return self();
	}
	
	public S format(String format) {
		this.format = format;
		return self();
	}
	
	@Override
	protected String getGitExe() {
		if (AppLoader.injector == null) {
			return "git";
		} else {
			return super.getGitExe();
		}
	}
	
	abstract protected String getSubCommand();
	
	protected Commandline newCommand() {
		Commandline cmd = cmd();
		cmd.addArgs(getSubCommand(), rev, "--full-index", "-p", "--no-color");
		return cmd;
	}
	
	protected Commandline buildCommand() {
		Commandline cmd = newCommand();
		
		addArgContextLines(cmd);
		addArgWhitespaceType(cmd);
		addArgRenameType(cmd);
		addArgDiffAlgorithm(cmd);
		addArgFormat(cmd);
		addArgPaths(cmd);
		
		return cmd;
	}
	
	protected void addArgFormat(Commandline cmd) {
		if (!Strings.isNullOrEmpty(format)) {
			cmd.addArgs("--format=", format);
		}
	}
	
	protected void addArgContextLines(Commandline cmd) {
		if (contextLines > 0) {
			cmd.addArgs("-U" + contextLines);
		}
	}
	
	protected void addArgPaths(Commandline cmd) {
		if (paths.isEmpty())
			return;
		
		cmd.addArgs("--");
		for (String path : paths) {
			if (!Strings.isNullOrEmpty(path)) {
				cmd.addArgs(path);
			}
		}
	}
	
	protected void addArgWhitespaceType(Commandline cmd) {
		switch (whitespaceType) {
		case IGNORE_SPACE_AT_EOL:
			cmd.addArgs("--ignore-space-at-eol");
			break;
			
		case IGNORE_SPACE_CHANGE:
			cmd.addArgs("--ignore-space-change");
			break;
			
		case IGNORE_ALL_SPACE:
			cmd.addArgs("--ignore-all-space");
			break;
			
		case IGNORE_BLANK_LINES:
			cmd.addArgs("--ignore-blank-lines");
			break;
			
		default:
			break;
		}
	}
	
	protected void addArgRenameType(Commandline cmd) {
		if (renameType == RenameType.FIND_COPIES_HEADER) {
			cmd.addArgs("--find-copies-harder");
		} else if (renameType == RenameType.FIND_COPIES) {
			cmd.addArgs("-C" + (threshold > 0 ? threshold : ""));
		} else if (renameType == RenameType.FIND_RENAMES) {
			cmd.addArgs("-M" + (threshold > 0 ? threshold : ""));
		}
	}
	
	protected void addArgDiffAlgorithm(Commandline cmd) {
		if (diffAlgorithm != DiffAlgorithm.MYERS) {
			cmd.addArgs("--" + diffAlgorithm.name().toLowerCase());
		}
	}
	
}
