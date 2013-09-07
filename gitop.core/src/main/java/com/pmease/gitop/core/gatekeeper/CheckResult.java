package com.pmease.gitop.core.gatekeeper;

import java.util.List;

import com.pmease.commons.util.EasyList;

public abstract class CheckResult {
	
	private final List<String> reasons;
	
	public CheckResult(List<String> reasons) {
		this.reasons = reasons;
	}
	
	public CheckResult(String reason) {
		this.reasons = EasyList.of(reason);
	}

	public List<String> getReasons() {
		return reasons;
	}
	
	public abstract boolean isAccept();
	
	public abstract boolean isReject();
	
	public abstract boolean isPending();
	
	public abstract boolean isBlock();
	
	/* accept the merge request. */
	public static class Accept extends CheckResult {
		
		public Accept(List<String> reasons) {
			super(reasons);
		}

		public Accept(String reason) {
			super(reason);
		}

		@Override
		public boolean isPending() {
			return false;
		}

		@Override
		public boolean isAccept() {
			return true;
		}

		@Override
		public boolean isReject() {
			return false;
		}

		@Override
		public boolean isBlock() {
			return false;
		}
		
	};
	
	/* reject the merge request. */
	public static class Reject extends CheckResult {

		public Reject(List<String> reasons) {
			super(reasons);
		}

		public Reject(String reason) {
			super(reason);
		}

		@Override
		public boolean isAccept() {
			return false;
		}

		@Override
		public boolean isReject() {
			return true;
		}

		@Override
		public boolean isPending() {
			return false;
		}

		@Override
		public boolean isBlock() {
			return false;
		}
		
	};
	
	/* merge request acceptance check is pending and result is unknown yet */
	public static class Pending extends CheckResult {

		public Pending(List<String> reasons) {
			super(reasons);
		}

		public Pending(String reason) {
			super(reason);
		}

		@Override
		public boolean isAccept() {
			return false;
		}

		@Override
		public boolean isReject() {
			return false;
		}

		@Override
		public boolean isPending() {
			return true;
		}

		@Override
		public boolean isBlock() {
			return false;
		}
	};
	
	/* 
	 * same as Pending, but followed gate keeper should not be checked unless result 
	 * of this gate keeper has been determined.
	 */
	public static class Block extends CheckResult {
		
		public Block(List<String> reasons) {
			super(reasons);
		}

		public Block(String reason) {
			super(reason);
		}

		@Override
		public boolean isPending() {
			return false;
		}

		@Override
		public boolean isAccept() {
			return false;
		}

		@Override
		public boolean isReject() {
			return false;
		}

		@Override
		public boolean isBlock() {
			return true;
		}
	};
	
}