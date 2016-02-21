grammar FullBranchMatch;

match: criteria+ EOF;

criteria: includeMatch | excludeMatch;

includeMatch: INCLUDE '(' fullBranchMatch ')';
excludeMatch: EXCLUDE '(' fullBranchMatch ')';

fullBranchMatch: depotMatch ':' branchMatch | branchMatch;

depotMatch: DEPOT depotFQN=Value;
branchMatch: BRANCH Value;

INCLUDE: 'include';
EXCLUDE: 'exclude';
DEPOT: 'repository';
BRANCH: 'branch';

Value: '(' (ESCAPE|~[()\\])+? ')';

fragment
ESCAPE: '\\'[()\\];

WS: [ \t\r\n]+ -> skip;