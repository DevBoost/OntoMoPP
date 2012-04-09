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
package org.emftext.ontomopp.modelsync.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;

import aterm.ATerm;
import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public abstract class AbstractModelsyncTestCase {

	public static final String NS = "test://mapping#";
	protected OWLOntologyManager manager;
	protected OWLDataFactory factory;
	protected PelletReasoner reasoner;

	protected SWRLDArgument createConstant(String value) {
		return factory.getSWRLLiteralArgument(factory.getOWLStringLiteral(value));
	}

	protected SWRLDArgument createConstant(int value) {
		return factory.getSWRLLiteralArgument(factory.getOWLTypedLiteral(value));
	}

	protected void setObjectProperty(OWLOntology ontology, OWLIndividual source, OWLObjectProperty property,
			OWLIndividual target) {
		OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, source, target);
		manager.addAxiom(ontology, axiom);
		clearReasoner();
	}

	protected void assertDataPropertyValue(OWLOntology ontology, String individualName, String propertyName,
			boolean value) {
		ATermAppl property = findDataPropertyInReasoner(ontology, propertyName);
		assertNotNull("Can't find data property " + propertyName, property);
		
		ATermAppl individual = findIndividualInReasoner(ontology, individualName);
		assertNotNull(individual);
		
		List<ATermAppl> dataPropertyValues = reasoner.getKB().getDataPropertyValues(property, individual);
		//System.out.println(individualName + "." + propertyName + " = " + dataPropertyValues);
		assertEquals("Can't find values for data property: " + propertyName, 1, dataPropertyValues.size());
		ATermAppl first = dataPropertyValues.get(0);
		assertEquals("Data property " + individualName + "." + propertyName + " must be " + value, value, first.toString().equals("literal(true,(),http://www.w3.org/2001/XMLSchema#boolean)"));
	}

	protected void assertObjectPropertyValue(
			OWLOntology ontology, 
			String individualName, 
			String propertyName,
			String targetName) {

		boolean foundTarget = hasObjectPropertyValue(ontology, individualName,
				propertyName, targetName);
		if (!foundTarget) {
			fail("Can't find target '" + targetName + "' for object property: " + propertyName);
		}
	}

	protected boolean hasObjectPropertyValue(OWLOntology ontology,
			String individualName, String propertyName, String targetName) {
		ATermAppl individual = findIndividualInReasoner(ontology, individualName);
		ATermAppl target = findIndividualInReasoner(ontology, targetName);
		ATermAppl property = findObjectPropertyInReasoner(propertyName);
		List<ATermAppl> objectPropertyValues = reasoner.getKB().getObjectPropertyValues(property, individual);
		//System.out.println(individualName + "." + propertyName + " = " + objectPropertyValues);
		boolean foundTarget = false;
		for (ATermAppl aTermAppl : objectPropertyValues) {
			if (aTermAppl.equals(target)) {
				foundTarget = true;
			}
		}
		return foundTarget;
	}

	protected void assertDataPropertyValue(OWLOntology ontology, String individualName, String propertyName,
			String value) {
				ATermAppl property = findDataPropertyInReasoner(ontology, propertyName);
				assertNotNull(property);
				
				ATermAppl individual = findIndividualInReasoner(ontology, individualName);
				assertNotNull(individual);
				
				List<ATermAppl> dataPropertyValues = reasoner.getKB().getDataPropertyValues(property, individual);
				//System.out.println(individualName + "." + propertyName + " = " + dataPropertyValues);
				assertEquals("Can't find values for data property: " + propertyName, 1, dataPropertyValues.size());
				ATermAppl first = dataPropertyValues.get(0);
				ATerm argument = first.getArgument(0);
				assertEquals(value, argument.toString());
			}

	private ATermAppl findDataPropertyInReasoner(OWLOntology ontology, String name) {
		Set<ATermAppl> dataProperties = getReasoner(ontology).getKB().getDataProperties();
		for (ATermAppl dataProperty : dataProperties) {
			if (dataProperty.getName().equals(NS + name)) {
				//System.out.println("findDataPropertyInReasoner()" + dataProperty);
				return dataProperty;
			}
		}
		return null;
	}

	private ATermAppl findObjectPropertyInReasoner(String name) {
		Set<ATermAppl> objectProperties = reasoner.getKB().getObjectProperties();
		for (ATermAppl objectProperty : objectProperties) {
			if (objectProperty.getName().equals(NS + name)) {
				//System.out.println("findObjectPropertyInReasoner(" + name + ") : " + objectProperty);
				return objectProperty;
			}
		}
		//System.out.println("findObjectPropertyInReasoner(" + name + ") : null");
		return null;
	}

	private ATermAppl findIndividualInReasoner(OWLOntology ontology, String name) {
		Set<ATermAppl> individuals = getReasoner(ontology).getKB().getIndividuals();
		for (ATermAppl individual : individuals) {
			if (individual.getName().equals(NS + name)) {
				//System.out.println("findIndividualInReasoner()" + individual);
				return individual;
			}
		}
		return null;
	}

	protected void setDataProperty(OWLOntology ontology, OWLIndividual individual, String name,
			boolean value) {
				OWLDataPropertyExpression property = findDataProperty(name);
				OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(property, individual, value);
				manager.addAxiom(ontology, axiom);
			}

	protected void setDataProperty(OWLOntology ontology, OWLIndividual individual, String name,
			String value) {
				OWLDataPropertyExpression property = findDataProperty(name);
				OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(property, individual, value);
				manager.addAxiom(ontology, axiom);
			}

	protected SWRLRule createSWRLRule(OWLOntology ontology, List<SWRLAtom> headAtoms, List<SWRLAtom> bodyAtoms) {
		
		Set<SWRLAtom> body = new LinkedHashSet<SWRLAtom>();
		body.addAll(bodyAtoms);
		Set<SWRLAtom> head = new LinkedHashSet<SWRLAtom>();
		head.addAll(headAtoms);
		// Rule( antecedent(From(?x)) consequent(To(?x)) )
		//SWRLBuiltInsVocabulary.
	
		SWRLRule swrlRule = factory.getSWRLRule(body, head);
		manager.addAxiom(ontology, swrlRule);
		//System.out.println("ModelsyncTest.createSWRLRule() " + swrlRule);
        clearReasoner();
		return swrlRule;
	}

	protected SWRLAtom createSWRLClassAtom(OWLClass owlClass, String variableName) {
		SWRLIArgument var = createVariable(variableName);
		return factory.getSWRLClassAtom(owlClass, var);
	}

	protected SWRLAtom createSWRLObjectPropertyAtom(OWLObjectProperty property, String variable1,
			String variable2) {
				SWRLIArgument var1 = createVariable(variable1);
				SWRLIArgument var2 = createVariable(variable2);
				return factory.getSWRLObjectPropertyAtom(property, var1, var2);
			}

	protected SWRLAtom createSWRLDataPropertyAtom(OWLDataProperty property, String variable1,
			String variable2) {
				SWRLIArgument var1 = createVariable(variable1);
				SWRLDArgument var2 = createVariable(variable2);
				return factory.getSWRLDataPropertyAtom(property, var1, var2);
			}

	protected SWRLVariable createVariable(String name) {
		return factory.getSWRLVariable(IRI.create(NS + name));
	}

	protected void clearReasoner() {
		reasoner = null;
	}

	protected void assertNotIsInstance(OWLOntology ontology, OWLClass aClass, OWLIndividual object) {
		assertFalse(object + " must not be instance of " + aClass, isInstanceOf(ontology, aClass, object));
	}

	protected void assertIsInstance(OWLOntology ontology, OWLClass aClass, OWLIndividual object) {
		assertTrue(object + " should be instance of " + aClass, isInstanceOf(ontology, aClass, object));
	}

	protected OWLIndividual addIndividual(OWLOntology ontology, OWLClass aClass, String name) {
		OWLIndividual individual = factory.getOWLNamedIndividual(IRI.create(NS + name));
	    OWLAxiom axiomN = factory.getOWLClassAssertionAxiom(aClass, individual);
	    manager.addAxiom(ontology, axiomN);
	    clearReasoner();
		return individual;
	}

	protected OWLClass findClass(String className) {
		return findClass(NS, className);
	}

	private OWLClass findClass(String namespace, String className) {
		OWLClass foundClass = factory.getOWLClass(IRI.create(namespace + className));
		return foundClass;
	}

	protected OWLDataProperty findDataProperty(String name) {
		OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(NS + name));
		return dataProperty;
	}

	protected OWLObjectProperty findObjectProperty(String name) {
		OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(NS + name));
		return objectProperty;
	}

	protected boolean isInstanceOf(OWLOntology mOnto, OWLClass aClass, OWLIndividual individual) {
		getReasoner(mOnto);
	
		Set<OWLNamedIndividual> individuals = reasoner.getInstances(aClass, false).getFlattened();
		for (OWLNamedIndividual next : individuals) {
			//System.out.println("Individual: " + next + " is instance of " + aClass.getIRI());
			if (next.equals(individual)) {
				return true;
			}
		}
		return false;
	}

	protected PelletReasoner getReasoner(OWLOntology ontology) {
		if (reasoner == null) {
			reasoner = new OWLTestHelper().createReasoner(ontology);
		}
		return reasoner;
	}

	protected OWLOntology loadOntology(URI uri) {
		return new OWLTestHelper().loadOntology(manager, uri);
	}

	protected URI getInputModelURI(String testcaseName, String modelName, String extension) {
		File inputFolder = new File("input" + File.separator + testcaseName);
		URI uri = URI.createFileURI(inputFolder.getAbsolutePath() + File.separator + modelName + "." + extension);
		return uri;
	}

	protected OWLOntology loadOntology(String testcaseName) {
		OWLOntology mOnto = loadOntology(getInputModelURI(testcaseName, "mapping", "owl"));
		return mOnto;
	}

}
