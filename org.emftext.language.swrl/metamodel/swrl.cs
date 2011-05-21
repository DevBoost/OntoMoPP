SYNTAXDEF swrl
FOR <http://www.emftext.org/language/swrl>
START SWRLDocument

//IMPORTS {
//	owl : <http://org.emftext/owl.ecore> <../../org.emftext.language.owl/metamodel/owl.genmodel>
//	WITH SYNTAX owl <../../org.emftext.language.owl/metamodel/owl.cs>
//}

OPTIONS {
	usePredefinedTokens = "false";
	disableLaunchSupport = "true";
	disableDebugSupport = "true";
}

TOKENS {
	DEFINE COMMENTS $'//'(~('\n'|'\r'))*$;
	DEFINE WHITESPACE $(' '|'\t'|'\f')$;
	DEFINE LINEBREAKS $('\r\n'|'\r'|'\n')$;
	// copied from OWL syntax
	DEFINE IRI $(('<')(~('>')|('\\''>'))*('>'))|(('A'..'Z' | ':' | 'a'..'z' | '0'..'9' | '_' | '-' )+)$;
}

TOKENSTYLES {
	"COMMENTS" COLOR #008000;
}

RULES {
	SWRLDocument ::=
		("import" imports[IRI])+
		rules+;

	Rule ::= antecedent "=>" consequent;

	Antecedent ::= body ("and" body)*;
	Consequent ::= body ("and" body)*;
	
	DescriptionAtom ::= description "(" object ")";
	PropertyAtom ::= property[IRI] "(" source "," target ")";
	//DataRangeAtom   ::= dataRange[IRI]   "(" object ")";
	DifferentFromAtom ::= "differentFrom" "(" objectA "," objectB ")";
	SameAsAtom        ::= "sameAs" "(" objectA "," objectB ")";

	// TODO DVariable ::= uri[];
	DLiteral  ::= literal;
	
	IVariable ::= "?" iri[IRI];
	DVariable ::= "?" iri[IRI];

	// adapted from OWL syntax
	ClassAtomic ::= not["not" : ""] clazz[IRI];
	//NestedDescription ::= not["not" : ""] description : ClassAtomic;
	
	BooleanLiteral ::= value["true":"false"];
}