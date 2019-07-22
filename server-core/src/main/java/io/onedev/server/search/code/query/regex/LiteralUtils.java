package io.onedev.server.search.code.query.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

public class LiteralUtils {
	
	/**
	 * Trim unnecessary rows in specified row. For instance, considering below rows:
	 * <li>a & b 
	 * <li>ac & bd 
	 * The first row states that both 'a' and 'b' should occur, and the second row 
	 * states that 'ab' should occur. Combination of these two rows states that one 
	 * of the two rows should occur. When <tt>outmost</tt> is true, we know that these 
	 * rows will not be concatenated to other rows, so as long as all literals of 
	 * first row is a substring of second row, we can discard second row, and vice 
	 * versa. In this case, the second row will be discarded if param <tt>outmost</tt>
	 * is set to true.<br> 
	 * If however param <tt>outmost</tt> is set to false, we know that these two rows 
	 * can still be concatenated with other rows, so simply checking substring 
	 * occurrences is not sufficient, and we need to take prefix and suffix into user, 
	 * so the rule to discard row2 becomes: 
	 * <li> if start literals of row2 starts with prefix of row1 (prefix of row1 is 
	 * defined as literals before the first opaque literal)
	 * <li> if end literals of row2 ends with suffix of row1 (suffix of row1 is 
	 * defined as literals after the last opaque literal)
	 * <li> literals between first and last opaque literal of row1 is substring of 
	 * literals of row2
	 * 
	 * @param rows
	 * @param outmost
	 */
	public static void trim(List<List<LeafLiterals>> rows, boolean outmost) {
		Set<Integer> trimmed = new HashSet<>();
		
		int index1 = 0;
		for (List<LeafLiterals> row1: rows) {
			int index2 = 0;
			for (List<LeafLiterals> row2: rows) {
				if (!trimmed.contains(index1) && !trimmed.contains(index2) && index1!=index2) {
					if (outmost) {
						if (covers(row2, row1))
							trimmed.add(index2);
					} else {
						String prefix1 = getPrefix(row1, true);
						String suffix1 = getSuffix(row1, true);
						String prefix2 = getPrefix(row2, false);
						String suffix2 = getSuffix(row2, false);
						if (prefix1 != null && prefix2.startsWith(prefix1) 
								&& suffix1 != null && suffix2.endsWith(suffix1)
								&& covers(row2, getMiddle(row1))) {
							trimmed.add(index2);
						}
					}
				}
				index2++;
			}
			index1++;
		}
		
		int index = 0;
		for (Iterator<List<LeafLiterals>> it = rows.iterator(); it.hasNext();) {
			it.next();
			if (trimmed.contains(index))
				it.remove();
			index++;
		}
	}

	// row1 covers row2 if row1 covers all literals of row2
	private static boolean covers(List<LeafLiterals> row1, List<LeafLiterals> row2) {
		boolean covers = true;
		for (LeafLiterals literals: row2) {
			if (!covers(row1, literals)) {
				covers = false;
				break;
			}
		}
		return covers;
	}
	
	// row covers literals if literals is substring of any literals of row
	private static boolean covers(List<LeafLiterals> row, LeafLiterals leaf) {
		if (leaf.getLiteral() != null) {
			boolean covers = false;
			for (LeafLiterals each: row) {
				if (each.getLiteral() != null && each.getLiteral().contains(leaf.getLiteral())) {
					covers = true;
					break;
				}
			}
			return covers;
		} else {
			return true;
		}
	}
	
	@Nullable
	private static String getPrefix(List<LeafLiterals> row, boolean mustBeforeOpaque) {
		int firstOpaqueIndex = getFirstOpaqueIndex(row);
		if (firstOpaqueIndex == -1) {
			if (mustBeforeOpaque)
				return null;
			else if (row.size() == 0)
				return "";
			else if (row.size() == 1)
				return Preconditions.checkNotNull(row.get(0).getLiteral());
			else
				throw new IllegalStateException();
		} else if (firstOpaqueIndex == 0) {
			return "";
		} else { 
			return Preconditions.checkNotNull(row.get(0).getLiteral());
		}
	}
	
