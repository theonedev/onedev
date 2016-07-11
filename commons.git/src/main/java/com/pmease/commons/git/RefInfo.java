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
	public int compareTo(RefInfo o) {
		if (obj instanceof RevTag) {
			if (o.obj instanceof RevTag) {
				return ((RevTag)obj).getTaggerIdent().getWhen().compareTo(((RevTag)o.obj).getTaggerIdent().getWhen());
			} else {
				return -1;
			}
		} else {
			if (o.obj instanceof RevTag) {
				return 1;
			} else {
				if (obj instanceof RevCommit) {
					if (o.peeledObj instanceof RevCommit) {
						return ((RevCommit)peeledObj).getCommitTime() - ((RevCommit)o.peeledObj).getCommitTime();
					} else {
						return -1;
					}
				} else {
					if (o.peeledObj instanceof RevCommit) {
						return 1;
					} else {
						return ref.getName().compareTo(o.ref.getName());
					}
				}
			}
		}
	}
	
}
