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
import org.mindswap.pellet.PelletOptions;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;

import aterm.ATerm;
import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public class AbstractModelsyncTest {

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
			}

	protected void assertDataPropertyValue(String individualName, String propertyName,
			boolean value) {
				ATermAppl property = findDataPropertyInReasoner(propertyName);
				assertNotNull(property);
				
				ATermAppl individual = findIndividualInReasoner(individualName);
				assertNotNull(individual);
				
				List<ATermAppl> dataPropertyValues = reasoner.getKB().getDataPropertyValues(property, individual);
				System.out.println(individualName + "." + propertyName + " = " + dataPropertyValues);
				assertEquals("Can't find values for data property: " + propertyName, 1, dataPropertyValues.size());
				ATermAppl first = dataPropertyValues.get(0);
				assertEquals(value, first.toString().equals("literal(true,(),http://www.w3.org/2001/XMLSchema#boolean)"));
			}

	protected void assertDataPropertyValue(String individualName, String propertyName,
			String value) {
				ATermAppl property = findDataPropertyInReasoner(propertyName);
				assertNotNull(property);
				
				ATermAppl individual = findIndividualInReasoner(individualName);
				assertNotNull(individual);
				
				List<ATermAppl> dataPropertyValues = reasoner.getKB().getDataPropertyValues(property, individual);
				System.out.println(individualName + "." + propertyName + " = " + dataPropertyValues);
				assertEquals("Can't find values for data property: " + propertyName, 1, dataPropertyValues.size());
				ATermAppl first = dataPropertyValues.get(0);
				ATerm argument = first.getArgument(0);
				assertEquals(value, argument.toString());
			}

	private ATermAppl findDataPropertyInReasoner(String name) {
		Set<ATermAppl> dataProperties = reasoner.getKB().getDataProperties();
		for (ATermAppl dataProperty : dataProperties) {
			if (dataProperty.getName().equals(NS + name)) {
				System.out.println("findDataPropertyInReasoner()" + dataProperty);
				return dataProperty;
			}
		}
		return null;
	}

	private ATermAppl findIndividualInReasoner(String name) {
		Set<ATermAppl> individuals = reasoner.getKB().getIndividuals();
		for (ATermAppl individual : individuals) {
			if (individual.getName().equals(NS + name)) {
				System.out.println("findIndividualInReasoner()" + individual);
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
		System.out.println("ModelsyncTest.createSWRLRule() " + swrlRule);
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
		assertFalse(object + " must not be instance of " + aClass, isInstanceOf(ontology, object, aClass));
	}

	protected void assertIsInstance(OWLOntology ontology, OWLClass aClass, OWLIndividual object) {
		assertTrue(object + " should be instance of " + aClass, isInstanceOf(ontology, object, aClass));
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

	private boolean isInstanceOf(OWLOntology mOnto, OWLIndividual individual, OWLClass aClass) {
		if (reasoner == null) {
			PelletOptions.USE_UNIQUE_NAME_ASSUMPTION = true;
			reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner(mOnto);
			reasoner.getKB().realize();
		}
	
		Set<OWLNamedIndividual> individuals = reasoner.getInstances(aClass, false).getFlattened();
		for (OWLNamedIndividual next : individuals) {
			//System.out.println("Individual: " + next + " is instance of " + aClass.getIRI());
			if (next.equals(individual)) {
				return true;
			}
		}
		return false;
	}

	protected OWLOntology loadOntology(URI uri) {
		String fileString = uri.toFileString();
		IRI iri = IRI.create(new File(fileString));
		try {
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(iri);
			return ontology;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
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
