package org.emftext.runtime.owltext.test;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.compare.util.ModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Individual;
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
import org.junit.Test;
import org.owltext.feature.Annotation;
import org.owltext.feature.Feature;
import org.owltext.feature.FeaturePackage;
import org.owltext.feature.OptionalFeature;
import org.owltext.feature.resource.fea.mopp.FeaResource;
import org.owltext.feature.resource.fea.mopp.FeaResourceFactory;

public class OWLTextTest {

	//private static ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();

	static {
		FeaResourceFactory feaResourceFactory = new FeaResourceFactory();
		OwlResourceFactory owlResourceFactory = new OwlResourceFactory();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"fea", feaResourceFactory);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"owl", owlResourceFactory);
	}
	
	// TODO Test owlification and retrieval for various attribute types EString, EInt, EFloat, etc.
	//
	// TODO implement test for eSet, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for eGet, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for eUnset, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for eIsSet, EAttributes, EReferences, different lower
	// and upper bounds

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
		System.out.println(printedWith2);
		EObject parsedFromPrintedWith2 = parse(printedWith2);
		
		outStream = new ByteArrayOutputStream();
		OwlPrinter printer = new OwlPrinter(outStream, null);
		printer.print(od);
		String printedWith1 = outStream.toString();
		System.out.println(printedWith1);
		EObject parsedFromPrintedWith1 = parse(printedWith1);
		
		File compareDiffFile = new File("./testInput/printerTest.diff");
		checkDiff(compareDiffFile, parsedFromPrintedWith2, parsedFromPrintedWith1);
	}
	

	private EObject parse(String inputString) {
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
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
			//f.setName("Feature_" + i);
			manyChilds.add(f);
		}
		((Feature) rootObject).getChildren().addAll(manyChilds);
		
		File owlifiedModelOutputFile = new File("./testInput/" + inFileName
				+ ".err.owl");

		assertTrue("Root object is a OWLTextEobject",
				rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl);

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
		validate(owlifiedModelOutputFile, true);
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testAddChildrenWithAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
		f.setName("Feature_0");
		feature.getChildren().add(f);

		assertCorrespondance(feature, "addChildrenWithAttribute");	
		
	}
	
	@Test
	public void testAddChildrenWithoutAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";	
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		Feature feature = (Feature)loadResource.getContents().get(0);
		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
			//ohne Namen
			//f.setName(null);
			feature.getChildren().add(f);
		}
		
		//validate the modified RootFeature
		validateRootObjectAsOWLRepresentation(feature, true);		
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testAddComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		for (int i = 0; i < 3; i++) {
			feature.getComments().add("Comment_" + i);			
		}
		
		assertCorrespondance(feature, "addComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testAddAllChildren() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		List<Feature> childs = new LinkedList<Feature>();
		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
			f.setName("Feature_" + i);
			childs.add(f);			
		}
		feature.getChildren().addAll(childs);
		
		assertCorrespondance(feature, "addAllChildren");	
	}
	//test attributes with bounds 0..-1
	@Test
	public void testAddAllComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		List<String> comments = new LinkedList<String>();
		for (int i = 0; i < 3; i++) {
			comments.add("Comment_" + i);			
		}
		feature.getComments().addAll(comments);
		
		assertCorrespondance(feature, "addAllComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testAddIndexChildren() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		for (int i = 0; i < 3; i++) {
			OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
			f.setName("Feature_" + i);
			feature.getChildren().add(0, f);		
		}
		assertCorrespondance(feature, "addIndexChildren");			
	}
	
	
	//test attributes with bounds 0..-1
	@Test
	public void testAddIndexComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		for (int i = 0; i < 3; i++) {
			feature.getComments().add(0, "Comment_" + i);			
		}
		
		assertCorrespondance(feature, "addIndexComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testAddAllIndexChildren() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
		f.setName("Feature_0");
		feature.getChildren().add(f);
		
		List<Feature> childs = new LinkedList<Feature>();
		for (int i = 1; i < 3; i++) {
			OptionalFeature of = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
			of.setName("Feature_" + i);
			childs.add(of);
		}
		feature.getChildren().addAll(0, childs);
		
		assertCorrespondance(feature, "addAllIndexChildren");			
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testAddAllIndexComment() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().add("Comment_0");
		
		List<String> comments = new LinkedList<String>();
		for (int i = 1; i < 3; i++) {
			comments.add("Comment_" + i);			
		}
		feature.getComments().addAll(0, comments);
		
		assertCorrespondance(feature, "addAllIndexComment");
	}
	
	//test reference with bounds 0..1
	@Test
	public void testAnnotationWithAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		Annotation a = FeaturePackage.eINSTANCE.getFeatureFactory().createAnnotation();
		a.setValue("Annotation_1");
		feature.setAnnotation(a);
		
		assertCorrespondance(feature, "annotationWithAttribute");	
	}
	
	@Test
	public void testAnnotationWithoutAttribute() throws Throwable {
		String inFileName = "syntaxMinimum.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		Annotation a = FeaturePackage.eINSTANCE.getFeatureFactory().createAnnotation();
		a.setValue(null);
		feature.setAnnotation(a);		
		
		//validate the modified RootFeature
		validateRootObjectAsOWLRepresentation(feature, true);
	}
	
	
	//test reference with bounds 0..-1
	@Test
	public void testClearChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		feature.getChildren().clear();
		
		assertCorrespondance(feature, "clearChildren");
	}
	
	//test attributes with bounds 0..-1
	
	@Test
	public void testClearComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().clear();
		
		assertCorrespondance(feature, "clearComment");
		
	}
	
	//test reference with bounds 0..-1	
	@Test
	public void testRemoveChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getChildren().remove(feature.getChildren().get(0));
		
		assertCorrespondance(feature, "removeChildren");
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testRemoveComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().remove(feature.getComments().get(0));
		
		assertCorrespondance(feature, "removeComment");		
	}
	//test reference with bounds 0..-1	
	@Test
	public void testRemoveAllChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		List<Feature> childs = new LinkedList<Feature>();
		for (Feature f : feature.getChildren()) {
			childs.add(f);
		}
		feature.getChildren().removeAll(childs);
		
		assertCorrespondance(feature, "removeAllChildren");
	}
	//test attributes with bounds 0..-1
	@Test
	public void testRemoveAllComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		List<String> comments = new LinkedList<String>();
		for (String s : feature.getComments()) {
			comments.add(s);
		}
		feature.getComments().removeAll(comments);
		
		assertCorrespondance(feature, "removeAllComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testRemoveIndexChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getChildren().remove(1);
		
		assertCorrespondance(feature, "removeIndexChildren");
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testRemoveIndexComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().remove(1);
		
		assertCorrespondance(feature, "removeIndexComment");
	}
	
	@Test
	public void testRetainAllChildren() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		List<Feature> childs = new LinkedList<Feature>();
		childs.add(feature.getChildren().get(0));
		
		feature.getChildren().retainAll(childs);
		
		assertCorrespondance(feature, "retainAllChildren");
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testRetainAllComment() throws Throwable {
		String inFileName = "clearOrRemoveOrRetainComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);

		List<String> comments = new LinkedList<String>();
		comments.add(feature.getComments().get(1));
		comments.add(feature.getComments().get(2));
		
		feature.getComments().retainAll(comments);
		
		assertCorrespondance(feature, "retainAllComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testContainsChildren() throws Throwable {
		String inFileName = "containsOrContainsAllChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertTrue("Feature shoud contains the expected child.", feature.getChildren().contains(feature.getChildren().get(1)));		
		
	}
	//test attributes with bounds 0..-1
	@Test
	public void testContainsComment() throws Throwable {
		String inFileName = "containsOrContainsAllComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertTrue("Feature shoud contains the expected child.", feature.getComments().contains(feature.getComments().get(1)));		
		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testContainsAllChildren() throws Throwable {
		String inFileName = "containsOrContainsAllChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		List<Feature> childs = new LinkedList<Feature>();
		childs.add(feature.getChildren().get(0));
		childs.add(feature.getChildren().get(1));
		childs.add(feature.getChildren().get(2));
		
		assertTrue("Feature shoud contains all childs.", feature.getChildren().containsAll(childs));			
	}	
	
	//test attributes with bounds 0..-1
	@Test
	public void testContainsAllComment() throws Throwable {
		String inFileName = "containsOrContainsAllComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		List<String> childs = new LinkedList<String>();
		childs.add(feature.getComments().get(0));
		childs.add(feature.getComments().get(1));
		childs.add(feature.getComments().get(2));
		
		assertTrue("Feature shoud contains all childs.", feature.getComments().containsAll(childs));		
		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testIsEmptyChildren() throws Throwable {
		String inFileName = "isEmptyChildrenAndComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertTrue("Feature shoudn't contains any childs.", feature.getChildren().isEmpty());
		
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testIsEmptyComment() throws Throwable {
		String inFileName = "isEmptyChildrenAndComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertTrue("Feature shoudn't contains any comments.", feature.getComments().isEmpty());
		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testSizeChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertEquals("wrong size.", 3, feature.getChildren().size());
		
	}
	
	//test attributes with bounds 0..-1
	@Test
	public void testSizeComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertEquals("wrong size.", 3, feature.getComments().size());
		
	}
	//test reference with bounds 0..-1
	@Test
	public void testToArrayChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getChildren().add(feature.getChildren().get(0));
		
		Object[] array = feature.getChildren().toArray();
		assertEquals("wrong size.", 4, array.length);
		assertTrue("wrong typ", array[0] instanceof Feature);
		assertEquals("wrong child", feature.getChildren().get(0), array[0]);		
	}
	//test attributes with bounds 0..-1
	@Test
	public void testToArrayComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));
		
		Object[] array = feature.getComments().toArray();
		assertEquals("wrong size.", 4, array.length);
		assertTrue("wrong typ", array[0] instanceof String);
		assertEquals("wrong comment", feature.getComments().get(0), array[0]);		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testToArraySpecifiedChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getChildren().add(feature.getChildren().get(0));
		
		Feature[] array = feature.getChildren().toArray(new Feature[0]);
		assertEquals("wrong size.", 4, array.length);
		assertTrue("wrong typ", array[0] instanceof Feature);
		assertEquals("wrong child", feature.getChildren().get(0), array[0]);	
		
		Feature[] array2 = new Feature[10];
		for(int i=4; i<10; i++)
			array2[i] = feature.getChildren().get(2);
		array2 = feature.getChildren().toArray(array2);
		assertEquals("wrong size.", 10, array2.length);
		for(int i=4; i<10; i++)
			assertNull("the unused space of the specified array should be cleared", array2[i]);
	}
	//test attributes with bounds 0..-1
	@Test
	public void testToArraySpecifiedComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));

		
		String[] array = feature.getComments().toArray(new String[0]);
		assertEquals("wrong size.", 4, array.length);
		assertTrue("wrong typ", array[0] instanceof String);
		assertEquals("wrong child", feature.getComments().get(0), array[0]);	
		
		String[] array2 = new String[10];
		for(int i=4; i<10; i++)
			array2[i] = feature.getComments().get(2);
		array2 = feature.getComments().toArray(array2);
		assertEquals("wrong size.", 10, array2.length);
		for(int i=4; i<10; i++)
			assertNull("the unused space of the specified array should be cleared", array2[i]);
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testIndexOfChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertEquals("wrong index.", 0, feature.getChildren().indexOf(feature.getChildren().get(0)));
		assertEquals("wrong index.", 1, feature.getChildren().indexOf(feature.getChildren().get(1)));
		assertEquals("wrong index.", 2, feature.getChildren().indexOf(feature.getChildren().get(2)));		
	}
	//test attributes with bounds 0..-1
	@Test
	public void testIndexOfComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		assertEquals("wrong index.", 0, feature.getComments().indexOf(feature.getComments().get(0)));
		assertEquals("wrong index.", 1, feature.getComments().indexOf(feature.getComments().get(1)));
		assertEquals("wrong index.", 2, feature.getComments().indexOf(feature.getComments().get(2)));		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testLastIndexOfChildren() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getChildren().add(feature.getChildren().get(0));
		assertEquals("wrong index.", 3, feature.getChildren().lastIndexOf(feature.getChildren().get(0)));	
	}
	//test attributes with bounds 0..-1
	@Test
	public void testLastIndexOfComment() throws Throwable {
		String inFileName = "indexOrSizeOrToArray.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().add(feature.getComments().get(0));
		assertEquals("wrong index.", 3, feature.getComments().lastIndexOf(feature.getComments().get(0)));	
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testMoveChildren() throws Throwable {
		String inFileName = "moveOrSetChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getChildren().move(1, feature.getChildren().get(0));
		
		assertCorrespondance(feature, "moveChildren");
	}
	//test attributes with bounds 0..-1
	@Test
	public void testMoveComment() throws Throwable {
		String inFileName = "moveOrSetComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().move(1, feature.getComments().get(0));
		
		assertCorrespondance(feature, "moveComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testMoveIndexChildren() throws Throwable {
		String inFileName = "moveOrSetChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getChildren().move(1, 0);
		
		assertCorrespondance(feature, "moveIndexChildren");
	}
	//test attributes with bounds 0..-1
	@Test
	public void testMoveIndexComment() throws Throwable {
		String inFileName = "moveOrSetComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().move(1, 0);
		
		assertCorrespondance(feature, "moveIndexComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testSetChildren() throws Throwable {
		String inFileName = "moveOrSetChildren.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		OptionalFeature f = FeaturePackage.eINSTANCE.getFeatureFactory().createOptionalFeature();
		f.setName("Feature_new");
		feature.getChildren().set(1, f);

		assertCorrespondance(feature, "setChildren");
	}
	//test attributes with bounds 0..-1
	@Test
	public void testSetComment() throws Throwable {
		String inFileName = "moveOrSetComment.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		
		feature.getComments().set(1, "Comment_new");

		assertCorrespondance(feature, "setComment");
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testIteratorChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getChildren().add(feature.getChildren().get(0));
		
		Iterator<Feature> iter = feature.getChildren().iterator();
		int count = 0;
		while(iter.hasNext()){
			Feature child = iter.next();
			assertEquals("wrong order of childs", feature.getChildren().get(count), child);				
			count++;			
		}
		assertEquals("iterator has wrong size", 4, count);	
	}
	//test attributes with bounds 0..-1
	@Test
	public void testIteratorComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));
		
		Iterator<String> iter = feature.getComments().iterator();
		int count = 0;
		while(iter.hasNext()){
			String comment = iter.next();
			assertEquals("wrong order of comments", feature.getComments().get(count), comment);				
			count++;			
		}
		assertEquals("iterator has wrong size", 4, count);		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testListIteratorChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getChildren().add(feature.getChildren().get(0));
		
		ListIterator<Feature> iter = feature.getChildren().listIterator();
		while(iter.hasNext()){
			Feature child = iter.next();
			//assertEquals("wrong order of childs", feature.getChildren().get(iter.nextIndex()-1), child);						
		}
		assertEquals("ListIterator has wrong index", 4, iter.nextIndex());	
	}
	//test attributes with bounds 0..-1
	@Test
	public void testListIteratorComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));
		
		ListIterator<String> iter = feature.getComments().listIterator();
		while(iter.hasNext()){
			String comment = iter.next();
			assertEquals("wrong order of comments", feature.getComments().get(iter.nextIndex()-1), comment);						
		}
		assertEquals("ListIterator has wrong index", 4, iter.nextIndex());		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testListIteratorIndexChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getChildren().add(feature.getChildren().get(0));
		
		int count = 0;
		ListIterator<Feature> iter = feature.getChildren().listIterator(1);
		while(iter.hasNext()){
			Feature child = iter.next();
			assertEquals("wrong order of childs", feature.getChildren().get(iter.nextIndex()-1), child);	
			count++;
		}
		assertEquals("ListIterator has wrong size", 3, count);	
		assertEquals("ListIterator has wrong index", 4, iter.nextIndex());	
	}
	//test attributes with bounds 0..-1
	@Test
	public void testListIteratorIndexComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));
		
		int count = 0;
		ListIterator<String> iter = feature.getComments().listIterator(1);
		while(iter.hasNext()){
			String comment = iter.next();
			assertEquals("wrong order of comments", feature.getComments().get(iter.nextIndex()-1), comment);
			count++;
		}
		assertEquals("ListIterator has wrong size", 3, count);	
		assertEquals("ListIterator has wrong index", 4, iter.nextIndex());		
	}
	
	//test reference with bounds 0..-1
	@Test
	public void testSubListChildren() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getChildren().add(feature.getChildren().get(0));
		
		int count = 2;
		List<Feature> list = feature.getChildren().subList(2, 4);
		assertEquals("SubList has wrong size", 2, list.size());	
		for(Feature fea : list){
			assertEquals("wrong order of childs", feature.getChildren().get(count), fea);	
			fea.setName("test"+count);
			count++;
		}
		assertEquals("child has wrong name", "test2", feature.getChildren().get(2).getName());
		assertEquals("child has wrong name", "test3", feature.getChildren().get(3).getName());
		
	}
	//test attributes with bounds 0..-1
	@Test
	public void testSubListComment() throws Throwable {
		String inFileName = "iteratorOrSublist.fea";		
		
		FeaResource loadResource = loadResource(new File("./testInput/"+ inFileName));
		EObject rootObject = loadResource.getContents().get(0);
		assertTrue("Root object is a Feature", rootObject instanceof Feature);
		Feature feature = (Feature)loadResource.getContents().get(0);
		feature.getComments().add(feature.getComments().get(0));
		
		int count = 2;
		List<String> list = feature.getComments().subList(2, 4);
		assertEquals("SubList has wrong size", 2, list.size());	
		for(String fea : list){
			assertEquals("wrong order of childs", feature.getComments().get(count), fea);
			list.set(count-2, "test"+count);
			count++;
		}
		assertEquals("comment has wrong content", "test2", feature.getComments().get(2));
		assertEquals("comment has wrong content", "test3", feature.getComments().get(3));
	}
	
	//usefull for debug
	private void feaResourceToFile(FeaResource loadResource, String outFileName)throws Throwable {
		File outputFile = new File("./testInput/" + outFileName);
		if (outputFile.exists())outputFile.delete();		
		loadResource.save(new FileOutputStream(outputFile, true), null);
	}
	
	private void validateRootObjectAsOWLRepresentation(EObject rootObject, boolean errorsExpected)throws Exception {
		File owlifiedModelOutputFile = new File("./testInput/temp.owl");

		assertTrue("Root object is a OWLTextEobject",
				rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl);

		BufferedWriter out = new BufferedWriter(new FileWriter(owlifiedModelOutputFile));
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
		OwlResource owlifiedResource = (OwlResource) rs.getResource(URI
				.createFileURI(outFile.getAbsolutePath()), true);
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
	
	public void assertCorrespondance(EObject rootObject, String rootNameOfFiles) throws Throwable {
		File owlifiedModelOutputFile = new File("./testInput/" + rootNameOfFiles + ".out.owl");
		if (owlifiedModelOutputFile.exists())
			owlifiedModelOutputFile.delete();
		File expectedOutputFile = new File("./testInput/" + rootNameOfFiles + ".owl");
		File compareDiffFile = new File("./testInput/" + rootNameOfFiles + ".diff");
		
		// owlify feature model
		assertTrue("Root object is a Feature", rootObject instanceof Feature);

		assertTrue("Root object is a OWLTextEobject", rootObject instanceof OWLTextEObjectImpl);
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) rootObject;
		String owlRepresentation = OWLTextEObjectPrinter
				.getOWLRepresentation(rootOWLTextObjectImpl);

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
				.getOWLRepresentation(rootOWLTextObjectImpl);
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
		options.put(MatchOptions.OPTION_SEARCH_WINDOW, Integer.valueOf(10));
		MatchModel matchResult = MatchService.doContentMatch(
				expectedOntologyRoot, owlifiedOntologyRoot, options);
		DiffModel genDiff = DiffService.doDiff(matchResult, false);
		// Serialize to XMI
		ModelUtils.save(genDiff, compareDiffFile.getAbsolutePath());
		// TODO should use EMFCompare for comparison, to be independent of tree
		// structure.
		assertTrue("Expected ontology output does not equal produced output.",
				matchResult.getUnmatchedElements().size() == 0);
	}

	@SuppressWarnings("unchecked")
	private <RESOURCE_TYPE extends Resource> RESOURCE_TYPE loadResource(
			File modelInputFile) {
		ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		RESOURCE_TYPE modelResource = (RESOURCE_TYPE) rs  .getResource(URI
				.createFileURI(modelInputFile.getAbsolutePath()), true);
		assertNotNull("Model input resource was not loaded.", modelResource);
		assertTrue("Model input resource is empty.", modelResource
				.getContents().size() == 1);
		return modelResource;
	}

}
