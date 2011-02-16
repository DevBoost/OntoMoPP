package org.emftext.ontomopp.modelsync.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mindswap.pellet.utils.Pair;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;

import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

/**
 * A test case for the ontology-based model synchronization algorithm that was
 * developed by Federico Rieckhof.
 */
public class ModelsyncTest {

    public final static String NS = "test://mapping#";

	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private PelletReasoner reasoner;
	
	@Test
	public void testRenaming() {
		String testcaseName = "renaming";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass leftClass = findClass("NamedElement");
		OWLClass rightClass = findClass("Nameable");
		OWLClass otherClass = findClass("OtherClass");
		OWLClass leftRightClass = findClass("MName");
		
        // add an instance of LeftClass and check whether it is recognized as instance
		// of RightClass too
        OWLIndividual leftObject = addIndividual(mOnto, leftClass, "left");
		assertIsInstance(mOnto, leftRightClass, leftObject);
		assertIsInstance(mOnto, leftClass, leftObject);
		assertIsInstance(mOnto, rightClass, leftObject);
		assertNotIsInstance(mOnto, otherClass, leftObject);
		
		// add an instance of RightClass and check whether it is recognized as instance
		// of LeftClass too
		clearReasoner();
        OWLIndividual rightObject = addIndividual(mOnto, rightClass, "right");
		assertIsInstance(mOnto, leftRightClass, rightObject);
		assertIsInstance(mOnto, leftClass, rightObject);
		assertIsInstance(mOnto, rightClass, rightObject);
		assertNotIsInstance(mOnto, otherClass, rightObject);
	}

	@Test
	public void testCrossDistribution() {
		String testcaseName = "cross-distribution";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass methodClass = findClass("Method");
		OWLClass fieldClass = findClass("Field");
		OWLClass basicEntryClass = findClass("BasicEntry");
		OWLClass fullEntryClass = findClass("FullEntry");

		// add abstract method1
        OWLIndividual method1Object = addIndividual(mOnto, methodClass, "method1");
		setDataProperty(mOnto, method1Object, "isAbstract", true);
		
		assertIsInstance(mOnto, methodClass, method1Object);
		assertIsInstance(mOnto, basicEntryClass, method1Object);
		assertNotIsInstance(mOnto, fullEntryClass, method1Object);

		// add field1
        OWLIndividual field1Object = addIndividual(mOnto, fieldClass, "field1");
		//setDataProperty(mOnto, field1Object, "isAbstract", false);
		clearReasoner();
		assertIsInstance(mOnto, fieldClass, field1Object);
		assertNotIsInstance(mOnto, methodClass, field1Object);
		assertIsInstance(mOnto, basicEntryClass, field1Object);

		{
			// add a full entry and see what it corresponds to
	        OWLIndividual entry1 = addIndividual(mOnto, fullEntryClass, "entry1");
			setDataProperty(mOnto, entry1, "isBold", false);
			setDataProperty(mOnto, entry1, "isItalic", false);
			clearReasoner();
			assertIsInstance(mOnto, fullEntryClass, entry1);
			assertDataPropertyValue("entry1", "isAbstract", false);
			assertIsInstance(mOnto, methodClass, entry1);
			assertNotIsInstance(mOnto, fieldClass, entry1);
		}

		{
			// make basic entry italic - result: method gets abstract
	        OWLIndividual entry2 = addIndividual(mOnto, basicEntryClass, "entry2");
			setDataProperty(mOnto, entry2, "isBold", false);
			setDataProperty(mOnto, entry2, "isItalic", true);
			clearReasoner();
			assertIsInstance(mOnto, basicEntryClass, entry2);
			assertIsInstance(mOnto, methodClass, entry2);
			assertDataPropertyValue("entry2", "isAbstract", true);
			assertNotIsInstance(mOnto, fieldClass, entry2);
		}

		{
			// make basic entry bold - result: concrete field
	        OWLIndividual entry3 = addIndividual(mOnto, basicEntryClass, "entry3");
			setDataProperty(mOnto, entry3, "isBold", true);
			setDataProperty(mOnto, entry3, "isItalic", false);
			clearReasoner();
			assertIsInstance(mOnto, basicEntryClass, entry3);
			assertNotIsInstance(mOnto, methodClass, entry3);
			assertIsInstance(mOnto, fieldClass, entry3);
		}
	}

