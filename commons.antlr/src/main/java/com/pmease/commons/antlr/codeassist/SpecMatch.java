package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class SpecMatch {

	private final List<TokenNode> paths;
	
	private final boolean matched;

	public SpecMatch(List<TokenNode> paths, boolean matched) {
		this.paths = paths;
		this.matched = matched;
	}

	/**
	 * Get different paths in this match
	 * 
	 * @return
	 * 			get different match paths, each path represents an alternative match 
	 * 			(full or partial) between the spec and the stream, and each of them 
	 * 			represents parse tree (nodes of the parse tree can be deducted by calling 
	 * 			{@link TokenNode#getPrevious()}. Also all these path point to the same token 
	 * 			as those shorter paths have been discarded due to our greedy match strategy. 
	 * 			A parse tree can be deducted from each path via 
	 * 			{@link TokenNode#getPrevious()} Note that this list may not be empty even if 
	 * 			{@link #isMatched()} return false, and in this case, the spec is only 
	 * 			partially matched, and these paths represent to which point this partial 
	 * 			match goes. 
	 * 			Specifically if the list is empty, no tokens are consumed from the stream
	 */
	public List<TokenNode> getPaths() {
		return paths;
	}

	/**
	 * Whether or not there is a match in above paths.
	 * 
	 * @return
	 * 			true as long as one of paths above represents a full match (the whole spec
	 * 			can be matched)
	 */
	public boolean isMatched() {
		return matched;
	}

}