	@Nullable
	private static String getSuffix(List<LeafLiterals> row, boolean mustAfterOpaque) {
		int lastOpaqueIndex = getLastOpaqueIndex(row);
		if (lastOpaqueIndex == -1) {
			if (mustAfterOpaque)
				return null;
			else if (row.size() == 0)
				return "";
			else if (row.size() == 1)
				return Preconditions.checkNotNull(row.get(0).getLiteral());
			else
				throw new IllegalStateException();
		} else if (lastOpaqueIndex == row.size()-1) {
			return "";
		} else { 
			return Preconditions.checkNotNull(row.get(row.size()-1).getLiteral());
		}
	}

	private static int getFirstOpaqueIndex(List<LeafLiterals> row) {
		for (int i=0; i<row.size(); i++) {
			if (row.get(i).getLiteral() == null) 
				return i;
		}
		return -1;
	}
	
	private static int getLastOpaqueIndex(List<LeafLiterals> row) {
		for (int i=row.size()-1; i>=0; i--) {
			if (row.get(i).getLiteral() == null) 
				return i;
		}
		return -1;
	}
	
	private static List<LeafLiterals> getMiddle(List<LeafLiterals> row) {
		int firstOpaqueIndex = getFirstOpaqueIndex(row);
		int lastOpaqueIndex = getLastOpaqueIndex(row);
		
		if (firstOpaqueIndex != lastOpaqueIndex)
			return row.subList(firstOpaqueIndex+1, lastOpaqueIndex);
		else
			return new ArrayList<>();
	}
	
	/**
	 * Merge specified list of leaf literals to concatenate continuous non-opaque literals.
	 * 
	 * @param row
	 * @return
	 * 			list of merged literals
	 */
	public static List<LeafLiterals> merge(List<LeafLiterals> row, boolean outmost) {
		List<LeafLiterals> mergedRow = new ArrayList<>();
		
		LeafLiterals merging = null;
		for (LeafLiterals leaf: row) {
			if (leaf.getLiteral() != null) {
				if (merging == null) {
					merging = new LeafLiterals(leaf.getLiteral());
				} else if (merging.getLiteral() != null) {
					merging = new LeafLiterals(merging.getLiteral() + leaf.getLiteral());
				} else {
					mergedRow.add(merging);
					merging = new LeafLiterals(leaf.getLiteral());
				}
			} else if (merging == null) {
				merging = new LeafLiterals(leaf.getLiteral());
			} else if (merging.getLiteral() != null) {
				mergedRow.add(merging);
				merging = new LeafLiterals(leaf.getLiteral());
			}
		}
		if (merging != null)
			mergedRow.add(merging);
		
		/*
		 * Below code checks if some leaf literal is substring of other leaf literals, and 
		 * if yes, discard it.
		 */
		Set<Integer> trimmed = new HashSet<>();
		
		int from, to;
		if (outmost) {
			from = 0;
			to = mergedRow.size();
		} else {
			from = getFirstOpaqueIndex(mergedRow);
			to = getLastOpaqueIndex(mergedRow);
		}
		for (int i1=from; i1<to; i1++) {
			String literal1 = mergedRow.get(i1).getLiteral();
			if (literal1 != null) {
				boolean covered = false;
				for (int i2=0; i2<mergedRow.size(); i2++) {
					if (i1 != i2 && !trimmed.contains(i1) && !trimmed.contains(i2)) {
						String literal2 = mergedRow.get(i2).getLiteral();
						if (literal2 != null && literal2.contains(literal1)) {
							covered = true;
							break;
						}
					}
				}
				if (covered)
					trimmed.add(i1);
			}
		}

		LeafLiterals prevLeaf = null;
		int index = 0;
		for (Iterator<LeafLiterals> it = mergedRow.iterator(); it.hasNext();) {
			LeafLiterals leaf = it.next();
			if (trimmed.contains(index) 
					|| (leaf.getLiteral() == null && prevLeaf != null && prevLeaf.getLiteral() == null)) {
				it.remove();
			} else {
				prevLeaf = leaf; 
			}
			index++;
		}

		return mergedRow;
	}

}
