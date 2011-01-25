SYNTAXDEF petrinets
FOR <http://www.emftext.org/language/petrinets>
START PetriNet

OPTIONS {
	overrideBuilder = "false";	
	reloadGeneratorModel = "true";
	usePredefinedTokens = "false";
}

TOKENS {
 	DEFINE SL_COMMENT $'//'(~('\n'|'\r'|'\uffff'))* $;
	DEFINE ML_COMMENT $'/*'.*'*/'$;
	DEFINE PLIST $'PList'$;
	DEFINE IDENTIFIER $('A'..'Z' | 'a'..'z'| '_' | '-' )('A'..'Z' | 'a'..'z' | '0'..'9' | '_' | '-' | '::')*$;
	
	DEFINE WHITESPACE $(' '|'\t'|'\f')$;
	DEFINE LINEBREAKS $('\r\n'|'\r'|'\n')$;
	
	
	DEFINE STRING_LITERAL $'"'('\\'('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')|('\\''u'('0'..'9'|'a'..'f'|'A'..'F')('0'..'9'|'a'..'f'|'A'..'F')('0'..'9'|'a'..'f'|'A'..'F')('0'..'9'|'a'..'f'|'A'..'F'))|'\\'('0'..'7')|~('\\'|'"'))*'"'$;
	DEFINE DECIMAL_FLOAT_LITERAL $('0'..'9')+ '.' ('0'..'9')* (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+)? ('f'|'F') | ('.' ('0'..'9')+ (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+)?) ('f'|'F') | (('0'..'9')+ (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+) ('f'|'F') | ('0'..'9')+ ('f'|'F'))$;
	DEFINE DECIMAL_DOUBLE_LITERAL $('0'..'9')+ '.' ('0'..'9')* (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+)? ('d'|'D')? | ('.' ('0'..'9')+ (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+)?) ('d'|'D')? | (('0'..'9')+ (('e'|'E'|'p'|'P') ('+'|'-')? ('0'..'9')+) ('d'|'D')? | ('0'..'9')+ ('d'|'D'))$;
	DEFINE DECIMAL_LONG_LITERAL $('0'|'1'..'9''0'..'9'*)('l'|'L')$;
	DEFINE DECIMAL_INTEGER_LITERAL $('0'|'1'..'9''0'..'9'*)$;
	
}

TOKENSTYLES {
	"ML_COMMENT" COLOR #008000, ITALIC;
	"SL_COMMENT" COLOR #000080, ITALIC;
}  
 
RULES { 
	PetriNet ::= ("package" pkg[IDENTIFIER] ("." pkg[IDENTIFIER])* ";")? "petrinet" name[STRING_LITERAL]?
				("import" ePackages['<','>'] genModels['<','>'] ";")+
				("FUNCTIONS:" "{" functions* "}")?
				"{" (components | arcs)* "}";
				
	BasicFunction ::= "function" type[IDENTIFIER] context[IDENTIFIER]"."name[IDENTIFIER] "(" (parameters ("," parameters)*)? ")";
	ListFunction ::= "function" (type[IDENTIFIER] | type[PLIST] "[" returnListType[IDENTIFIER] "]") context[PLIST] "[" listTypeDef "]" "."name[IDENTIFIER] "(" (parameters ("," parameters)*)? ")";
	PGenericType ::= name[IDENTIFIER];
	
	Parameter ::= type[IDENTIFIER] name[IDENTIFIER];
	
	Arc ::= in[STRING_LITERAL] "->" out[STRING_LITERAL] 
		("{" ( arcStatements* ";"
		)* "}")*;
		
	Variable ::= name[IDENTIFIER] "=" initialisation; 
	
	FunctionCall ::= function[IDENTIFIER] "(" ( parameters ("," parameters)*)?")" 
		("." nextExpression)?;
	VariableCall ::= variable[IDENTIFIER] ("." nextExpression)?;
	ConstructorCall ::= "new" type[IDENTIFIER]"(" ")";
	
	ConsumingArc ::= in[STRING_LITERAL] "-consume->" out[STRING_LITERAL];
 	ProducingArc ::= in[STRING_LITERAL] "-produce->" out[STRING_LITERAL];	
	
	Place ::= "place" name[STRING_LITERAL]? ":" type[IDENTIFIER];
	Transition ::= "transition" name[STRING_LITERAL]?; 
	
	StringLiteral ::= value[STRING_LITERAL] ("." nextExpression)?;
	IntegerLiteral ::= value[DECIMAL_INTEGER_LITERAL] ("." nextExpression)?;
	FloatLiteral ::= value[DECIMAL_FLOAT_LITERAL] ("." nextExpression)?;
	DoubleLiteral ::= value[DECIMAL_DOUBLE_LITERAL] ("." nextExpression)?;
	LongLiteral ::= value[DECIMAL_LONG_LITERAL] ("." nextExpression)?;
	BooleanLiteral ::= value["true":"false"] ("." nextExpression)?;

}