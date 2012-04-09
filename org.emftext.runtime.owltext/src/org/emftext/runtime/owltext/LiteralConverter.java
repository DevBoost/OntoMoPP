/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.runtime.owltext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private Datatype xsdLong;
	private Datatype xsdShort;
	private Datatype xsdDouble;
	private Datatype xsdByte;
	private Datatype xsdDate;
	private Datatype xsdDecimal;

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

		xsdDecimal = factory.createDatatype();
		xsdDecimal.setIri("xsd:decimal");

		xsdDate = factory.createDatatype();
		xsdDate.setIri("xsd:dateTime");
	}

	public Literal convert(Object newValue) {
		if (newValue == null)
			return null;
		if (newValue instanceof String || newValue instanceof char[]
				|| newValue instanceof Character) {
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
		TypedLiteral textLiteral = factory.createTypedLiteral();
		textLiteral.setLexicalValue(newValue.toString());
		textLiteral.setTheDatatype(xsdDecimal);

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

	@SuppressWarnings("unchecked")
	public <T> T reconvert(Class<T> targetClass, Literal literal) {
		if (literal == null)
			return null;
		if (targetClass.equals(String.class)) {
			return (T) createString(literal);
		}
		if (targetClass.equals(Character.class)
				|| (targetClass.equals(char.class))) {
			return (T) createCharacter(literal);
		}
		if (targetClass.equals(Integer.class)
				|| (targetClass.equals(int.class))) {
			return (T) createInteger(literal);
		}
		if (targetClass.equals(Boolean.class)
				|| (targetClass.equals(boolean.class))) {
			return (T) createBoolean(literal);
		}
		if (targetClass.equals(BigInteger.class)) {
			return (T) createBigInteger(literal);
		}
		if (targetClass.equals(Long.class) || (targetClass.equals(long.class))) {
			return (T) createLong(literal);
		}
		if (targetClass.equals(Short.class)
				|| (targetClass.equals(short.class))) {
			return (T) createShort(literal);
		}
		if (targetClass.equals(BigDecimal.class)) {
			return (T) createBigDecimal(literal);
		}
		if (targetClass.equals(Float.class)
				|| (targetClass.equals(float.class))) {
			return (T) createFloat(literal);
		}
		if (targetClass.equals(Double.class)
				|| (targetClass.equals(double.class))) {
			return (T) createDouble(literal);
		}
		if (targetClass.equals(Byte.class) || (targetClass.equals(byte.class))) {
			return (T) createByte(literal);
		}
		if (targetClass.equals(Date.class)) {
			return (T) createDate(literal);
		}
		throw new RuntimeException(
				"Conversion error. Requested datatype not supported: "
						+ targetClass);
	}

	private Character createCharacter(Literal literal) {
		AbbreviatedXSDStringLiteral textLiteral = (AbbreviatedXSDStringLiteral) literal;
		return new Character(textLiteral.getValue().charAt(0));
	}

	private String createString(Literal literal) {
		AbbreviatedXSDStringLiteral textLiteral = (AbbreviatedXSDStringLiteral) literal;
		return new String(textLiteral.getValue());
	}

	private Date createDate(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		try {
			return new SimpleDateFormat().parse(tl.getLexicalValue());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Byte createByte(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		return new Byte(tl.getLexicalValue());
	}

	private Double createDouble(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		return new Double(tl.getLexicalValue());
	}

	private BigDecimal createBigDecimal(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		return new BigDecimal(tl.getLexicalValue());
	}

	private Short createShort(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		return new Short(tl.getLexicalValue());
	}

	private Boolean createBoolean(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		return new Boolean(tl.getLexicalValue());
	}

	private Long createLong(Literal literal) {
		TypedLiteral tl = (TypedLiteral) literal;
		return new Long(tl.getLexicalValue());
	}

	private Float createFloat(Literal literal) {
		FloatingPointLiteral fpl = (FloatingPointLiteral) literal;
		return new Float(fpl.getLiteral());
	}

	private BigInteger createBigInteger(Literal literal) {
		IntegerLiteral il = (IntegerLiteral) literal;
		return new BigInteger("" + il.getValue());
	}

	private Integer createInteger(Literal literal) {
		IntegerLiteral il = (IntegerLiteral) literal;
		return new Integer(il.getValue());

	}

}
