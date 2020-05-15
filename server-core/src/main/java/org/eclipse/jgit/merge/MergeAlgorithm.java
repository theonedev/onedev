/*
 * Copyright (C) 2009, Christian Halstrick <christian.halstrick@sap.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;
import org.eclipse.jgit.merge.MergeChunk.ConflictState;

import io.onedev.server.git.UseOursOnConflict;

/**
 * Provides the merge algorithm which does a three-way merge on content provided
 * as RawText. By default {@link org.eclipse.jgit.diff.HistogramDiff} is used as
 * diff algorithm.
 */
public final class MergeAlgorithm {
	private final DiffAlgorithm diffAlg;

	/**
	 * Creates a new MergeAlgorithm which uses
	 * {@link org.eclipse.jgit.diff.HistogramDiff} as diff algorithm
	 */
	public MergeAlgorithm() {
		this(new HistogramDiff());
	}

	/**
	 * Creates a new MergeAlgorithm
	 *
	 * @param diff
	 *            the diff algorithm used by this merge
	 */
	public MergeAlgorithm(DiffAlgorithm diff) {
		this.diffAlg = diff;
	}

	// An special edit which acts as a sentinel value by marking the end the
	// list of edits
	private static final Edit END_EDIT = new Edit(Integer.MAX_VALUE,
			Integer.MAX_VALUE);

	private static boolean isEndEdit(Edit edit) {
		return edit == END_EDIT;
	}

