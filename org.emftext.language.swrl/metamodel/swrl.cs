SYNTAXDEF swrl
FOR <http://www.emftext.org/language/swrl>
START Rule

IMPORTS {
	owl : <http://org.emftext/owl.ecore> <../../org.emftext.language.owl/metamodel/owl.genmodel>
	WITH SYNTAX owl <../../org.emftext.language.owl/metamodel/owl.cs>
}

OPTIONS {
	usePredefinedTokens = "false";
}

RULES {
	Rule ::= antecedent "=>" consequent;
	Antecedent ::= body;
	Consequent ::= body;
	
	DescriptionAtom ::= description[IRI] "(" object ")";
	DataRangeAtom   ::= dataRange[IRI]   "(" object ")";

	// TODO DVariable ::= uri[];
	DLiteral  ::= literal;
	
	IVariable ::= iri[IRI];
}