package com.pmease.gitplex.web.page.repository.code.commit.diff.patch;

import static org.eclipse.jgit.util.RawParseUtils.match;
import static org.eclipse.jgit.util.RawParseUtils.nextLF;
import static org.eclipse.jgit.util.RawParseUtils.parseBase10;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.util.MutableInteger;
import org.eclipse.jgit.util.RawParseUtils;

import com.google.common.collect.Lists;
import com.pmease.commons.util.Charsets;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.HunkLine.LineType;

/** Hunk header describing the layout of a single block of lines */
public class HunkHeader {
	/** Details about an old image of the file. */
	public abstract static class OldImage {
		/** First line number the hunk starts on in this file. */
		int startLine;

		/** Total number of lines this hunk covers in this file. */
		int lineCount;

		/** Number of lines deleted by the post-image from this file. */
		int nDeleted;

		/** Number of lines added by the post-image not in this file. */
		int nAdded;

		/** @return first line number the hunk starts on in this file. */
		public int getStartLine() {
			return startLine;
		}

		/** @return total number of lines this hunk covers in this file. */
		public int getLineCount() {
			return lineCount;
		}

		/** @return number of lines deleted by the post-image from this file. */
		public int getLinesDeleted() {
			return nDeleted;
		}

		/** @return number of lines added by the post-image not in this file. */
		public int getLinesAdded() {
			return nAdded;
		}

		/** @return object id of the pre-image file. */
		public abstract AbbreviatedObjectId getId();
	}

	final FileHeader file;

	/** Offset within {@link #file}.buf to the "@@ -" line. */
	final int startOffset;

	/** Position 1 past the end of this hunk within {@link #file}'s buf. */
	int endOffset;

	private final OldImage old;

	/** First line number in the post-image file where the hunk starts */
	int newStartLine;

	/** Total number of post-image lines this hunk covers (context + inserted) */
	int newLineCount;

	/** Total number of lines of context appearing in this hunk */
	int nContext;

	private EditList editList;

	HunkHeader(final FileHeader fh, final int offset) {
		this(fh, offset, new OldImage() {
			@Override
			public AbbreviatedObjectId getId() {
				return fh.getOldId();
			}
		});
	}

	HunkHeader(final FileHeader fh, final int offset, final OldImage oi) {
		file = fh;
		startOffset = offset;
		old = oi;
	}

	HunkHeader(final FileHeader fh, final EditList editList) {
		this(fh, fh.buf.length);
		this.editList = editList;
		endOffset = startOffset;
		nContext = 0;
		if (editList.isEmpty()) {
			newStartLine = 0;
			newLineCount = 0;
		} else {
			newStartLine = editList.get(0).getBeginB();
			Edit last = editList.get(editList.size() - 1);
			newLineCount = last.getEndB() - newStartLine;
		}
	}

	/** @return header for the file this hunk applies to */
	public FileHeader getFileHeader() {
		return file;
	}

	/** @return the byte array holding this hunk's patch script. */
	public byte[] getBuffer() {
		return file.buf;
	}

	/** @return offset the start of this hunk in {@link #getBuffer()}. */
	public int getStartOffset() {
		return startOffset;
	}

	/** @return offset one past the end of the hunk in {@link #getBuffer()}. */
	public int getEndOffset() {
		return endOffset;
	}

	/** @return information about the old image mentioned in this hunk. */
	public OldImage getOldImage() {
		return old;
	}

	/** @return first line number in the post-image file where the hunk starts */
	public int getNewStartLine() {
		return newStartLine;
	}

	/** @return Total number of post-image lines this hunk covers */
	public int getNewLineCount() {
		return newLineCount;
	}

	public int getNewEndLine() {
		return newStartLine + newLineCount - 1;
	}
	
	public int getOldEndLine() {
		return old.startLine + old.lineCount - 1;
	}
	
	/** @return total number of lines of context appearing in this hunk */
	public int getLinesContext() {
		return nContext;
	}

