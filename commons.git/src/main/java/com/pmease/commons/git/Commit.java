package com.pmease.commons.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

@SuppressWarnings("serial")
public class Commit extends BriefCommit {

    private final String body;
    
    private final String note;
    
    private final List<String> parentHashes;

    public Commit(String hash, PersonIdent committer, PersonIdent author, String subject, 
    		@Nullable String body, List<String> parentHashes, @Nullable String note) {
    	super(hash, committer, author, subject);
    	
    	this.body = body;
    	this.parentHashes = new ArrayList<>(checkNotNull(parentHashes, "parentHashes"));
    	this.note = note;
    }
    
    public Commit(BriefCommit briefCommit, @Nullable String body, 
    		List<String> parentHashes, @Nullable String note) {
    	super(briefCommit);
    	
    	this.body = body;
    	this.parentHashes = new ArrayList<>(checkNotNull(parentHashes, "parentHashes"));
    	this.note = note;
    }
    
	public String getMessage() {
		if (body != null)
			return getSubject() + "\n\n" + body;
		else
			return getSubject();
	}

	@Nullable
	public String getNote() {
		return note;
	}

	public List<String> getParentHashes() {
		return Collections.unmodifiableList(parentHashes);
	}
	
	@Nullable
	public String getBody() {
		return body;
	}

    public static class Builder extends BriefCommit.Builder {
    	
    	public String body;
		
    	public List<String> parentHashes = new ArrayList<>();
		
    	public String note;
		
		public Commit build() {
			return new Commit(super.build(), StringUtils.isNotBlank(body)?body.trim():null, parentHashes, note);
		}
	}
	
    public static Builder builder() {
    	return new Builder();
    }

}
