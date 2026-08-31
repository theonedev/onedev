# Text Diff Expander Test Plan

## Application Overview

Text diffs collapse unchanged lines around changes. A middle collapsed region offers independent up, both, and down expansion, while file-edge regions keep one applicable control.

## Test Scenarios

### 1. Commit Diff Expansion

**Seed:** `tests/text-diff-expander.spec.js` logs in as administrator, creates a project, and pushes a two-commit fixture with separated changes.

#### 1.1. expand-a-middle-gap-by-direction

**File:** `tests/text-diff-expander.spec.js`

**Steps:**

1. Open the fixture commit detail.
   - expect: file-start and file-end gaps have one control.
   - expect: the middle gap has three equal-width up, both, and down controls.
   - expect: the skipped-line count appears on the middle control.
2. Expand upward.
   - expect: lines adjacent to the preceding hunk appear while lines adjacent to the following hunk do not change.
3. Reload and expand downward.
   - expect: lines adjacent to the following hunk appear while lines adjacent to the preceding hunk do not change.
4. Reload and expand both ways.
   - expect: context appears on both sides of the hidden gap.
5. Repeatedly expand upward until that direction is exhausted.
   - expect: the row becomes one control for the remaining direction.
6. Repeatedly activate the remaining control.
   - expect: the expander disappears when all unchanged lines are visible.

