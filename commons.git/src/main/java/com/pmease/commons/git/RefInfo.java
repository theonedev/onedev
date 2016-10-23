package com.pmease.commons.git;

import java.io.IOException;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

public class RefInfo implements Comparable<RefInfo> {

	private final Ref ref;
	
	private final RevObject obj;
	
	private final RevObject peeledObj;
	
	public RefInfo(RevWalk revWalk, Ref ref) {
		this.ref = ref;
		try {
			obj = revWalk.parseAny(ref.getObjectId());
			peeledObj = revWalk.peel(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RefInfo(Ref ref, RevObject obj, RevObject peeledObj) {
		this.ref = ref;
		this.obj = obj;
		this.peeledObj = peeledObj;
	}

	public Ref getRef() {
		return ref;
	}

	public RevObject getObj() {
		return obj;
	}

	public RevObject getPeeledObj() {
		return peeledObj;
	}

	@Override
	public int compareTo(RefInfo other) {
		if (obj instanceof RevTag && ((RevTag)obj).getTaggerIdent() != null) {
			if (other.obj instanceof RevTag && ((RevTag)other.obj).getTaggerIdent() != null) {
				return ((RevTag)obj).getTaggerIdent().getWhen().compareTo(((RevTag)other.obj).getTaggerIdent().getWhen());
			} else {
				return -1;
			}
		} else {
			if (other.obj instanceof RevTag && ((RevTag)other.obj).getTaggerIdent() != null) {
				return 1;
			} else {
				if (obj instanceof RevCommit) {
					if (other.peeledObj instanceof RevCommit) {
						return ((RevCommit)peeledObj).getCommitTime() - ((RevCommit)other.peeledObj).getCommitTime();
					} else {
						return -1;
					}
				} else {
					if (other.peeledObj instanceof RevCommit) {
						return 1;
					} else {
						return ref.getName().compareTo(other.ref.getName());
					}
				}
			}
		}
	}
	
}
