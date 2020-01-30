package io.onedev.server.git;

import java.io.Serializable;

public interface BlobIdentFilter extends Serializable {
	
	boolean filter(BlobIdent blobIdent);
	
	public static final BlobIdentFilter ALL = new BlobIdentFilter() {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean filter(BlobIdent blobIdent) {
			return true;
		}
		
	};
	
	public static final BlobIdentFilter TREE = new BlobIdentFilter() {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean filter(BlobIdent blobIdent) {
			return blobIdent.isTree();
		}
		
	};
	
}
