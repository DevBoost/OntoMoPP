package org.emftext.runtime.owltext.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.compare.util.ModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.owl.AbbreviatedXSDStringLiteral;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.FeatureReference;
import org.emftext.language.owl.Individual;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.ObjectPropertyValue;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.reasoning.OwlReasoningBuilder;
import org.emftext.language.owl.resource.owl.mopp.OwlPrinter;
import org.emftext.language.owl.resource.owl.mopp.OwlPrinter2;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;
import org.emftext.language.owl.resource.owl.util.OwlStreamUtil;
import org.emftext.runtime.owltext.OWLTextEObjectImpl;
import org.emftext.runtime.owltext.OWLTextEObjectPrinter;
import org.emftext.runtime.owltext.OWLTextValidationMarker;
import org.junit.Test;
import org.owltext.feature.Annotation;
import org.owltext.feature.AnyBigDecimal;
import org.owltext.feature.AnyBigInteger;
import org.owltext.feature.AnyBoolean;
import org.owltext.feature.AnyByte;
import org.owltext.feature.AnyChar;
import org.owltext.feature.AnyDouble;
import org.owltext.feature.AnyFloat;
import org.owltext.feature.AnyInt;
import org.owltext.feature.AnyLong;
import org.owltext.feature.AnyShort;
import org.owltext.feature.Feature;
import org.owltext.feature.FeaturePackage;
import org.owltext.feature.OptionalFeature;
import org.owltext.feature.resource.fea.mopp.FeaResource;
import org.owltext.feature.resource.fea.mopp.FeaResourceFactory;

public class OWLTextTest {

	// private static ResourceSet rs = new
	// org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();