	private void assertDataPropertyValue(String individualName, String propertyName, boolean value) {
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

	private void setDataProperty(OWLOntology ontology, OWLIndividual individual,
			String name, boolean value) {
		OWLDataPropertyExpression property = findDataProperty(name);
		OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(property, individual, value);
		manager.addAxiom(ontology, axiom);
	}

	@Test
	public void testUnfolding() {
		String testcaseName = "unfolding";
		OWLOntology mOnto = loadOntology(testcaseName);
		OWLClass packageClass = findClass("Package");
		OWLClass mChapterClass = findClass("MChapter");
		OWLClass mSectionClass = findClass("MSection");

		// Package(?x) -> MChapter(?x) and MSection(?x)
		List<Pair<String, OWLClass>> from = new ArrayList<Pair<String, OWLClass>>();
		from.add(new Pair<String, OWLClass>("x", packageClass));
		
		List<Pair<String, OWLClass>> to = new ArrayList<Pair<String, OWLClass>>();
		to.add(new Pair<String, OWLClass>("x", mChapterClass));

		createSWRLRule(mOnto, from, to);
		//createSWRLRule(mOnto, packageClass, mSectionClass);

        OWLIndividual packageObject = addIndividual(mOnto, packageClass, "package1");
		assertIsInstance(mOnto, packageClass, packageObject);
		reasoner.getKB().realize();
		reasoner.getKB().printClassTree();

		// check rule execution
		assertIsInstance(mOnto, mChapterClass, packageObject);
		assertIsInstance(mOnto, mSectionClass, packageObject);
	}

	private SWRLRule createSWRLRule(
			OWLOntology ontology, 
			List<Pair<String, OWLClass>> from, 
			List<Pair<String, OWLClass>> to) {
		
		Set<SWRLAtom> body = new LinkedHashSet<SWRLAtom>();
		Set<SWRLAtom> head = new LinkedHashSet<SWRLAtom>();
		// Rule( antecedent(From(?x)) consequent(To(?x)) )
		for (Pair<String, OWLClass> nextFrom : from) {
			SWRLIArgument var = createVariable(nextFrom.first);
			body.add(factory.getSWRLClassAtom(nextFrom.second, var));
		}
		for (Pair<String, OWLClass> nextTo : to) {
			SWRLIArgument var = createVariable(nextTo.first);
			head.add(factory.getSWRLClassAtom(nextTo.second, var));
		}
		//SWRLBuiltInsVocabulary.

		SWRLRule swrlRule = factory.getSWRLRule(body, head);
		manager.addAxiom(ontology, swrlRule);
		System.out.println("ModelsyncTest.createSWRLRule() " + swrlRule);
		return swrlRule;
	}

	private SWRLVariable createVariable(String name) {
		return factory.getSWRLVariable(IRI.create(NS + name));
	}

	@Test
	public void testDistribution() {
		String testcaseName = "distribution";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass entityClass = findClass("Entity");
		OWLClass enumClass = findClass("Enum");
		OWLClass otherClass = findClass("OtherClass");
		OWLClass paragraphClass = findClass("Paragraph");
		
		// add an instance of Entity and check whether it is recognized as instance
		// of Paragraph too
        OWLIndividual entityObject = addIndividual(mOnto, entityClass, "entity1");
		assertIsInstance(mOnto, entityClass, entityObject);
		assertIsInstance(mOnto, paragraphClass, entityObject);
		assertNotIsInstance(mOnto, enumClass, entityObject);
		assertNotIsInstance(mOnto, otherClass, entityObject);

		// add an instance of Enum and check whether it is recognized as instance
		// of Paragraph too
		clearReasoner();
        OWLIndividual enumObject = addIndividual(mOnto, enumClass, "enum1");
        assertNotIsInstance(mOnto, entityClass, enumObject);
		assertIsInstance(mOnto, paragraphClass, enumObject);
		assertIsInstance(mOnto, enumClass, enumObject);
		assertNotIsInstance(mOnto, otherClass, enumObject);

		// add an instance of Paragraph and check whether it is recognized as instance
		// of Entity or Enum too
		clearReasoner();
        OWLIndividual paragraphObject = addIndividual(mOnto, paragraphClass, "paragraph1");
        assertNotIsInstance(mOnto, entityClass, paragraphObject);
		assertIsInstance(mOnto, paragraphClass, paragraphObject);
		assertNotIsInstance(mOnto, enumClass, paragraphObject);
		assertNotIsInstance(mOnto, otherClass, paragraphObject);
	}

	private void clearReasoner() {
		reasoner = null;
	}

	private void assertNotIsInstance(OWLOntology ontology, OWLClass aClass,
			OWLIndividual object) {
		assertFalse(object + " must not be instance of " + aClass, isInstanceOf(ontology, object, aClass));
	}

	private void assertIsInstance(OWLOntology ontology, OWLClass aClass,
			OWLIndividual object) {
		assertTrue(object + " should be instance of " + aClass, isInstanceOf(ontology, object, aClass));
	}

	private OWLOntology loadOntology(String testcaseName) {
		OWLOntology mOnto = loadOntology(getInputModelURI(testcaseName, "mapping", "owl"));
		return mOnto;
	}

	private OWLIndividual addIndividual(OWLOntology ontology, OWLClass aClass, String name) {
		OWLIndividual lrObject = factory.getOWLNamedIndividual(IRI.create(NS + name));
        OWLAxiom axiomN = factory.getOWLClassAssertionAxiom(aClass, lrObject);
        manager.addAxiom(ontology, axiomN);
		return lrObject;
	}

	@Before
	public void setUp() {
		manager = OWLManager.createOWLOntologyManager();
		/*
		manager.addIRIMapper(new OWLOntologyIRIMapper() {
			
			public IRI getDocumentIRI(IRI ontologyIRI) {
				System.out.println("getDocumentIRI(" + ontologyIRI + ")");
				String iriString = ontologyIRI.toString();
				String dir = "test://";
				if (iriString.startsWith(dir)) {
					iriString = new File("output").getAbsolutePath() + File.separator + "class-1-to-1" + File.separator + iriString.substring(dir.length()) + ".owl";
					return IRI.create(new File(iriString).getAbsoluteFile());
				}
				return ontologyIRI;
			}
		});
		*/
		factory = manager.getOWLDataFactory();
		clearReasoner();
	}

	@After
	public void tearDown() {
		manager = null;
	}

	/*
	private void printAxioms(OWLOntology mOnto) {
		for (OWLAxiom axiom : mOnto.getAxioms()) {
			System.out.println("axiom: " + axiom);
		}
	}
	*/

	private OWLClass findClass(String className) {
		return findClass(NS, className);
	}

	private OWLClass findClass(String namespace, String className) {
		OWLClass foundClass = factory.getOWLClass(IRI.create(namespace + className));
		return foundClass;
	}

	private OWLDataProperty findDataProperty(String name) {
		OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(NS + name));
		return dataProperty;
	}

