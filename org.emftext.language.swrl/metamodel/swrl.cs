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
	IndividualPropertyAtom ::= property[IRI] "(" source "," target ")";
	
	//DataRangeAtom   ::= dataRange[IRI]   "(" object ")";

	// TODO DVariable ::= uri[];
	//DLiteral  ::= literal;
	
	IVariable ::= "?" iri[IRI];
	DVariable ::= "?" iri[IRI];

	// adapted from OWL syntax
	//@Operator(type = "primitive", weight="2", superclass="Description")
	ClassAtomic ::= not["not" : ""] clazz[IRI];

	//@Operator(type = "binary_left_associative", weight="1", superclass="Description")
	//Conjunction ::= primaries "and" primaries;
}