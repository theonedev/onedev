grammar FullBranchMatch;

match: criteria+ EOF;

criteria: includeMatch | excludeMatch;

includeMatch: INCLUDE '(' fullBranchMatch ')';
excludeMatch: EXCLUDE '(' fullBranchMatch ')';

fullBranchMatch: accountMatch ':' depotMatch ':' branchMatch | depotMatch ':' branchMatch | branchMatch;

accountMatch: ACCOUNT Value;
depotMatch: REPOSITORY Value;
branchMatch: BRANCH Value;

INCLUDE: 'include';
EXCLUDE: 'exclude';
ACCOUNT: 'account';
REPOSITORY: 'repository';
BRANCH: 'branch';

Value: '(' (ESCAPE|~[()\\])+? ')';

fragment
ESCAPE: '\\'[()\\];

WS: [ \t\r\n]+ -> skip;