	private boolean isInstanceOf(OWLOntology mOnto, OWLIndividual individual, OWLClass aClass) {
		if (reasoner == null) {
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

	private OWLOntology loadOntology(URI uri) {
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

	/*
	private void transformMetamodel(String testcaseName, String modelName) {
		URI inputURI = getInputModelURI(testcaseName, modelName, "ecore");
		URI outputURI = getOutputModelURI(testcaseName, modelName, "owl");
		
		ResourceSet rs = getResourceSet();
		Resource resource = rs.getResource(inputURI, true);
		assertNotNull("Resource must not be null", resource);
		assertTrue("Resource must not be emtpy.", resource.getContents().size() > 0);
		EPackage rootPackage = (EPackage) resource.getContents().get(0);
		
		new Ecore2Owl().transformMetamodel(rootPackage, outputURI);
	}
	*/

	private URI getInputModelURI(String testcaseName, String modelName, String extension) {
		File inputFolder = new File("input" + File.separator + testcaseName);
		URI uri = URI.createFileURI(inputFolder.getAbsolutePath() + File.separator + modelName + "." + extension);
		return uri;
	}

	/*
	private URI getOutputModelURI(String testcaseName, String modelName, String extension) {
		File outputFolder = new File("output" + File.separator + testcaseName);
		outputFolder.mkdirs();
		
		URI targetURI = URI.createFileURI(outputFolder.getAbsolutePath() + File.separator + modelName + "." + extension);
		return targetURI;
	}
	
	private ResourceSet getResourceSet() {
		// this is just to register the Ecore language
		EcorePackage.eINSTANCE.getEClass();
		
		ResourceSet rs = new ResourceSetImpl();
		Map<String, Object> extensionToFactoryMap = rs.getResourceFactoryRegistry().getExtensionToFactoryMap();
		extensionToFactoryMap.put("ecore", new XMIResourceFactoryImpl());
		extensionToFactoryMap.put("owl", new OwlResourceFactory());
		return rs;
	}
	*/
}
