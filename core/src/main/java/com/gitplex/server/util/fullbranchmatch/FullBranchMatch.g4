grammar FullBranchMatch;

match: ' '* criteria (' '+ criteria)* ' '* EOF;

criteria: includeMatch | excludeMatch;

includeMatch: INCLUDE '(' fullBranchMatch ')';
excludeMatch: EXCLUDE '(' fullBranchMatch ')';

fullBranchMatch: branchMatch | fullDepotMatch ':' branchMatch;

fullDepotMatch: Value;
branchMatch: Value;

INCLUDE: 'include';
EXCLUDE: 'exclude';

Value: (ESCAPE|~[:() \\])+;

fragment
ESCAPE: '\\'[:()\\];