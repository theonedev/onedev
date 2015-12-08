grammar CodeAssistTest1;

selfReference: 'ab' selfReferenceTail;

selfReferenceTail: 'cd' selfReferenceTail|;

mandatories: 'ab' 'c' | 'ab' 'c' | ('cd' ('ef' 'g')) 'h';

notRealAmbiguity: ID+ ID;

ID: [a-zA-Z]+;

WS: [ \t\r\n]+ -> skip;