	/** @return a list describing the content edits performed within the hunk. */
	public EditList toEditList() {
		if (editList == null) {
			editList = new EditList();
			final byte[] buf = file.buf;
			int c = nextLF(buf, startOffset);
			int oLine = old.startLine;
			int nLine = newStartLine;
			Edit in = null;

			SCAN: for (; c < endOffset; c = nextLF(buf, c)) {
				switch (buf[c]) {
				case ' ':
				case '\n':
					in = null;
					oLine++;
					nLine++;
					continue;

				case '-':
					if (in == null) {
						in = new Edit(oLine - 1, nLine - 1);
						editList.add(in);
					}
					oLine++;
					in.extendA();
					continue;

				case '+':
					if (in == null) {
						in = new Edit(oLine - 1, nLine - 1);
						editList.add(in);
					}
					nLine++;
					in.extendB();
					continue;

				case '\\': // Matches "\ No newline at end of file"
					continue;

				default:
					break SCAN;
				}
			}
		}
		return editList;
	}

	String hunkFunction;
	
	void parseHeader() {
		// Parse "@@ -236,9 +236,9 @@ protected boolean"
		//
		final byte[] buf = file.buf;
		final MutableInteger ptr = new MutableInteger();
		ptr.value = nextLF(buf, startOffset, ' ');
		old.startLine = -parseBase10(buf, ptr.value, ptr);
		if (buf[ptr.value] == ',')
			old.lineCount = parseBase10(buf, ptr.value + 1, ptr);
		else
			old.lineCount = 1;

		newStartLine = parseBase10(buf, ptr.value + 1, ptr);
		if (buf[ptr.value] == ',')
			newLineCount = parseBase10(buf, ptr.value + 1, ptr);
		else
			newLineCount = 1;

		// decode from " @@ protected boolean"
		//
		this.hunkFunction = RawParseUtils.decode(buf, ptr.value + 4, nextLF(buf, ptr.value));
		if (this.hunkFunction.endsWith("\n")) {
			this.hunkFunction = this.hunkFunction.substring(0, hunkFunction.length() - 1);
		}
	}

	public String getHunkFunction() {
		return hunkFunction;
	}
	
	List<HunkLine> lines = Lists.newArrayList();
	
	public List<HunkLine> getLines() {
		return lines;
	}
	
	public Charset getCharset() {
		return Charsets.detectFrom(new ByteArrayInputStream(file.buf));
	}
	
	int parseBody(final Patch script, final int end) {
		final byte[] buf = file.buf;
		
		Charset charset = getCharset();
		
		int c = nextLF(buf, startOffset), last = c;

		old.nDeleted = 0;
		old.nAdded = 0;

		int oldLineNo = old.startLine;
		int newLineNo = newStartLine;
		
		HunkLine al = null;
		SCAN: for (; c < end; last = c, c = nextLF(buf, c)) {
			String line;
			int eol;
			switch (buf[c]) {
			case ' ':
			case '\n':
				nContext++;
				eol = nextLF(buf, c);
				line = RawParseUtils.decode(charset, buf, c + 1, eol - 1);
				al = new HunkLine(line, LineType.CONTEXT, oldLineNo, newLineNo);
				lines.add(al);
				oldLineNo++;
				newLineNo++;
				continue;

			case '-':
				old.nDeleted++;
				eol = nextLF(buf, c);
				line = RawParseUtils.decode(charset, buf, c + 1, eol - 1);
				al = new HunkLine(line, LineType.OLD, oldLineNo, -1);
				lines.add(al);
				file.diffStat.deletions++;
				script.diffStat.deletions++;
				oldLineNo++;
				continue;

			case '+':
				old.nAdded++;
				eol = nextLF(buf, c);
				line = RawParseUtils.decode(charset, buf, c + 1, eol - 1);
				al = new HunkLine(line, LineType.NEW, -1, newLineNo);
				lines.add(al);
				file.diffStat.additions++;
				script.diffStat.additions++;
				newLineNo++;
				continue;

			case '\\': // Matches "\ No newline at end of file"
				// no newline can occurred on either side, old line or new line
				//
				if (al != null) {
					al.noNewLine = true;
				}
				continue;

			default:
				break SCAN;
			}
		}

		if (last < end && nContext + old.nDeleted - 1 == old.lineCount
				&& nContext + old.nAdded == newLineCount
				&& match(buf, last, Patch.SIG_FOOTER) >= 0) {
			// This is an extremely common occurrence of "corruption".
			// Users add footers with their signatures after this mark,
			// and git diff adds the git executable version number.
			// Let it slide; the hunk otherwise looked sound.
			//
			old.nDeleted--;
			return last;
		}

		if (nContext + old.nDeleted < old.lineCount) {
			final int missingCount = old.lineCount - (nContext + old.nDeleted);
			script.error(buf, startOffset, MessageFormat.format(
					JGitText.get().truncatedHunkOldLinesMissing,
					Integer.valueOf(missingCount)));

		} else if (nContext + old.nAdded < newLineCount) {
			final int missingCount = newLineCount - (nContext + old.nAdded);
			script.error(buf, startOffset, MessageFormat.format(
					JGitText.get().truncatedHunkNewLinesMissing,
					Integer.valueOf(missingCount)));

		} else if (nContext + old.nDeleted > old.lineCount
				|| nContext + old.nAdded > newLineCount) {
			final String oldcnt = old.lineCount + ":" + newLineCount; //$NON-NLS-1$
			final String newcnt = (nContext + old.nDeleted) + ":" //$NON-NLS-1$
					+ (nContext + old.nAdded);
			script.warn(buf, startOffset, MessageFormat.format(
					JGitText.get().hunkHeaderDoesNotMatchBodyLineCountOf, oldcnt, newcnt));
		}

		return c;
	}