	/**
	 * Does the three way merge between a common base and two sequences.
	 *
	 * @param cmp comparison method for this execution.
	 * @param base the common base sequence
	 * @param ours the first sequence to be merged
	 * @param theirs the second sequence to be merged
	 * @return the resulting content
	 */
	public <S extends Sequence> MergeResult<S> merge(
			SequenceComparator<S> cmp, S base, S ours, S theirs) {
		List<S> sequences = new ArrayList<>(3);
		sequences.add(base);
		sequences.add(ours);
		sequences.add(theirs);
		MergeResult<S> result = new MergeResult<>(sequences);

		boolean useOursOnConflict = UseOursOnConflict.get();
		
		if (ours.size() == 0) {
			if (theirs.size() != 0) {
				EditList theirsEdits = diffAlg.diff(cmp, base, theirs);
				if (!theirsEdits.isEmpty()) {
					// we deleted, they modified -> Let their complete content
					// conflict with empty text
					if (useOursOnConflict) {
						result.add(1, 0, 0, ConflictState.NO_CONFLICT);
					} else {
						result.add(1, 0, 0, ConflictState.FIRST_CONFLICTING_RANGE);
						result.add(2, 0, theirs.size(),
								ConflictState.NEXT_CONFLICTING_RANGE);
					} 
				} else
					// we deleted, they didn't modify -> Let our deletion win
					result.add(1, 0, 0, ConflictState.NO_CONFLICT);
			} else
				// we and they deleted -> return a single chunk of nothing
				result.add(1, 0, 0, ConflictState.NO_CONFLICT);
			return result;
		} else if (theirs.size() == 0) {
			EditList oursEdits = diffAlg.diff(cmp, base, ours);
			if (!oursEdits.isEmpty()) {
				// we modified, they deleted -> Let our complete content
				// conflict with empty text
				if (useOursOnConflict) {
					result.add(1, 0, ours.size(), ConflictState.NO_CONFLICT);
				} else {
					result.add(1, 0, ours.size(),
							ConflictState.FIRST_CONFLICTING_RANGE);
					result.add(2, 0, 0, ConflictState.NEXT_CONFLICTING_RANGE);
				} 
			} else
				// they deleted, we didn't modify -> Let their deletion win
				result.add(2, 0, 0, ConflictState.NO_CONFLICT);
			return result;
		}

		EditList oursEdits = diffAlg.diff(cmp, base, ours);
		Iterator<Edit> baseToOurs = oursEdits.iterator();
		EditList theirsEdits = diffAlg.diff(cmp, base, theirs);
		Iterator<Edit> baseToTheirs = theirsEdits.iterator();
		int current = 0; // points to the next line (first line is 0) of base
		                 // which was not handled yet
		Edit oursEdit = nextEdit(baseToOurs);
		Edit theirsEdit = nextEdit(baseToTheirs);

		// iterate over all edits from base to ours and from base to theirs
		// leave the loop when there are no edits more for ours or for theirs
		// (or both)
		while (!isEndEdit(theirsEdit) || !isEndEdit(oursEdit)) {
			if (oursEdit.getEndA() < theirsEdit.getBeginA()) {
				// something was changed in ours not overlapping with any change
				// from theirs. First add the common part in front of the edit
				// then the edit.
				if (current != oursEdit.getBeginA()) {
					result.add(0, current, oursEdit.getBeginA(),
							ConflictState.NO_CONFLICT);
				}
				result.add(1, oursEdit.getBeginB(), oursEdit.getEndB(),
						ConflictState.NO_CONFLICT);
				current = oursEdit.getEndA();
				oursEdit = nextEdit(baseToOurs);
			} else if (theirsEdit.getEndA() < oursEdit.getBeginA()) {
				// something was changed in theirs not overlapping with any
				// from ours. First add the common part in front of the edit
				// then the edit.
				if (current != theirsEdit.getBeginA()) {
					result.add(0, current, theirsEdit.getBeginA(),
							ConflictState.NO_CONFLICT);
				}
				result.add(2, theirsEdit.getBeginB(), theirsEdit.getEndB(),
						ConflictState.NO_CONFLICT);
				current = theirsEdit.getEndA();
				theirsEdit = nextEdit(baseToTheirs);
			} else {
				// here we found a real overlapping modification

				// if there is a common part in front of the conflict add it
				if (oursEdit.getBeginA() != current
						&& theirsEdit.getBeginA() != current) {
					result.add(0, current, Math.min(oursEdit.getBeginA(),
							theirsEdit.getBeginA()), ConflictState.NO_CONFLICT);
				}

				// set some initial values for the ranges in A and B which we
				// want to handle
				int oursBeginB = oursEdit.getBeginB();
				int theirsBeginB = theirsEdit.getBeginB();
				// harmonize the start of the ranges in A and B
				if (oursEdit.getBeginA() < theirsEdit.getBeginA()) {
					theirsBeginB -= theirsEdit.getBeginA()
							- oursEdit.getBeginA();
				} else {
					oursBeginB -= oursEdit.getBeginA() - theirsEdit.getBeginA();
				}

				// combine edits:
				// Maybe an Edit on one side corresponds to multiple Edits on
				// the other side. Then we have to combine the Edits of the
				// other side - so in the end we can merge together two single
				// edits.
				//
				// It is important to notice that this combining will extend the
				// ranges of our conflict always downwards (towards the end of
				// the content). The starts of the conflicting ranges in ours
				// and theirs are not touched here.
				//
				// This combining is an iterative process: after we have
				// combined some edits we have to do the check again. The
				// combined edits could now correspond to multiple edits on the
				// other side.
				//
				// Example: when this combining algorithm works on the following
				// edits
				// oursEdits=((0-5,0-5),(6-8,6-8),(10-11,10-11)) and
				// theirsEdits=((0-1,0-1),(2-3,2-3),(5-7,5-7))
				// it will merge them into
				// oursEdits=((0-8,0-8),(10-11,10-11)) and
				// theirsEdits=((0-7,0-7))
				//
				// Since the only interesting thing to us is how in ours and
				// theirs the end of the conflicting range is changing we let
				// oursEdit and theirsEdit point to the last conflicting edit
				Edit nextOursEdit = nextEdit(baseToOurs);
				Edit nextTheirsEdit = nextEdit(baseToTheirs);
				for (;;) {
					if (oursEdit.getEndA() >= nextTheirsEdit.getBeginA()) {
						theirsEdit = nextTheirsEdit;
						nextTheirsEdit = nextEdit(baseToTheirs);
					} else if (theirsEdit.getEndA() >= nextOursEdit.getBeginA()) {
						oursEdit = nextOursEdit;
						nextOursEdit = nextEdit(baseToOurs);
					} else {
						break;
					}
				}

				// harmonize the end of the ranges in A and B
				int oursEndB = oursEdit.getEndB();
				int theirsEndB = theirsEdit.getEndB();
				if (oursEdit.getEndA() < theirsEdit.getEndA()) {
					oursEndB += theirsEdit.getEndA() - oursEdit.getEndA();
				} else {
					theirsEndB += oursEdit.getEndA() - theirsEdit.getEndA();
				}

				// A conflicting region is found. Strip off common lines in
				// in the beginning and the end of the conflicting region

				// Determine the minimum length of the conflicting areas in OURS
				// and THEIRS. Also determine how much bigger the conflicting
				// area in THEIRS is compared to OURS. All that is needed to
				// limit the search for common areas at the beginning or end
				// (the common areas cannot be bigger then the smaller
				// conflicting area. The delta is needed to know whether the
				// complete conflicting area is common in OURS and THEIRS.
				int minBSize = oursEndB - oursBeginB;
				int BSizeDelta = minBSize - (theirsEndB - theirsBeginB);
				if (BSizeDelta > 0)
					minBSize -= BSizeDelta;

				int commonPrefix = 0;
				while (commonPrefix < minBSize
						&& cmp.equals(ours, oursBeginB + commonPrefix, theirs,
								theirsBeginB + commonPrefix))
					commonPrefix++;
				minBSize -= commonPrefix;
				int commonSuffix = 0;
				while (commonSuffix < minBSize
						&& cmp.equals(ours, oursEndB - commonSuffix - 1, theirs,
								theirsEndB - commonSuffix - 1))
					commonSuffix++;
				minBSize -= commonSuffix;

				// Add the common lines at start of conflict
				if (commonPrefix > 0)
					result.add(1, oursBeginB, oursBeginB + commonPrefix,
							ConflictState.NO_CONFLICT);

				// Add the conflict (Only if there is a conflict left to report)
				if (minBSize > 0 || BSizeDelta != 0) {
					if (useOursOnConflict) {
						result.add(1, oursBeginB + commonPrefix, oursEndB
								- commonSuffix,
								ConflictState.NO_CONFLICT);
					} else {
						result.add(1, oursBeginB + commonPrefix, oursEndB
								- commonSuffix,
								ConflictState.FIRST_CONFLICTING_RANGE);
						result.add(2, theirsBeginB + commonPrefix, theirsEndB
								- commonSuffix,
								ConflictState.NEXT_CONFLICTING_RANGE);
					}
				}

				// Add the common lines at end of conflict
				if (commonSuffix > 0)
					result.add(1, oursEndB - commonSuffix, oursEndB,
							ConflictState.NO_CONFLICT);

				current = Math.max(oursEdit.getEndA(), theirsEdit.getEndA());
				oursEdit = nextOursEdit;
				theirsEdit = nextTheirsEdit;
			}
		}
		// maybe we have a common part behind the last edit: copy it to the
		// result
		if (current < base.size()) {
			result.add(0, current, base.size(), ConflictState.NO_CONFLICT);
		}
		return result;
	}

	/**
	 * Helper method which returns the next Edit for an Iterator over Edits.
	 * When there are no more edits left this method will return the constant
	 * END_EDIT.
	 *
	 * @param it
	 *            the iterator for which the next edit should be returned
	 * @return the next edit from the iterator or END_EDIT if there no more
	 *         edits
	 */
	private static Edit nextEdit(Iterator<Edit> it) {
		return (it.hasNext() ? it.next() : END_EDIT);
	}
}
