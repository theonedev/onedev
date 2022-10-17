package io.onedev.server.git.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

public class RefFacade implements Comparable<RefFacade>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final RevObject obj;
	
	private final RevObject peeledObj;
	
	public RefFacade(RevWalk revWalk, Ref ref) {
		name = ref.getName();
		try {
			obj = revWalk.parseAny(ref.getObjectId());
			peeledObj = revWalk.peel(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RefFacade(Ref ref, RevObject obj, RevObject peeledObj) {
		name = ref.getName();
		this.obj = obj;
		this.peeledObj = peeledObj;
	}

	public String getName() {
		return name;
	}
	
	public ObjectId getObjectId() {
		return obj.copy();
	}

	public RevObject getObj() {
		return obj;
	}

	public RevObject getPeeledObj() {
		return peeledObj;
	}

	@Override
	public int compareTo(RefFacade other) {
		Date date;
		if (obj instanceof RevTag && ((RevTag)obj).getTaggerIdent() != null) {
			date =  ((RevTag)obj).getTaggerIdent().getWhen();
		} else if (peeledObj instanceof RevCommit) {
			date = ((RevCommit)peeledObj).getCommitterIdent().getWhen();
		} else {
			date = null;
		}
		Date otherDate;
		if (other.obj instanceof RevTag && ((RevTag)other.obj).getTaggerIdent() != null) {
			otherDate =  ((RevTag)other.obj).getTaggerIdent().getWhen();
		} else if (other.peeledObj instanceof RevCommit) {
			otherDate = ((RevCommit)other.peeledObj).getCommitterIdent().getWhen();
		} else {
			otherDate = null;
		}
		
		if (date != null) {
			if (otherDate != null)
				return date.compareTo(otherDate);
			else
				return 1;
		} else {
			if (otherDate != null)
				return -1;
			else
				return name.compareTo(other.name);
		}
	}
	
}
