grammar RefMatch;

match: criteria+ EOF;

criteria: branchMatch | tagMatch | patternMatch | excludeBranchMatch | excludeTagMatch | excludePatternMatch;

branchMatch: BRANCH Value;
tagMatch: TAG Value;
patternMatch: PATTERN Value;
excludeBranchMatch: EXCLUDE_BRANCH Value;
excludeTagMatch: EXCLUDE_TAG Value;
excludePatternMatch: EXCLUDE_PATTERN Value;

BRANCH: 'branch';
TAG: 'tag';
PATTERN: 'pattern';
EXCLUDE_BRANCH: 'excludeBranch';
EXCLUDE_TAG: 'excludeTag';
EXCLUDE_PATTERN: 'excludePattern';

Value: '(' (ESCAPE|~[()\\])+? ')';

fragment
ESCAPE: '\\'[()\\];

WS: [ \t\r\n]+ -> skip;