	static {
		FeaResourceFactory feaResourceFactory = new FeaResourceFactory();
		OwlResourceFactory owlResourceFactory = new OwlResourceFactory();
		ResourceFactoryImpl diffFactory = new XMIResourceFactoryImpl();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"fea", feaResourceFactory);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"owl", owlResourceFactory);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"diff", diffFactory);

	}

	@Test
	public void testPrinting() throws IOException, InterruptedException {
		OwlFactory owlFactory = OwlFactory.eINSTANCE;
		OntologyDocument od = owlFactory.createOntologyDocument();
		Ontology ontology = owlFactory.createOntology();
		od.setOntology(ontology);

		ontology.setUri("<test.ontotology>");

		Class exampleClass = owlFactory.createClass();
		exampleClass.setIri("exampleClass");
		ontology.getFrames().add(exampleClass);

		Individual i = owlFactory.createIndividual();
		i.setIri("exampleIndividual");
		ontology.getFrames().add(i);

		ClassAtomic classAtomic = owlFactory.createClassAtomic();
		classAtomic.setClazz(exampleClass);
		i.getTypes().add(classAtomic);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		OwlPrinter2 printer2 = new OwlPrinter2(outStream, null);
		printer2.print(od);
		String printedWith2 = outStream.toString();
		EObject parsedFromPrintedWith2 = parse(printedWith2);

		outStream = new ByteArrayOutputStream();
		OwlPrinter printer = new OwlPrinter(outStream, null);
		printer.print(od);
		String printedWith1 = outStream.toString();
		EObject parsedFromPrintedWith1 = parse(printedWith1);

		File compareDiffFile = new File("./testInput/printerTest.diff");
		checkDiff(compareDiffFile, parsedFromPrintedWith2,
				parsedFromPrintedWith1);
	}

	@Test
	public void testProxyPrinting() throws IOException, InterruptedException {
		OwlFactory owlFactory = OwlFactory.eINSTANCE;
		OntologyDocument od = owlFactory.createOntologyDocument();
		Ontology ontology = owlFactory.createOntology();
		od.setOntology(ontology);

		ontology.setUri("<test.ontotology>");

		Class exampleClass = owlFactory.createClass();
		exampleClass.setIri("exampleClass");
		ontology.getFrames().add(exampleClass);

		Class exampleClassProxy = owlFactory.createClass();
		exampleClassProxy.setIri("exampleClass");

		Individual i = owlFactory.createIndividual();
		i.setIri("exampleIndividual");
		ontology.getFrames().add(i);

		ObjectPropertyValue objectPropertyValue = owlFactory
				.createObjectPropertyValue();
		AbbreviatedXSDStringLiteral value = owlFactory
				.createAbbreviatedXSDStringLiteral();
		value.setValue("TestValue");
		FeatureReference featureReference = owlFactory.createFeatureReference();
		ObjectProperty objectProperty = owlFactory.createObjectProperty();
		objectProperty.setIri("testProperty");
		featureReference.setFeature(objectProperty);
		objectPropertyValue.setFeatureReference(featureReference);

		ontology.getFrames().add(objectProperty);

		objectPropertyValue.setLiteral(value);
		i.getTypes().add(objectPropertyValue);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		OwlPrinter2 printer2 = new OwlPrinter2(outStream, null);
		printer2.print(od);
		String printedWith2 = outStream.toString();
		EObject parsedFromPrintedWith2 = parse(printedWith2);

		TreeIterator<EObject> allContents = parsedFromPrintedWith2
				.eAllContents();
		while (allContents.hasNext()) {
			EObject eObject = (EObject) allContents.next();
			if (eObject instanceof ObjectPropertyValue) {
				ObjectPropertyValue opv = (ObjectPropertyValue) eObject;
				assertTrue(opv.getFeatureReference().getFeature().getIri()
						.equals("testProperty"));
			}

		}
	}

	private EObject parse(String inputString) {
		InputStream inputStream = new ByteArrayInputStream(
				inputString.getBytes());
		ResourceSet rs = new ResourceSetImpl();
		Resource r = rs.createResource(URI.createURI("temp.owl"));
		assertNotNull(r);
		try {
			r.load(inputStream, null);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		List<Diagnostic> errors = r.getErrors();
		for (Diagnostic error : errors) {
			System.out.println("Error " + error.getMessage() + " at "
					+ error.getLine() + "," + error.getColumn());
		}
		List<EObject> contents = r.getContents();
		assertTrue(contents.size() > 0);
		return contents.get(0);
	}

	@Test
	public void testMinimum() throws Throwable {
		String inFileName = "syntaxMinimum.fea";
		File outFile = new File("./testInput/" + inFileName + ".out.owl");
		String expectedOutFileName = "syntaxMinimum.owl";
		assertCorrespondance(inFileName, expectedOutFileName);
		validate(outFile, false);
	}

	@Test
	public void testComments() throws Throwable {
		String inFileName = "comments.fea";
		String expectedOutFileName = "comments.owl";
		assertCorrespondance(inFileName, expectedOutFileName);
	}

	@Test
	public void testSampleOwlification() throws Throwable {
		String inFileName = "sample.fea";
		String expectedOutFileName = "sample.owl";
		assertCorrespondance(inFileName, expectedOutFileName);
	}

	@Test
	public void testSampleValidation() throws Throwable {
		String inFileName = "sample.fea";
		File outFile = new File("./testInput/" + inFileName + ".out.owl");
		validate(outFile, false);
		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);

		// incorporate error
		((Feature) rootObject).setName(null);
		// test with large input model
		List<Feature> manyChilds = new LinkedList<Feature>();
		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
					.createOptionalFeature();
			// f.setName("Feature_" + i);
			manyChilds.add(f);
		}
		((Feature) rootObject).getChildren().addAll(manyChilds);

		File owlifiedModelOutputFile = new File("./testInput/" + inFileName
				+ ".err.owl");

		assertTrue("Root object is a OWLTextEobject",
				rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl
						.getOWLRepresentation());

		BufferedWriter out = new BufferedWriter(new FileWriter(
				owlifiedModelOutputFile));
		out.write(owlRepresentation);
		out.close();
		OwlResource owlifiedOutputResource = loadResource(owlifiedModelOutputFile);
		EObject owlifiedOntologyRoot = owlifiedOutputResource.getContents()
				.get(0);
		assertTrue(
				"The root element of the owlified output resource not instanceOf OntologyDocument",
				owlifiedOntologyRoot instanceof OntologyDocument);
		owlifiedOutputResource.save(Collections.EMPTY_MAP);
		validate(owlifiedModelOutputFile, false);
		checkConsistency(
				loadResource,
				new String[] {
						"The minimal cardinality of '1' for attribute 'name' is not satisfied.",
						"The minimal cardinality of '1' for attribute 'name' is not satisfied.",
						"The minimal cardinality of '1' for attribute 'name' is not satisfied.",
						"The minimal cardinality of '1' for attribute 'name' is not satisfied." });
	}

	
	
	
	private void checkConsistency(FeaResource loadResource,
			String[] expectedErrors) {
		OWLTextValidationMarker cc = new OWLTextValidationMarker();
		cc.annotateValidationResults(loadResource);
		List<String> foundErrors = new ArrayList<String>();
		EList<Diagnostic> errors = loadResource.getErrors();
		for (Diagnostic diagnostic : errors) {
			foundErrors.add(diagnostic.getMessage());
		}
		assertArrayEquals("Not all errors found and/or expected.", expectedErrors,
				foundErrors.toArray());
	}

	// test reference with bounds 0..-1
	@Test
	public void testAddChildrenWithAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createOptionalFeature();
		f.setName("Feature_0");
		feature.getChildren().add(f);
		
		assertTrue(feature.getChildren().get(0).getName().equals("Feature_0"));

		assertCorrespondance(feature, "addChildrenWithAttribute");

	}

	@Test
	public void testAddChildrenWithoutAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		Feature feature = (Feature) loadResource.getContents().get(0);
		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
					.createOptionalFeature();
			// ohne Namen
			// f.setName(null);
			feature.getChildren().add(f);
		}

		// validate the modified RootFeature
		validateRootObjectAsOWLRepresentation(feature, false);
		checkConsistency(
				loadResource,
				new String[] {
						"The minimal cardinality of '1' for attribute 'name' is not satisfied.",
						"The minimal cardinality of '1' for attribute 'name' is not satisfied.",
						"The minimal cardinality of '1' for attribute 'name' is not satisfied." });
	}

	// test attributes with bounds 0..-1
	@Test
	public void testAddComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		for (int i = 0; i < 3; i++) {
			feature.getComments().add("Comment_" + i);
		}

		assertCorrespondance(feature, "addComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testAddAllChildren() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<Feature> childs = new LinkedList<Feature>();
		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
					.createOptionalFeature();
			f.setName("Feature_" + i);
			childs.add(f);
		}
		feature.getChildren().addAll(childs);
		
		assertTrue(feature.getChildren().size() == 3);

		assertCorrespondance(feature, "addAllChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testAddAllComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<String> comments = new LinkedList<String>();
		for (int i = 0; i < 3; i++) {
			comments.add("Comment_" + i);
		}
		feature.getComments().addAll(comments);

		assertCorrespondance(feature, "addAllComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testAddIndexChildren() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
					.createOptionalFeature();
			f.setName("Feature_" + i);
			feature.getChildren().add(0, f);
		}
		
		assertTrue(feature.getChildren().size() == 3);
		
		assertCorrespondance(feature, "addIndexChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testAddIndexComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		for (int i = 0; i < 3; i++) {
			feature.getComments().add(0, "Comment_" + i);
		}

		assertCorrespondance(feature, "addIndexComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testAddAllIndexChildren() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createOptionalFeature();
		f.setName("Feature_0");
		feature.getChildren().add(f);

		List<Feature> childs = new LinkedList<Feature>();
		for (int i = 1; i < 3; i++) {
			OptionalFeature of = FeaturePackage.eINSTANCE.getFeatureFactory()
					.createOptionalFeature();
			of.setName("Feature_" + i);
			childs.add(of);
		}
		feature.getChildren().addAll(0, childs);

		assertCorrespondance(feature, "addAllIndexChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testAddAllIndexComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getComments().add("Comment_0");

		List<String> comments = new LinkedList<String>();
		for (int i = 1; i < 3; i++) {
			comments.add("Comment_" + i);
		}
		feature.getComments().addAll(0, comments);

		assertCorrespondance(feature, "addAllIndexComment");
	}

	// test reference with bounds 0..1
	@Test
	public void testAnnotationWithAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Annotation a = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnnotation();
		a.setValue("Annotation_1");
		feature.setAnnotation(a);

		assertCorrespondance(feature, "annotationWithAttribute");
	}

	@Test
	public void testAnnotationWithoutAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Annotation a = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnnotation();
		a.setValue(null);
		feature.setAnnotation(a);

		// validate the modified RootFeature
		validateRootObjectAsOWLRepresentation(feature, false);
		checkConsistency(loadResource, new String[]{"The minimal cardinality of '1' for attribute 'value' is not satisfied."});
	}

	// test reference with bounds 0..-1
	@Test
	public void testClearChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getChildren().clear();

		assertCorrespondance(feature, "clearChildren");
	}

	// test attributes with bounds 0..-1

	@Test
	public void testClearComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getComments().clear();

		assertCorrespondance(feature, "clearComment");

	}

	// test reference with bounds 0..-1
	@Test
	public void testRemoveChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Feature toRemove = feature.getChildren().get(0);
		feature.getChildren().remove(toRemove);

		assertTrue(feature.getChildren().size() == 2);
		assertFalse(feature.getChildren().contains(toRemove));
		assertCorrespondance(feature, "removeChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testRemoveComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getComments().remove(feature.getComments().get(0));

		assertCorrespondance(feature, "removeComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testRemoveAllChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<Feature> childs = new LinkedList<Feature>();
		for (Feature f : feature.getChildren()) {
			childs.add(f);
		}
		feature.getChildren().removeAll(childs);

		assertCorrespondance(feature, "removeAllChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testRemoveAllComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<String> comments = new LinkedList<String>();
		for (String s : feature.getComments()) {
			comments.add(s);
		}
		feature.getComments().removeAll(comments);

		assertCorrespondance(feature, "removeAllComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testRemoveIndexChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getChildren().remove(1);
		assertTrue("Children was removed", feature.getChildren().size() == 2);
		assertCorrespondance(feature, "removeIndexChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testRemoveIndexComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getComments().remove(1);

		assertCorrespondance(feature, "removeIndexComment");
	}

	@Test
	public void testRetainAllChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<Feature> childs = new LinkedList<Feature>();
		childs.add(feature.getChildren().get(0));

		feature.getChildren().retainAll(childs);
		assertTrue("One children retained", feature.getChildren().size() == 1);
		assertTrue(feature.getChildren().get(0).equals(childs.get(0)));
		assertCorrespondance(feature, "retainAllChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testRetainAllComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<String> comments = new LinkedList<String>();
		comments.add(feature.getComments().get(1));
		comments.add(feature.getComments().get(2));

		feature.getComments().retainAll(comments);

		assertCorrespondance(feature, "retainAllComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testContainsChildren() throws Throwable {
		String inFileName = "containsOrContainsAllChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertTrue("Feature shoud contain the expected child.", feature
				.getChildren().contains(feature.getChildren().get(1)));

	}

	// test attributes with bounds 0..-1
	@Test
	public void testContainsComment() throws Throwable {
		String inFileName = "containsOrContainsAllComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertTrue("Feature shoud contains the expected child.", feature
				.getComments().contains(feature.getComments().get(1)));

	}

	// test reference with bounds 0..-1
	@Test
	public void testContainsAllChildren() throws Throwable {
		String inFileName = "containsOrContainsAllChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<Feature> childs = new LinkedList<Feature>();
		childs.add(feature.getChildren().get(0));
		childs.add(feature.getChildren().get(1));
		childs.add(feature.getChildren().get(2));

		assertTrue("Feature shoud contains all childs.", feature.getChildren()
				.containsAll(childs));
	}

	// test attributes with bounds 0..-1
	@Test
	public void testContainsAllComment() throws Throwable {
		String inFileName = "containsOrContainsAllComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		List<String> childs = new LinkedList<String>();
		childs.add(feature.getComments().get(0));
		childs.add(feature.getComments().get(1));
		childs.add(feature.getComments().get(2));

		assertTrue("Feature shoud contains all childs.", feature.getComments()
				.containsAll(childs));

	}

	// test reference with bounds 0..-1
	@Test
	public void testIsEmptyChildren() throws Throwable {
		String inFileName = "isEmptyChildrenAndComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertTrue("Feature shoudn't contains any childs.", feature
				.getChildren().isEmpty());

	}

	// test attributes with bounds 0..-1
	@Test
	public void testIsEmptyComment() throws Throwable {
		String inFileName = "isEmptyChildrenAndComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertTrue("Feature shoudn't contains any comments.", feature
				.getComments().isEmpty());

	}

	// test reference with bounds 0..-1
	@Test
	public void testSizeChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertEquals("wrong size.", 3, feature.getChildren().size());

	}

	// test attributes with bounds 0..-1
	@Test
	public void testSizeComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertEquals("wrong size.", 3, feature.getComments().size());

	}

	// test reference with bounds 0..-1
	@Test
	public void testToArrayChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Object[] array = feature.getChildren().toArray();
		assertEquals("wrong size.", 3, array.length);
		assertTrue("wrong typ", array[0] instanceof Feature);
		assertEquals("wrong child", feature.getChildren().get(0), array[0]);
	}

	// test attributes with bounds 0..-1
	@Test
	public void testToArrayComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Object[] array = feature.getComments().toArray();
		assertEquals("wrong size.", 3, array.length);
		assertTrue("wrong typ", array[0] instanceof String);
		assertEquals("wrong comment", feature.getComments().get(0), array[0]);
	}

	// test reference with bounds 0..-1
	@Test
	public void testToArraySpecifiedChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Feature[] array = feature.getChildren().toArray(new Feature[0]);
		assertEquals("wrong size.", 3, array.length);
		assertTrue("wrong typ", array[0] instanceof Feature);
		assertEquals("wrong child", feature.getChildren().get(0), array[0]);

		Feature[] array2 = new Feature[10];
		for (int i = 3; i < 10; i++)
			array2[i] = feature.getChildren().get(2);
		array2 = feature.getChildren().toArray(array2);
		assertEquals("wrong size.", 10, array2.length);
		for (int i = 3; i < 10; i++)
			assertNull(
					"the unused space of the specified array should be cleared",
					array2[i]);
	}

	// test attributes with bounds 0..-1
	@Test
	public void testToArraySpecifiedComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		String[] array = feature.getComments().toArray(new String[0]);
		assertEquals("wrong size.", 3, array.length);
		assertTrue("wrong typ", array[0] instanceof String);
		assertEquals("wrong child", feature.getComments().get(0), array[0]);

		String[] array2 = new String[10];
		for (int i = 3; i < 10; i++)
			array2[i] = feature.getComments().get(2);
		array2 = feature.getComments().toArray(array2);
		assertEquals("wrong size.", 10, array2.length);
		for (int i = 3; i < 10; i++)
			assertNull(
					"the unused space of the specified array should be cleared",
					array2[i]);
	}

	// test reference with bounds 0..-1
	@Test
	public void testIndexOfChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertEquals("wrong index.", 0,
				feature.getChildren().indexOf(feature.getChildren().get(0)));
		assertEquals("wrong index.", 1,
				feature.getChildren().indexOf(feature.getChildren().get(1)));
		assertEquals("wrong index.", 2,
				feature.getChildren().indexOf(feature.getChildren().get(2)));
	}

	// test attributes with bounds 0..-1
	@Test
	public void testIndexOfComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		assertEquals("wrong index.", 0,
				feature.getComments().indexOf(feature.getComments().get(0)));
		assertEquals("wrong index.", 1,
				feature.getComments().indexOf(feature.getComments().get(1)));
		assertEquals("wrong index.", 2,
				feature.getComments().indexOf(feature.getComments().get(2)));
	}

	// test reference with bounds 0..-1
	@Test
	public void testMoveChildren() throws Throwable {
		String inFileName = "moveOrSetChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);
		Object old1 = feature.getChildren().get(1);
		Object old0 = feature.getChildren().get(0);
		
		feature.getChildren().move(1, feature.getChildren().get(0));
		
		// correctly moved
		assertTrue(feature.getChildren().get(0).equals(old1));
		assertTrue(feature.getChildren().get(1).equals(old0));
		
		assertCorrespondance(feature, "moveChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testMoveComment() throws Throwable {
		String inFileName = "moveOrSetComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Object old1 = feature.getComments().get(1);
		Object old0 = feature.getComments().get(0);
		
		feature.getComments().move(1, feature.getComments().get(0));

		// correctly moved
		assertTrue(feature.getComments().get(0).equals(old1));
		assertTrue(feature.getComments().get(1).equals(old0));
		
		assertCorrespondance(feature, "moveComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testMoveIndexChildren() throws Throwable {
		String inFileName = "moveOrSetChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Object old1 = feature.getChildren().get(1);
		Object old0 = feature.getChildren().get(0);
		
		
		feature.getChildren().move(1, 0);

		assertTrue(feature.getChildren().get(0).equals(old1));
		assertTrue(feature.getChildren().get(1).equals(old0));
	
		
		assertCorrespondance(feature, "moveIndexChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testMoveIndexComment() throws Throwable {
		String inFileName = "moveOrSetComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getComments().move(1, 0);

		assertCorrespondance(feature, "moveIndexComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testSetChildren() throws Throwable {
		String inFileName = "moveOrSetChildren.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createOptionalFeature();
		f.setName("Feature_new");
		
		feature.getChildren().set(1, f);

		assertTrue(feature.getChildren().get(1).equals(f));
		assertTrue(feature.getChildren().size() == 3);
		
		assertCorrespondance(feature, "setChildren");
	}

	// test attributes with bounds 0..-1
	@Test
	public void testSetComment() throws Throwable {
		String inFileName = "moveOrSetComment.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		feature.getComments().set(1, "Comment_new");

		assertCorrespondance(feature, "setComment");
	}

	// test reference with bounds 0..-1
	@Test
	public void testIteratorChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Iterator<Feature> iter = feature.getChildren().iterator();
		int count = 0;
		while (iter.hasNext()) {
			Feature child = iter.next();
			assertEquals("wrong order of childs",
					feature.getChildren().get(count), child);
			count++;
		}
		assertEquals("iterator has wrong size", 3, count);
	}

	// test attributes with bounds 0..-1
	@Test
	public void testIteratorComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		Iterator<String> iter = feature.getComments().iterator();
		int count = 0;
		while (iter.hasNext()) {
			String comment = iter.next();
			assertEquals("wrong order of comments",
					feature.getComments().get(count), comment);
			count++;
		}
		assertEquals("iterator has wrong size", 3, count);
	}

	// test reference with bounds 0..-1
	@Test
	public void testListIteratorChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		ListIterator<Feature> iter = feature.getChildren().listIterator();
		while (iter.hasNext()) {
			Feature child = iter.next();
			// assertEquals("wrong order of childs",
			// feature.getChildren().get(iter.nextIndex()-1), child);
		}
		assertEquals("ListIterator has wrong index", 3, iter.nextIndex());
	}

	// test attributes with bounds 0..-1
	@Test
	public void testListIteratorComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		ListIterator<String> iter = feature.getComments().listIterator();
		while (iter.hasNext()) {
			String comment = iter.next();
			assertEquals("wrong order of comments",
					feature.getComments().get(iter.nextIndex() - 1), comment);
		}
		assertEquals("ListIterator has wrong index", 3, iter.nextIndex());
	}

	// test reference with bounds 0..-1
	@Test
	public void testListIteratorIndexChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		int count = 0;
		ListIterator<Feature> iter = feature.getChildren().listIterator(1);
		while (iter.hasNext()) {
			Feature child = iter.next();
			assertEquals("wrong order of childs",
					feature.getChildren().get(iter.nextIndex() - 1), child);
			count++;
		}
		assertEquals("ListIterator has wrong size", 2, count);
		assertEquals("ListIterator has wrong index", 3, iter.nextIndex());
	}

	// test attributes with bounds 0..-1
	@Test
	public void testListIteratorIndexComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		int count = 0;
		ListIterator<String> iter = feature.getComments().listIterator(1);
		while (iter.hasNext()) {
			String comment = iter.next();
			assertEquals("wrong order of comments",
					feature.getComments().get(iter.nextIndex() - 1), comment);
			count++;
		}
		assertEquals("ListIterator has wrong size", 2, count);
		assertEquals("ListIterator has wrong index", 3, iter.nextIndex());
	}

	// test reference with bounds 0..-1
	@Test
	public void testSubListChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		int count = 1;
		List<Feature> list = feature.getChildren().subList(1, 3);
		assertEquals("SubList has wrong size", 2, list.size());
		for (Feature fea : list) {
			assertEquals("wrong order of childs",
					feature.getChildren().get(count), fea);
			fea.setName("test" + count);
			count++;
		}

		assertEquals("child has wrong name", "test1", feature.getChildren()
				.get(1).getName());
		assertEquals("child has wrong name", "test2", feature.getChildren()
				.get(2).getName());

	}

	// test attributes with bounds 0..-1
	@Test
	public void testSubListComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));

		int count = 1;
		List<String> list = feature.getComments().subList(1, 3);
		assertEquals("SubList has wrong size", 2, list.size());
		for (String str : list) {
			assertEquals("wrong order of childs",
					feature.getComments().get(count), str);
			count++;
		}
		assertEquals("comment has wrong content", "comment_1", feature
				.getComments().get(1));
		assertEquals("comment has wrong content", "comment_2", feature
				.getComments().get(2));
	}

	// test attributes with bounds 0..-1
	@Test
	public void testAddLiteralInteger() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		AnyInt i1 = FeaturePackage.eINSTANCE.getFeatureFactory().createAnyInt();
		AnyInt i2 = FeaturePackage.eINSTANCE.getFeatureFactory().createAnyInt();
		i1.setLiteral(0);
		i2.setLiteral(-123);
		feature.getAnyLiterals().add(i1);
		feature.getAnyLiterals().add(i2);

		AnyBigInteger bi1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyBigInteger();
		AnyBigInteger bi2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyBigInteger();
		bi1.setLiteral(new BigInteger("0"));
		bi2.setLiteral(new BigInteger("-123"));
		feature.getAnyLiterals().add(bi1);
		assertTrue("Literal deConversion.", bi1.getLiteral().equals(new BigInteger("0")) );
		feature.getAnyLiterals().add(bi2);
		assertTrue("Literal deConversion.", bi2.getLiteral().equals(new BigInteger("-123")) );

		AnyLong l1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyLong();
		AnyLong l2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyLong();
		l1.setLiteral(0L);
		l2.setLiteral(-123L);
		feature.getAnyLiterals().add(l1);
		assertTrue("Literal deConversion.",l1.getLiteral() == 0L);
		feature.getAnyLiterals().add(l2);
		assertTrue("Literal deConversion.",l2.getLiteral() == -123L);

		AnyShort s1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyShort();
		AnyShort s2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyShort();
		s1.setLiteral((short) 0);
		s2.setLiteral((short) -123);
		feature.getAnyLiterals().add(s1);
		assertTrue("Literal deConversion.",s1.getLiteral() == 0);
		feature.getAnyLiterals().add(s2);
		assertTrue("Literal deConversion.",s2.getLiteral() == -123);

		assertCorrespondance(feature, "addLiteralInteger");
	}

	@Test
	public void testAddLiteralFloatDouble() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		AnyFloat f1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyFloat();
		AnyFloat f2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyFloat();
		f1.setLiteral(0.001f);
		f2.setLiteral(-123.45f);
		feature.getAnyLiterals().add(f1);
		assertTrue("Literal deConversion.",f1.getLiteral() == 0.001f);
		feature.getAnyLiterals().add(f2);
		assertTrue("Literal deConversion.",f2.getLiteral() == -123.45f);
		AnyDouble d1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyDouble();
		AnyDouble d2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyDouble();
		d1.setLiteral(0.001);
		d2.setLiteral(-123.45);
		feature.getAnyLiterals().add(d1);
		assertTrue("Literal deConversion.",d1.getLiteral() == 0.001);
		feature.getAnyLiterals().add(d2);
		assertTrue("Literal deConversion.",d2.getLiteral() == -123.45);

		assertCorrespondance(feature, "addLiteralFloatDouble");
	}

	@Test
	public void testAddLiteralBigDecimal() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		AnyBigDecimal bd1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyBigDecimal();
		AnyBigDecimal bd2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyBigDecimal();
		BigDecimal bdv1 = new BigDecimal(78.90);
		bd1.setLiteral(bdv1);
		BigDecimal bdv2 = new BigDecimal(-123.456);
		bd2.setLiteral(bdv2);
		feature.getAnyLiterals().add(bd1);
		assertTrue("Literal deConversion.",bd1.getLiteral().equals(new BigDecimal(78.90)));
		feature.getAnyLiterals().add(bd2);
		assertTrue("Literal deConversion.",bd2.getLiteral().equals(new BigDecimal(-123.456)));
		assertCorrespondance(feature, "addLiteralBigDecimal");
	}

	@Test
	public void testAddLiteralMisc() throws Throwable {
		String inFileName = "syntaxMinimum.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		AnyBoolean b1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyBoolean();
		AnyBoolean b2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyBoolean();
		b1.setLiteral(true);
		b2.setLiteral(false);
		feature.getAnyLiterals().add(b1);
		assertTrue("Literal deConversion.", b1.isLiteral() == true);
		feature.getAnyLiterals().add(b2);
		assertTrue("Literal deConversion.", b2.isLiteral() == false);
		
		AnyChar c1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyChar();
		AnyChar c2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyChar();
		c1.setLiteral('a');
		c2.setLiteral('b');
		feature.getAnyLiterals().add(c1);
		assertTrue("Literal deConversion.",c1.getLiteral() == 'a');
		feature.getAnyLiterals().add(c2);
		assertTrue("Literal deConversion.",c2.getLiteral() == 'b');
		
		AnyByte byte1 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyByte();
		AnyByte byte2 = FeaturePackage.eINSTANCE.getFeatureFactory()
				.createAnyByte();
		byte1.setLiteral((byte) 0x0000);
		byte2.setLiteral((byte) 0xFF85);
		feature.getAnyLiterals().add(byte1);
		assertTrue("Literal deConversion.",byte1.getLiteral() == (byte) 0x0000);
		feature.getAnyLiterals().add(byte2);
		assertTrue("Literal deConversion.",byte2.getLiteral() == (byte) 0xFF85);
		
		assertCorrespondance(feature, "addLiteralMisc");
	}

	// test reference with bounds 0..-1
	@Test
	public void testNotification() throws Throwable {
		String inFileName = "notification.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);
		
		Feature oldParent = feature.getChildren().get(0);
		Feature newParent = feature.getChildren().get(1);
		Feature childFeature = oldParent.getChildren().get(0);
		String childComment = oldParent.getComments().get(0);
		assertTrue(oldParent.getChildren().size() == 1);
		newParent.getChildren().add(childFeature);
		newParent.getComments().add(childComment);

		// containment references are moved
		assertFalse(oldParent.getChildren().contains(childFeature));
		assertTrue(newParent.getComments().contains(childComment));
		assertTrue(childFeature.getParent().equals(newParent));
		
		// attributes are copied
		assertTrue(oldParent.getComments().contains(childComment));
		assertTrue(newParent.getComments().contains(childComment));
		
		assertCorrespondance(feature, "notification");
	}

	// test reference with bounds 0..-1
	@Test
	public void testUnique() throws Throwable {
		String inFileName = "uniqueRelation.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		for (Feature fea : feature.getChildren())
			feature.getUniqueRelations().add(fea);

		feature.getUniqueRelations().add(feature.getChildren().get(0));

		assertEquals("wrong size.", 3, feature.getUniqueRelations().size());
	}

	@Test
	public void testNonUnique() throws Throwable {
		String inFileName = "uniqueRelation.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		for (Feature fea : feature.getChildren())
			feature.getNonUniqueRelations().add(fea);

		feature.getNonUniqueRelations().add(feature.getChildren().get(0));
		// references are always unique! (according to EMF Book)
		assertEquals("Uniqueness always true for references.", 3, feature.getNonUniqueRelations().size());
	}

	// test reference with bounds 0..-1
	@Test
	public void testLastIndexOfNonUniqueRelations() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";

		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature) loadResource.getContents().get(0);

		for (Feature fea : feature.getChildren())
			feature.getNonUniqueRelations().add(fea);

		feature.getNonUniqueRelations().add(feature.getChildren().get(0));
		// existing features are not added again
		assertEquals("wrong index.", 0, feature.getNonUniqueRelations()
				.lastIndexOf(feature.getChildren().get(0)));
	}

	// useful for debug
	private void feaResourceToFile(FeaResource loadResource, String outFileName)
			throws Throwable {
		File outputFile = new File("./testInput/" + outFileName);
		if (outputFile.exists())
			outputFile.delete();
		loadResource.save(new FileOutputStream(outputFile, true), null);
	}

	private void validateRootObjectAsOWLRepresentation(EObject rootObject,
			boolean errorsExpected) throws Exception {
		File owlifiedModelOutputFile = new File("./testInput/temp.owl");

		assertTrue("Root object is a OWLTextEobject",
				rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl
						.getOWLRepresentation());

		BufferedWriter out = new BufferedWriter(new FileWriter(
				owlifiedModelOutputFile));
		out.write(owlRepresentation);
		out.close();
		OwlResource owlifiedOutputResource = loadResource(owlifiedModelOutputFile);
		EObject owlifiedOntologyRoot = owlifiedOutputResource.getContents()
				.get(0);
		assertTrue(
				"The root element of the owlified output resource not instanceOf OntologyDocument",
				owlifiedOntologyRoot instanceof OntologyDocument);
		owlifiedOutputResource.save(Collections.EMPTY_MAP);
		validate(owlifiedModelOutputFile, errorsExpected);

		owlifiedModelOutputFile.delete();
	}

	private void validate(File outFile, boolean errorsExpected)
			throws Exception {
		ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		OwlResource owlifiedResource = (OwlResource) rs.getResource(
				URI.createFileURI(outFile.getAbsolutePath()), true);
		assertNotNull("Out resource was not loaded.", owlifiedResource);
		assertTrue("Out resource is empty.", owlifiedResource.getContents()
				.size() == 1);
		EObject ontologyRoot = owlifiedResource.getContents().get(0);
		assertTrue("Out root is not instanceOf OntologyDocument",
				ontologyRoot instanceof OntologyDocument);
		// outResource.save(new HashMap());
		InputStream stream;
		stream = new FileInputStream(outFile);
		String content = OwlStreamUtil.getContent(stream);

		OwlReasoningBuilder builder = new OwlReasoningBuilder();
		builder.validateOWL(content, owlifiedResource);

		EList<Diagnostic> errors = owlifiedResource.getErrors();
		for (Diagnostic diagnostic : errors) {
			System.out.println("Validation Error: " + diagnostic.getMessage());
		}
		if (!errorsExpected) {
			assertTrue("Resource is error free: ", owlifiedResource.getErrors()
					.size() == 0);
		} else {
			assertTrue("Resource should contain errors: ", owlifiedResource
					.getErrors().size() > 0);

		}
	}

	public void assertCorrespondance(EObject rootObject, String rootNameOfFiles)
			throws Throwable {
		File owlifiedModelOutputFile = new File("./testInput/"
				+ rootNameOfFiles + ".out.owl");
		if (owlifiedModelOutputFile.exists())
			owlifiedModelOutputFile.delete();
		File expectedOutputFile = new File("./testInput/" + rootNameOfFiles
				+ ".owl");
		File compareDiffFile = new File("./testInput/" + rootNameOfFiles
				+ ".diff");

		// owlify feature model
		assertTrue("Root object is a Feature", rootObject instanceof Feature);

		assertTrue("Root object is a OWLTextEobject",
				rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl
						.getOWLRepresentation());

		BufferedWriter out = new BufferedWriter(new FileWriter(
				owlifiedModelOutputFile));
		out.write(owlRepresentation);
		out.close();

		validate(owlifiedModelOutputFile, false);

		OwlResource owlifiedOutputResource = loadResource(owlifiedModelOutputFile);
		EObject owlifiedOntologyRoot = owlifiedOutputResource.getContents()
				.get(0);
		assertTrue(
				"The root element of the owlified output resource not instanceOf OntologyDocument",
				owlifiedOntologyRoot instanceof OntologyDocument);
		owlifiedOutputResource.save(Collections.EMPTY_MAP);

		OwlReasoningBuilder builder = new OwlReasoningBuilder();
		builder.validateOWL(owlRepresentation, owlifiedOutputResource);
		EList<Diagnostic> errors = owlifiedOutputResource.getErrors();
		for (Diagnostic diagnostic : errors) {
			System.out.println(diagnostic.getMessage());
		}
		assertTrue("Resource is error free: ", owlifiedOutputResource
				.getErrors().size() == 0);

		// load expected owl representation
		OwlResource expectedOwlResource = loadResource(expectedOutputFile);
		EObject expectedOntologyRoot = expectedOwlResource.getContents().get(0);
		assertTrue(
				"The root element of the expected owl output is not instance of an OntologyDocument",
				expectedOntologyRoot instanceof OntologyDocument);

		// compare expected and owlified ontology, store diff
		assertTrue("Owlified ontology is error free.", owlifiedOutputResource
				.getErrors().size() == 0);
		assertTrue("Expected ontology is error free.", expectedOwlResource
				.getErrors().size() == 0);

		checkDiff(compareDiffFile, owlifiedOntologyRoot, expectedOntologyRoot);
	}

	public void assertCorrespondance(String inFileName,
			String expectedOutFileName) throws Throwable {
		File modelInputFile = new File("./testInput/" + inFileName);
		File owlifiedModelOutputFile = new File("./testInput/" + inFileName
				+ ".out.owl");
		if (owlifiedModelOutputFile.exists())
			owlifiedModelOutputFile.delete();
		File expectedOutputFile = new File("./testInput/" + expectedOutFileName);
		File compareDiffFile = new File("./testInput/" + inFileName + ".diff");

		// load and owlify feature model
		FeaResource modelResource = loadResource(modelInputFile);
		EObject modelInputRoot = modelResource.getContents().get(0);
		assertTrue(
				"Root element of model input resource is not instanceOf OWLTextEObjectImpl",
				modelInputRoot instanceof OWLTextEObjectImpl);

		// write to temp file
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) modelInputRoot;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl
						.getOWLRepresentation());
		BufferedWriter out = new BufferedWriter(new FileWriter(
				owlifiedModelOutputFile));
		out.write(owlRepresentation);
		out.close();
		// load owl representation of input model and resave to test printing
		OwlResource owlifiedOutputResource = loadResource(owlifiedModelOutputFile);
		EObject owlifiedOntologyRoot = owlifiedOutputResource.getContents()
				.get(0);
		assertTrue(
				"The root element of the owlified output resource not instanceOf OntologyDocument",
				owlifiedOntologyRoot instanceof OntologyDocument);
		owlifiedOutputResource.save(Collections.EMPTY_MAP);

		OwlReasoningBuilder builder = new OwlReasoningBuilder();
		builder.validateOWL(owlRepresentation, owlifiedOutputResource);
		EList<Diagnostic> errors = owlifiedOutputResource.getErrors();
		for (Diagnostic diagnostic : errors) {
			System.out.println(diagnostic.getMessage());
		}
		assertTrue("Resource is error free: ", owlifiedOutputResource
				.getErrors().size() == 0);

		// load expected owl representation
		OwlResource expectedOwlResource = loadResource(expectedOutputFile);
		EObject expectedOntologyRoot = expectedOwlResource.getContents().get(0);
		assertTrue(
				"The root element of the expected owl output is not instance of an OntologyDocument",
				expectedOntologyRoot instanceof OntologyDocument);

		// compare expected and owlified ontology, store diff
		assertTrue("Owlified ontology is error free.", owlifiedOutputResource
				.getErrors().size() == 0);
		assertTrue("Owlified ontology is error free.", expectedOwlResource
				.getErrors().size() == 0);

		checkDiff(compareDiffFile, owlifiedOntologyRoot, expectedOntologyRoot);
	}

	private void checkDiff(File compareDiffFile, EObject owlifiedOntologyRoot,
			EObject expectedOntologyRoot) throws InterruptedException,
			IOException {
		Map<String, Object> options = new LinkedHashMap<String, Object>();
		options.put(MatchOptions.OPTION_IGNORE_ID, true);
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, true);
		options.put(MatchOptions.OPTION_SEARCH_WINDOW, Integer.valueOf(30)); // Toleranzwert
																				// für
																				// die
																				// Zahlen!!!
		MatchModel matchResult = MatchService.doContentMatch(
				expectedOntologyRoot, owlifiedOntologyRoot, options);
		DiffModel genDiff = DiffService.doDiff(matchResult, false);
		// Serialize to XMI
		ModelUtils.save(genDiff, compareDiffFile.getAbsolutePath());
		// TODO should use EMFCompare for comparison, to be independent of tree
		// structure.
		assertTrue("Expected ontology output does not equal produced output: "
				+ expectedOntologyRoot.eResource().getURI().fragment(),
				matchResult.getUnmatchedElements().size() == 0);
	}

	@SuppressWarnings("unchecked")
	private <RESOURCE_TYPE extends Resource> RESOURCE_TYPE loadResource(
			File modelInputFile) {
		ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		RESOURCE_TYPE modelResource = (RESOURCE_TYPE) rs.getResource(
				URI.createFileURI(modelInputFile.getAbsolutePath()), true);
		assertNotNull("Model input resource was not loaded.", modelResource);
		assertTrue("Model input resource is empty.", modelResource
				.getContents().size() == 1);
		return modelResource;
	}
	
	@Test
	public void testAnnotationConstraintValidation() throws Throwable {
		String inFileName = "optionalRoot.fea";
		FeaResource loadResource = loadResource(new File("./testInput/"
				+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);

		File owlifiedModelOutputFile = new File("./testInput/"
				+ inFileName + ".out.owl");
		if (owlifiedModelOutputFile.exists())
			owlifiedModelOutputFile.delete();
				// owlify feature model
		assertTrue("Root object is a Feature", rootObject instanceof Feature);

		assertTrue("Root object is a OWLTextEobject",
				rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl
						.getOWLRepresentation());

		BufferedWriter out = new BufferedWriter(new FileWriter(
				owlifiedModelOutputFile));
		out.write(owlRepresentation);
		out.close();
		
		checkConsistency(
				loadResource,
				new String[] {
						"Root Feature needs to be mandatory." });
	}

}
