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
	
	DEFINE BOOLEAN_OPERATOR $('&&'|'||')$;
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
	"library" COLOR #7F0055, BOLD;
}  
 
RULES { 
	PetriNet ::= ("package" pkg[IDENTIFIER] ("." pkg[IDENTIFIER])* ";")? "petrinet" name[IDENTIFIER]?
				("import" ePackages['<','>'] genModels['<','>'] ";")+
				("FUNCTIONS:" "{" functions* "}")?
				"{" (components | arcs)* "}";
				
	Place ::= "place" name[IDENTIFIER]? ":" type[IDENTIFIER]?;
	
	Transition ::= "transition"  name[IDENTIFIER] ( "->" alias[IDENTIFIER] )? ("if" "(" guard ")")? "do" 
				"{" (statements:Statement,Expression ";")*
				"}"; 
				
	BasicFunction ::= library["library": ""]?  "function" type[IDENTIFIER] context[IDENTIFIER]"."name[IDENTIFIER] "(" (parameters ("," parameters)*)? ")";
	ListFunction ::= library["library": ""]? "function" (type[IDENTIFIER] | type[PLIST] "[" returnListType[IDENTIFIER] "]") context[PLIST] "[" listTypeDef "]" "."name[IDENTIFIER] "(" (parameters ("," parameters)*)? ")";
	PGenericType ::= name[IDENTIFIER];
	
	Parameter ::= type[IDENTIFIER] name[IDENTIFIER];
	
	ConsumingArc ::= in[IDENTIFIER] "-consumes->" out[IDENTIFIER] 
		("{"  freeVariable ";" "}");

	FreeVariable ::= name[IDENTIFIER]; 
		
	ProducingArc ::= in[IDENTIFIER] "-produces->" out[IDENTIFIER] 
		("{"  output[IDENTIFIER] ";" "}");	
	
	InitialisedVariable ::= name[IDENTIFIER] "=" initialisation; 
	
	
	@Operator(type="binary_right_associative", weight="1", superclass="Expression")
	BooleanExpression ::= left (operator[BOOLEAN_OPERATOR]) right;

	@Operator(type="primitive", weight="5", superclass="Expression")
	FunctionCall ::= function[IDENTIFIER] "(" ( parameters ("," parameters)*)?")" 
		("." nextExpression)?;
		
	@Operator(type="primitive", weight="5", superclass="Expression")
	VariableCall ::= variable[IDENTIFIER] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	ConstructorCall ::= "new" type[IDENTIFIER]"(" ")";
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	Cast ::= "(" type[IDENTIFIER] ")" expression;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	NestedExpression ::= "(" expression ")"  ("." nextExpression)?;
	
				
	@Operator(type="primitive", weight="5", superclass="Expression")
	StringLiteral ::= value[STRING_LITERAL] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	IntegerLiteral ::= value[DECIMAL_INTEGER_LITERAL] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	FloatLiteral ::= value[DECIMAL_FLOAT_LITERAL] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	DoubleLiteral ::= value[DECIMAL_DOUBLE_LITERAL] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	LongLiteral ::= value[DECIMAL_LONG_LITERAL] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	BooleanLiteral ::= value["true":"false"] ("." nextExpression)?;
	
	@Operator(type="primitive", weight="5", superclass="Expression")
	EClassLiteral ::= "@" clazz[IDENTIFIER] ("." nextExpression)?;
	

}