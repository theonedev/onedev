package com.pmease.commons.git;

import java.util.Date;

public class Commit {
    
    private Date date;
    
    private String author;
    
    private String committer;
    
    private String hash;
    
    private String subject;
    
    private String body;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
    
}
