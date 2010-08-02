package org.emftext.runtime.owltext;

import java.math.BigDecimal;

import org.emftext.language.owl.AbbreviatedXSDStringLiteral;
import org.emftext.language.owl.Datatype;
import org.emftext.language.owl.DecimalLiteral;
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
		if (newValue == null)
			return null;
		if (newValue instanceof String || newValue instanceof char[] || newValue instanceof Character) {
			return doConvert(newValue.toString());
		} else if (newValue instanceof Integer) {
			return doConvert((Integer) newValue);
		} else if (newValue instanceof Float) {
			return doConvert((Float) newValue);
		} else if (newValue instanceof BigDecimal) {
			return doConvert((BigDecimal) newValue);
		} else if (newValue instanceof Boolean) {
			return doConvert((Boolean) newValue);
		} else {
			AbbreviatedXSDStringLiteral textLiteral = factory
					.createAbbreviatedXSDStringLiteral();
			textLiteral.setValue("The attribute value of type: "
					+ newValue.getClass()
					+ " could not be converted to a literal");
			return textLiteral;
		}
	}

	public Literal doConvert(String newValue) {
		AbbreviatedXSDStringLiteral textLiteral = factory
				.createAbbreviatedXSDStringLiteral();
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
	
	public Literal doConvert(BigDecimal newValue) {
		DecimalLiteral textLiteral = factory.createDecimalLiteral();
		textLiteral.setValue(newValue);
		
		return textLiteral;
	}

	public Literal doConvert(Boolean newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdBoolean);
		return textLiteral;
	}
	
	public Object convert(Literal newValue) {
		if (newValue == null)
			return null;
		if (newValue instanceof AbbreviatedXSDStringLiteral) {
			return doConvert((AbbreviatedXSDStringLiteral) newValue);
		} else if (newValue instanceof IntegerLiteral) {
			return doConvert((IntegerLiteral) newValue);
		} else if (newValue instanceof FloatingPointLiteral) {
			return doConvert((FloatingPointLiteral) newValue);
		} else if ((newValue instanceof TypedLiteral) && ((TypedLiteral)newValue).getTheDatatype().equals(xsdBoolean)) {
			return doConvert((TypedLiteral) newValue);
		} else {
			return "The attribute value of type: "
					+ newValue.getClass()
					+ " could not be converted to a representation";
		}
	}
	
	public String doConvert(AbbreviatedXSDStringLiteral newValue) {
		return newValue.getValue();
	}

	public Integer doConvert(IntegerLiteral newValue) {
		return newValue.getValue();
	}

	public Float doConvert(FloatingPointLiteral newValue) {
		return newValue.getLiteral();
	}
	
	public BigDecimal doConvert(DecimalLiteral newValue) {
		return newValue.getValue();
	}

	public Boolean doConvert(TypedLiteral newValue) {
		return Boolean.parseBoolean(newValue.getLexicalValue());
	}
}
