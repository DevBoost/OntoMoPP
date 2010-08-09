SYNTAXDEF fea
FOR <http://org.owltext/feature>
START MandatoryFeature

OPTIONS {
	resourcePluginID="org.emftext.runtime.owltext.test";
	resourceUIPluginID="org.emftext.runtime.owltext.test";
	overrideManifest="false";
	overrideUIManifest="false";
}
TOKENS{
	DEFINE COMMENT$'//'(~('\n'|'\r'|'\uffff'))*$;
	DEFINE OPERATOR$'+'|'-'|'*'|'/'$;
	
	//DEFINE INTEGER$('-')?('1'..'9')('0'..'9')*|'0'$;
	//DEFINE FLOAT$('-')?(('1'..'9') ('0'..'9')* | '0') '.' ('0'..'9')+ $;
	
}

TOKENSTYLES{
	"+Feature" COLOR #7F0055, BOLD;
	"-Feature" COLOR #7F0055, BOLD;
}

RULES{
	
	MandatoryFeature ::= "+Feature"   name['"','"'] annotation? 
		comments['<','>']* anyLiterals*   ("{" children* "}")?; // operator[OPERATOR]?
	
	OptionalFeature ::= "-Feature"   name['"','"'] 
		("{" children* "}")? 
		;
	
	Annotation ::=	value['[',']'];
	
	AnyInt ::= literal['$','$'];
	AnyBigInteger ::= literal['$b','$b'];
	AnyLong ::= literal['$l','$l'];
	AnyShort ::= literal['$s','$s'];
	AnyBigDecimal ::= literal['$d','$d'];
	AnyFloat ::= literal['%','%'];
	AnyDouble ::= literal['%d','%d'];
	AnyBoolean ::= literal['&','&'];
	AnyChar ::= literal['c','c'];
	AnyByte ::= literal['b','b'];
	//AnyDate ::= literal['_','_'];	
}