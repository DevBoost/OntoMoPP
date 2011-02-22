package org.emftext.ontomopp.modelsync.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.owl.resource.owl.mopp.OwlMetaInformation;
import org.emftext.language.swrl.SWRLDocument;
import org.emftext.language.swrl.resource.swrl.mopp.SwrlResource;
import org.emftext.language.swrl.resource.swrl.util.SwrlResourceUtil;
import org.emftext.language.swrl.resource.swrl.util.SwrlTextResourceUtil;
import org.emftext.language.swrl.util.SWRLRuleBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

/**
 * A test case for the ontology-based model synchronization algorithm that was
 * developed by Federico Rieckhof.
 */
public class ModelsyncTest extends AbstractModelsyncTest {

    @Test
	public void testUpperOntology() {
		String testcaseName = "upper-ontology";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass packageClass = findClass("Package");
		OWLClass typeClass = findClass("Type");
		OWLClass entryClass = findClass("Entry");
		
		OWLObjectProperty typesProperty = findObjectProperty("types");
		
        OWLIndividual package1 = addIndividual(mOnto, packageClass, "package1");
        OWLIndividual type1 = addIndividual(mOnto, typeClass, "type1");
        assertNotIsInstance(mOnto, entryClass, type1);
        clearReasoner();
        setObjectProperty(mOnto, package1, typesProperty, type1);
        
        assertIsInstance(mOnto, entryClass, type1);
	}

	@Test
	public void testSWRLRuleLoading() {
		new OwlMetaInformation().registerResourceFactory();
		
		SwrlResource resource = SwrlTextResourceUtil.getResource(getInputModelURI("petrinet2toytrain", "rules", "swrl"));
		SWRLDocument document = (SWRLDocument) resource.getContents().get(0);
		Set<EObject> unresolvedProxies = SwrlResourceUtil.findUnresolvedProxies(resource);
		for (EObject proxy : unresolvedProxies) {
			System.out.println("Unresolved proxy: " + proxy);
		}
		assertTrue("All proxies must be resolved.", unresolvedProxies.isEmpty());
		
		SWRLRuleBuilder builder = new SWRLRuleBuilder();
		List<SWRLRule> rules = builder.getRules(document);
		assertFalse(rules.isEmpty());
	}
	
	@Test
	public void testPetrinet2Toytrain() {
		String testcaseName = "petrinet2toytrain";
		OWLOntology mOnto = loadOntology(testcaseName);

		// get petri net classes
		OWLClass petrinetClass = findClass("PetriNet");
		OWLClass arcClass = findClass("Arc");
		OWLClass placeClass = findClass("Place");
		OWLClass transitionClass = findClass("Transition");

		// get toy train classes
		OWLClass projectClass = findClass("Project");
		OWLClass trackClass = findClass("Track");
		OWLClass switchClass = findClass("Switch");
		OWLClass outClass = findClass("Out");
		OWLClass inClass = findClass("In");

		{
			// add petri net instance, check mapping to project
			OWLIndividual petrinet = addIndividual(mOnto, petrinetClass, "petriNet1");
			assertIsInstance(mOnto, petrinetClass, petrinet);
			assertIsInstance(mOnto, projectClass, petrinet);
		}

		{
			// add project instance, check mapping to petri net
			OWLIndividual project = addIndividual(mOnto, projectClass, "project1");
			assertIsInstance(mOnto, projectClass, project);
			assertIsInstance(mOnto, petrinetClass, project);
		}

		{
			// add out port, check mapping to place
			OWLIndividual out1 = addIndividual(mOnto, outClass, "out1");
			assertIsInstance(mOnto, outClass, out1);
			assertIsInstance(mOnto, placeClass, out1);
		}

		{
			// add place, check mapping to out port
			OWLIndividual place1 = addIndividual(mOnto, placeClass, "place1");
			assertIsInstance(mOnto, placeClass, place1);
			assertIsInstance(mOnto, outClass, place1);
		}

		{
			// add in port, check mapping to transition
			OWLIndividual in1 = addIndividual(mOnto, inClass, "in1");
			assertIsInstance(mOnto, inClass, in1);
			assertIsInstance(mOnto, transitionClass, in1);
		}

		{
			// add transition, check mapping to in port
			OWLIndividual transition1 = addIndividual(mOnto, transitionClass, "transition1");
			assertIsInstance(mOnto, transitionClass, transition1);
			assertIsInstance(mOnto, inClass, transition1);
		}

		{
			// add track instance, check mapping to arc
			OWLIndividual track1 = addIndividual(mOnto, trackClass, "track1");
			assertIsInstance(mOnto, trackClass, track1);
			assertIsInstance(mOnto, arcClass, track1);
		}

		{
			// add switch instance, check mapping to arc
			OWLIndividual switch1 = addIndividual(mOnto, switchClass, "switch1");
			assertIsInstance(mOnto, switchClass, switch1);
			assertIsInstance(mOnto, arcClass, switch1);
		}
	}
	
