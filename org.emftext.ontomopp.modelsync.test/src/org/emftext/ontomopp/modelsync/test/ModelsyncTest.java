package org.emftext.ontomopp.modelsync.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
		assetIsInstance(mOnto, leftRightClass, leftObject);
		assetIsInstance(mOnto, leftClass, leftObject);
		assetIsInstance(mOnto, rightClass, leftObject);
		assertNotIsInstance(mOnto, otherClass, leftObject);
		
		// add an instance of RightClass and check whether it is recognized as instance
		// of LeftClass too
		clearReasoner();
        OWLIndividual rightObject = addIndividual(mOnto, rightClass, "right");
		assetIsInstance(mOnto, leftRightClass, rightObject);
		assetIsInstance(mOnto, leftClass, rightObject);
		assetIsInstance(mOnto, rightClass, rightObject);
		assertNotIsInstance(mOnto, otherClass, rightObject);
	}

	@Test
	public void testCrossDistribution() {
		String testcaseName = "cross-distribution";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass entityClass = findClass("Entity");
		OWLClass paragraphClass = findClass("Paragraph");
		OWLClass figureClass = findClass("Figure");

		// add abstract entity1
        OWLIndividual entity1Object = addIndividual(mOnto, entityClass, "entity1");
		setDataProperty(mOnto, entity1Object, "isAbstract", true);
		
		assetIsInstance(mOnto, entityClass, entity1Object);
		assetIsInstance(mOnto, paragraphClass, entity1Object);

		// add concrete entity2
        OWLIndividual entity2Object = addIndividual(mOnto, entityClass, "entity2");
		setDataProperty(mOnto, entity2Object, "isAbstract", false);
		clearReasoner();
		assetIsInstance(mOnto, entityClass, entity2Object);
		assetIsInstance(mOnto, figureClass, entity2Object);
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
		OWLClass chapterClass = findClass("MChapter");
		OWLClass sectionClass = findClass("MSection");

		createSWRLRule(mOnto, packageClass, chapterClass);
		createSWRLRule(mOnto, packageClass, sectionClass);

        OWLIndividual packageObject = addIndividual(mOnto, packageClass, "package1");
		assetIsInstance(mOnto, packageClass, packageObject);
		reasoner.getKB().realize();
		reasoner.getKB().printClassTree();

		// check rule execution
		assetIsInstance(mOnto, chapterClass, packageObject);
	}

	private SWRLRule createSWRLRule(OWLOntology ontology, OWLClass from, OWLClass... to) {
		Set<SWRLAtom> body = new LinkedHashSet<SWRLAtom>();
		Set<SWRLAtom> head = new LinkedHashSet<SWRLAtom>();
		// Rule( antecedent(From(?x)) consequent(To(?x)) )
		SWRLIArgument varX = factory.getSWRLVariable(IRI.create(NS + "x"));
		body.add(factory.getSWRLClassAtom(from, varX));
		for (OWLClass nextTo : to) {
			head.add(factory.getSWRLClassAtom(nextTo, varX));
		}
		//SWRLBuiltInsVocabulary.

		SWRLRule swrlRule = factory.getSWRLRule(body, head);
		manager.addAxiom(ontology, swrlRule);
		System.out.println("ModelsyncTest.createSWRLRule() " + swrlRule);
		return swrlRule;
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
		assetIsInstance(mOnto, entityClass, entityObject);
		assetIsInstance(mOnto, paragraphClass, entityObject);
		assertNotIsInstance(mOnto, enumClass, entityObject);
		assertNotIsInstance(mOnto, otherClass, entityObject);

		// add an instance of Enum and check whether it is recognized as instance
		// of Paragraph too
		clearReasoner();
        OWLIndividual enumObject = addIndividual(mOnto, enumClass, "enum1");
        assertNotIsInstance(mOnto, entityClass, enumObject);
		assetIsInstance(mOnto, paragraphClass, enumObject);
		assetIsInstance(mOnto, enumClass, enumObject);
		assertNotIsInstance(mOnto, otherClass, enumObject);

		// add an instance of Paragraph and check whether it is recognized as instance
		// of Entity or Enum too
		clearReasoner();
        OWLIndividual fruitObject = addIndividual(mOnto, paragraphClass, "fruit1");
        assertNotIsInstance(mOnto, entityClass, fruitObject);
		assetIsInstance(mOnto, paragraphClass, fruitObject);
		assertNotIsInstance(mOnto, enumClass, fruitObject);
		assertNotIsInstance(mOnto, otherClass, fruitObject);
	}

	private void clearReasoner() {
		reasoner = null;
	}

	private void assertNotIsInstance(OWLOntology ontology, OWLClass aClass,
			OWLIndividual object) {
		assertFalse(object + " must not be instance of " + aClass, isInstanceOf(ontology, object, aClass));
	}

	private void assetIsInstance(OWLOntology ontology, OWLClass aClass,
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
		OWLClass leftClass = factory.getOWLClass(IRI.create(NS + className));
		return leftClass;
	}

	private OWLDataPropertyExpression findDataProperty(String name) {
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
