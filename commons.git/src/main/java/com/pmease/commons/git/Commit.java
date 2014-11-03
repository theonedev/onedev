package com.pmease.commons.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

@SuppressWarnings("serial")
public class Commit extends BriefCommit {

    private final String messageBody;
    
    private final String note;
    
    private final List<String> parentHashes;

    public Commit(String hash, PersonIdent committer, PersonIdent author, String messageSummary, 
    		@Nullable String messageBody, List<String> parentHashes, @Nullable String note) {
    	super(hash, committer, author, messageSummary);
    	
    	this.parentHashes = checkNotNull(parentHashes, "parentHashes");
    	this.messageBody = messageBody;
    	this.note = note;
    }
    
	public String getMessage() {
		if (messageBody != null)
			return getMessageSummary() + "\n\n" + messageBody;
		else
			return getMessageSummary();
	}

	@Nullable
	public String getNote() {
		return note;
	}

	public List<String> getParentHashes() {
		return parentHashes;
	}
	
	@Nullable
	public String getMessageBody() {
		return messageBody;
	}

    public static class Builder extends BriefCommit.Builder {
    	
    	public String messageBody;
		
    	public List<String> parentHashes = new ArrayList<>();
		
    	public String note;
		
		public Commit build() {
			BriefCommit briefCommit = super.build();
			return new Commit(briefCommit.getHash(), briefCommit.getCommitter(), briefCommit.getAuthor(), 
					briefCommit.getMessageSummary(), messageBody, parentHashes, note);
		}
	}
	
    public static Builder builder() {
    	return new Builder();
    }

}
