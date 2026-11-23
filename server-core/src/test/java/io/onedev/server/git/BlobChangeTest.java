package io.onedev.server.git;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.junit.jupiter.api.Test;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.model.Project;
import io.onedev.server.util.diff.WhitespaceOption;

class BlobChangeTest {

	@Test
	void rejectsOldRangesOnWhitespaceChangedLines() {
		var change = change("header\n\treturn value;", "inserted\nheader\n    return value;");
		assertTrue(change.displaysOriginalTextForOldRange(new PlanarRange(0, 0, 0, 6)));
		assertFalse(change.displaysOriginalTextForOldRange(new PlanarRange(1, 1, 1, 7)));
	}

	@Test
	void rejectsMultilineRangesWhenAnyCoveredLineDiffers() {
		var change = change("first\n  middle\nlast", "first\nmiddle\nlast");
		assertFalse(change.displaysOriginalTextForOldRange(new PlanarRange(0, 0, 2, 4)));
	}

	@Test
	void acceptsOldRangesOnDeletedLines() {
		var change = change("first\nold\nlast", "first\nnew\nlast");
		assertTrue(change.displaysOriginalTextForOldRange(new PlanarRange(0, 0, 2, 4)));
	}

	private BlobChange change(String oldContent, String newContent) {
		return new BlobChange(ChangeType.MODIFY, new BlobIdent("old", "file"), new BlobIdent("new", "file"),
				WhitespaceOption.IGNORE_ALL) {
			@Override
			public Blob.Text getOldText() {
				return new Blob.Text(UTF_8, oldContent);
			}

			@Override
			public Blob.Text getNewText() {
				return new Blob.Text(UTF_8, newContent);
			}

			@Override
			public Project getProject() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
