SYNTAXDEF swrl
FOR <http://www.emftext.org/language/swrl>
START SWRLDocument

//IMPORTS {
//	owl : <http://org.emftext/owl.ecore> <../../org.emftext.language.owl/metamodel/owl.genmodel>
//	WITH SYNTAX owl <../../org.emftext.language.owl/metamodel/owl.cs>
//}

OPTIONS {
	usePredefinedTokens = "false";
}

TOKENS {
	DEFINE WHITESPACE $(' '|'\t'|'\f')$;
	DEFINE LINEBREAKS $('\r\n'|'\r'|'\n')$;
	DEFINE IRI $(('<')(~('>')|('\\''>'))*('>'))|(('A'..'Z' | ':' | 'a'..'z' | '0'..'9' | '_' | '-' )+)$;
}

RULES {
	SWRLDocument ::=
		("import" imports[IRI])+
		rules+;

	Rule ::= antecedent "=>" consequent;
	Antecedent ::= body;
	Consequent ::= body;
	
	DescriptionAtom ::= description[IRI] "(" object ")";
	//DataRangeAtom   ::= dataRange[IRI]   "(" object ")";

	// TODO DVariable ::= uri[];
	//DLiteral  ::= literal;
	
	IVariable ::= iri[IRI];
}