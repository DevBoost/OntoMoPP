package org.emftext.runtime.owltext;

import org.emftext.language.owl.AbbreviatedXSDStringLiteral;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.FloatingPointLiteral;
import org.emftext.language.owl.IntegerLiteral;
import org.emftext.language.owl.Literal;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.TypedLiteral;

public class LiteralConverter {

	private OwlFactory factory = OwlFactory.eINSTANCE;
	private Datatype xsdBoolean;
	
	public LiteralConverter() {
		xsdBoolean = factory.createDatatype();
		xsdBoolean.setIri("xsd:boolean");
	}
	
	public Literal convert(Object newValue) {
		if (newValue instanceof String) {
			return doConvert((String) newValue);
		} 
		else if (newValue instanceof Integer) {
			return doConvert((Integer) newValue);
		}
		else if (newValue instanceof Float) {
			return doConvert((Float) newValue);
		}
		else if (newValue instanceof Boolean) {
			return doConvert((Boolean) newValue);
		}
		else {
			AbbreviatedXSDStringLiteral textLiteral = factory.createAbbreviatedXSDStringLiteral();
			textLiteral.setValue("The attribute value of type: " + newValue.getClass() + " could not be converted to an literal");
			return textLiteral;
		}
	}
	
	public Literal doConvert(String newValue) {
		AbbreviatedXSDStringLiteral textLiteral = factory.createAbbreviatedXSDStringLiteral();
		textLiteral.setValue(newValue);
		return textLiteral;
	}

	public Literal doConvert(Integer newValue) {
		IntegerLiteral textLiteral = factory.createIntegerLiteral();
		textLiteral.setValue(newValue);
		return textLiteral;
	}
	
	public Literal doConvert(Float newValue) {
		FloatingPointLiteral textLiteral = factory.createFloatingPointLiteral();
		textLiteral.setLiteral(newValue);
		return textLiteral;
	}
	
	public Literal doConvert(Boolean newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		
		textLiteral.setTheDatatype(xsdBoolean);
		return textLiteral;
	}
	
}