	void extractFileLines(final OutputStream[] out) throws IOException {
		final byte[] buf = file.buf;
		int ptr = startOffset;
		int eol = nextLF(buf, ptr);
		if (endOffset <= eol)
			return;

		// Treat the hunk header as though it were from the ancestor,
		// as it may have a function header appearing after it which
		// was copied out of the ancestor file.
		//
		out[0].write(buf, ptr, eol - ptr);

		SCAN: for (ptr = eol; ptr < endOffset; ptr = eol) {
			eol = nextLF(buf, ptr);
			switch (buf[ptr]) {
			case ' ':
			case '\n':
			case '\\':
				out[0].write(buf, ptr, eol - ptr);
				out[1].write(buf, ptr, eol - ptr);
				break;
			case '-':
				out[0].write(buf, ptr, eol - ptr);
				break;
			case '+':
				out[1].write(buf, ptr, eol - ptr);
				break;
			default:
				break SCAN;
			}
		}
	}

	void extractFileLines(final StringBuilder sb, final String[] text,
			final int[] offsets) {
		final byte[] buf = file.buf;
		int ptr = startOffset;
		int eol = nextLF(buf, ptr);
		if (endOffset <= eol)
			return;
		copyLine(sb, text, offsets, 0);
		SCAN: for (ptr = eol; ptr < endOffset; ptr = eol) {
			eol = nextLF(buf, ptr);
			switch (buf[ptr]) {
			case ' ':
			case '\n':
			case '\\':
				copyLine(sb, text, offsets, 0);
				skipLine(text, offsets, 1);
				break;
			case '-':
				copyLine(sb, text, offsets, 0);
				break;
			case '+':
				copyLine(sb, text, offsets, 1);
				break;
			default:
				break SCAN;
			}
		}
	}

	void copyLine(final StringBuilder sb, final String[] text,
			final int[] offsets, final int fileIdx) {
		final String s = text[fileIdx];
		final int start = offsets[fileIdx];
		int end = s.indexOf('\n', start);
		if (end < 0)
			end = s.length();
		else
			end++;
		sb.append(s, start, end);
		offsets[fileIdx] = end;
	}

	void skipLine(final String[] text, final int[] offsets,
			final int fileIdx) {
		final String s = text[fileIdx];
		final int end = s.indexOf('\n', offsets[fileIdx]);
		offsets[fileIdx] = end < 0 ? s.length() : end + 1;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("HunkHeader[");
		buf.append(getOldImage().getStartLine());
		buf.append(',');
		buf.append(getOldImage().getLineCount());
		buf.append("->");
		buf.append(getNewStartLine()).append(',').append(getNewLineCount());
		buf.append(']');
		return buf.toString();
	}
}
