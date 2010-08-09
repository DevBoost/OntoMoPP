package org.emftext.runtime.owltext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

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
	private Datatype xsdLong;
	private Datatype xsdShort;
	private Datatype xsdDouble;
	private Datatype xsdByte;
	private Datatype xsdDate;

	public LiteralConverter() {
		xsdBoolean = factory.createDatatype();
		xsdBoolean.setIri("xsd:boolean");
		
		xsdLong = factory.createDatatype();
		xsdLong.setIri("xsd:long");
		
		xsdShort = factory.createDatatype();
		xsdShort.setIri("xsd:short");
		
		xsdDouble = factory.createDatatype();
		xsdDouble.setIri("xsd:double");
		
		xsdByte = factory.createDatatype();
		xsdByte.setIri("xsd:byte");
		
		xsdDate = factory.createDatatype();
		xsdDate.setIri("xsd:dateTime");
	}

	public Literal convert(Object newValue) {
		if (newValue == null)
			return null;
		if (newValue instanceof String || newValue instanceof char[] || newValue instanceof Character) {
			return doConvert(newValue.toString());
		} else if (newValue instanceof Integer) {
			return doConvert((Integer) newValue);
		} else if (newValue instanceof BigInteger) {
			return doConvert((BigInteger) newValue);
		} else if (newValue instanceof Long) {
			return doConvert((Long) newValue);
		} else if (newValue instanceof Short) {
			return doConvert((Short) newValue);
		} else if (newValue instanceof BigDecimal) {
			return doConvert((BigDecimal) newValue);
		} else if (newValue instanceof Float) {
			return doConvert((Float) newValue);
		} else if (newValue instanceof Double) {
			return doConvert((Double) newValue);
		} else if (newValue instanceof Boolean) {
			return doConvert((Boolean) newValue);
		} else if (newValue instanceof Byte) {
			return doConvert((Byte) newValue);
		} else if (newValue instanceof Date) {
			return doConvert((Date) newValue);
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
	
	public Literal doConvert(BigInteger newValue) {
		IntegerLiteral textLiteral = factory.createIntegerLiteral();
		textLiteral.setValue(newValue.intValue());
		
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
	
	public Literal doConvert(Long newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdLong);
		return textLiteral;
	}
	
	public Literal doConvert(Short newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdShort);
		return textLiteral;
	}
	
	public Literal doConvert(Double newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdDouble);
		return textLiteral;
	}
	
	public Literal doConvert(Byte newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdByte);
		return textLiteral;
	}
	
	public Literal doConvert(Date newValue) {
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdDate);
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
		} else if (newValue instanceof TypedLiteral){
			if(((TypedLiteral)newValue).getTheDatatype().equals(xsdBoolean)) 
				return doConvertBoolean((TypedLiteral) newValue);
			else if(((TypedLiteral)newValue).getTheDatatype().equals(xsdLong))
				return doConvertLong((TypedLiteral) newValue);
			else if(((TypedLiteral)newValue).getTheDatatype().equals(xsdShort))
				return doConvertShort((TypedLiteral) newValue);
			else if(((TypedLiteral)newValue).getTheDatatype().equals(xsdDouble))
				return doConvertDouble((TypedLiteral) newValue);
			else if(((TypedLiteral)newValue).getTheDatatype().equals(xsdByte))
				return doConvertByte((TypedLiteral) newValue);
			//else if(((TypedLiteral)newValue).getTheDatatype().equals(xsdDate))
				//return doConvertDate((TypedLiteral) newValue);
		}
		
		return "The attribute value of type: "
				+ newValue.getClass()
				+ " could not be converted to a representation";
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

	public Boolean doConvertBoolean(TypedLiteral newValue) {
		return Boolean.parseBoolean(newValue.getLexicalValue());
	}
	public Long doConvertLong(TypedLiteral newValue) {
		return Long.parseLong(newValue.getLexicalValue());
	}
	public Short doConvertShort(TypedLiteral newValue) {
		return Short.parseShort(newValue.getLexicalValue());
	}
	public Double doConvertDouble(TypedLiteral newValue) {
		return Double.parseDouble(newValue.getLexicalValue());
	}
	public Byte doConvertByte(TypedLiteral newValue) {
		return Byte.parseByte(newValue.getLexicalValue());
	}
	//public Date doConvertDate(TypedLiteral newValue) {
	//	return Date.parse(newValue.getLexicalValue());
	//}
}