	@Test
	public void testRenaming() {
		String testcaseName = "renaming";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass namedElementClass = findClass("NamedElement");
		OWLClass nameableClass = findClass("Nameable");
		OWLClass otherClass = findClass("OtherClass");
		OWLClass mNameClass = findClass("MName");
		
        // add an instance of LeftClass and check whether it is recognized as instance
		// of RightClass too
        OWLIndividual leftObject = addIndividual(mOnto, namedElementClass, "left");
		assertIsInstance(mOnto, mNameClass, leftObject);
		assertIsInstance(mOnto, namedElementClass, leftObject);
		assertIsInstance(mOnto, nameableClass, leftObject);
		assertNotIsInstance(mOnto, otherClass, leftObject);
		
		// add an instance of RightClass and check whether it is recognized as instance
		// of LeftClass too
		clearReasoner();
        OWLIndividual rightObject = addIndividual(mOnto, nameableClass, "right");
		assertIsInstance(mOnto, mNameClass, rightObject);
		assertIsInstance(mOnto, namedElementClass, rightObject);
		assertIsInstance(mOnto, nameableClass, rightObject);
		assertNotIsInstance(mOnto, otherClass, rightObject);
	}

	@Test
	public void testStringOperations() {
		String testcaseName = "string-operations";
		OWLOntology mOnto = loadOntology(testcaseName);

		// find meta elements
		OWLClass packageClass = findClass("Package");
		OWLClass sectionClass = findClass("Section");
		OWLDataProperty packageNameProperty = findDataProperty("packageName");
		OWLDataProperty sectionNameProperty = findDataProperty("sectionName");

		// create a package with name 'p1' 
        OWLIndividual package1 = addIndividual(mOnto, packageClass, "package1");
        setDataProperty(mOnto, package1, "packageName", "p1");
        
        // create SWRL rule
        // Package(?x) and packageName(?x,?y) and stringConcat(?z, "prefix_", ?y) =>
        // Section(?x) and sectionName(?x,?z)
        {
			List<SWRLAtom> from = new ArrayList<SWRLAtom>();
			from.add(createSWRLClassAtom(packageClass, "x"));
			from.add(createSWRLDataPropertyAtom(packageNameProperty, "x", "y"));
			
			List<SWRLDArgument> concatArgs = new ArrayList<SWRLDArgument>();
			concatArgs.add(createVariable("z"));
			concatArgs.add(createConstant("prefix_"));
			concatArgs.add(createVariable("y"));
			from.add(factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.STRING_CONCAT.getIRI(), concatArgs));
	
			List<SWRLAtom> to = new ArrayList<SWRLAtom>();
			to.add(createSWRLClassAtom(sectionClass, "x"));
			to.add(createSWRLDataPropertyAtom(sectionNameProperty, "x", "z"));
			
			createSWRLRule(mOnto, to, from);
        }
        clearReasoner();
        assertIsInstance(mOnto, sectionClass, package1);
        // check sectionName of package1
        assertDataPropertyValue("package1", "packageName", "p1");
        assertDataPropertyValue("package1", "sectionName", "prefix_p1");
        
        // now add an individual of type section and check whether the other direction
        // does also work
        OWLIndividual section1 = addIndividual(mOnto, sectionClass, "section1");
        setDataProperty(mOnto, section1, "sectionName", "prefix_s1");
        
        // add SWRL rule for backward transformation
        // Section(?x) and sectionName(?x,?y) and stringLength(?length, "prefix_") and
        // add(?total, ?length, 1) and substring(?z,?y,?total) =>
        // Package(?x) and packageName(?x,?z)
        {
			List<SWRLAtom> from = new ArrayList<SWRLAtom>();
			from.add(createSWRLClassAtom(sectionClass, "x"));
			from.add(createSWRLDataPropertyAtom(sectionNameProperty, "x", "y"));
			
			List<SWRLDArgument> concatArgs1 = new ArrayList<SWRLDArgument>();
			concatArgs1.add(createVariable("length"));
			concatArgs1.add(createConstant("prefix_"));
			from.add(factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.STRING_LENGTH.getIRI(), concatArgs1));
	
			List<SWRLDArgument> concatArgs2 = new ArrayList<SWRLDArgument>();
			concatArgs2.add(createVariable("total"));
			concatArgs2.add(createVariable("length"));
			concatArgs2.add(createConstant(1));
			from.add(factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.ADD.getIRI(), concatArgs2));
	
			List<SWRLDArgument> concatArgs3 = new ArrayList<SWRLDArgument>();
			concatArgs3.add(createVariable("z"));
			concatArgs3.add(createVariable("y"));
			concatArgs3.add(createVariable("total"));
			from.add(factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.SUBSTRING.getIRI(), concatArgs3));
	
			List<SWRLAtom> to = new ArrayList<SWRLAtom>();
			to.add(createSWRLClassAtom(packageClass, "x"));
			to.add(createSWRLDataPropertyAtom(packageNameProperty, "x", "z"));
			
			createSWRLRule(mOnto, to, from);
        }
        clearReasoner();

        assertIsInstance(mOnto, packageClass, section1);
        assertDataPropertyValue("section1", "packageName", "s1");
	}

	@Test
	public void testReference() {
		String testcaseName = "reference";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass packageClass = findClass("Package");
		OWLClass entityClass = findClass("Entity");
		OWLObjectProperty entitiesProperty = findObjectProperty("entities");
		
		OWLClass sectionClass = findClass("Section");
		OWLClass tableClass = findClass("Table");
		OWLObjectProperty tablesProperty = findObjectProperty("tables");
		
		// create a package that contains an entity
        OWLIndividual package1 = addIndividual(mOnto, packageClass, "package1");
        OWLIndividual entity1 = addIndividual(mOnto, entityClass, "entity1");
        setObjectProperty(mOnto, package1, entitiesProperty, entity1);
        
        assertIsInstance(mOnto, packageClass, package1);
        assertIsInstance(mOnto, entityClass, entity1);
        
        // add SWRL rule:
        // Package(?x) and entities(?x,?y) and Entity(?y) =>
        // Section(?x) and tables(?x,?y) and Table(?y)
		List<SWRLAtom> from = new ArrayList<SWRLAtom>();
		from.add(createSWRLClassAtom(packageClass, "x"));
		from.add(createSWRLObjectPropertyAtom(entitiesProperty, "x", "y"));
		from.add(createSWRLClassAtom(entityClass, "y"));
		
		List<SWRLAtom> to = new ArrayList<SWRLAtom>();
		to.add(createSWRLClassAtom(sectionClass, "x"));
		to.add(createSWRLObjectPropertyAtom(tablesProperty, "x", "y"));
		to.add(createSWRLClassAtom(tableClass, "y"));

		createSWRLRule(mOnto, to, from);
        clearReasoner();
        assertIsInstance(mOnto, sectionClass, package1);
        assertIsInstance(mOnto, tableClass, entity1);
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

	@Test
	public void testUnfolding() {
		String testcaseName = "unfolding";
		OWLOntology mOnto = loadOntology(testcaseName);
		OWLClass packageClass = findClass("Package");
		OWLClass mChapterClass = findClass("MChapter");
		//OWLClass mSectionClass = findClass("MSection");

		// Package(?x) -> MChapter(?x) and MSection(?x)
		List<SWRLAtom> from = new ArrayList<SWRLAtom>();
		from.add(createSWRLClassAtom(packageClass, "x"));
		
		List<SWRLAtom> to = new ArrayList<SWRLAtom>();
		to.add(createSWRLClassAtom(mChapterClass, "x"));

		createSWRLRule(mOnto, to, from);
		//createSWRLRule(mOnto, packageClass, mSectionClass);

        OWLIndividual packageObject = addIndividual(mOnto, packageClass, "package1");
		assertIsInstance(mOnto, packageClass, packageObject);
		reasoner.getKB().realize();
		reasoner.getKB().printClassTree();

		// check rule execution
		assertIsInstance(mOnto, mChapterClass, packageObject);
		// this assertion fails, because chapter and section are disjoint
		//assertIsInstance(mOnto, mSectionClass, packageObject);
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
