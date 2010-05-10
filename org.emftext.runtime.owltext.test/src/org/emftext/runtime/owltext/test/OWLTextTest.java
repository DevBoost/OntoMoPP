package org.emftext.runtime.owltext.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.reasoning.OwlReasoningBuilder;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;
import org.emftext.language.owl.resource.owl.util.OwlStreamUtil;
import org.emftext.runtime.owltext.OWLTextEObjectImpl;
import org.emftext.runtime.owltext.OWLTextEObjectPrinter;
import org.junit.Test;
import org.owltext.feature.Feature;
import org.owltext.feature.resource.fea.mopp.FeaResource;
import org.owltext.feature.resource.fea.mopp.FeaResourceFactory;

public class OWLTextTest {

	private static ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();

	static {
		FeaResourceFactory feaResourceFactory = new FeaResourceFactory();
		OwlResourceFactory owlResourceFactory = new OwlResourceFactory();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"fea", feaResourceFactory);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"owl", owlResourceFactory);
	}

	// TODO implement test for eSet, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for eGet, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for eUnset, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for eIsSet, EAttributes, EReferences, different lower
	// and upper bounds
	// TODO implement test for add
	// TODO implement test for addAll
	// TODO implement test for remove
	// TODO implement test for removeAll
	// TODO implement test for clear()

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
		OwlReasoningBuilder builder = new OwlReasoningBuilder();
		builder.validateOWL(owlRepresentation, owlifiedOutputResource);
	}

	private void validate(File outFile, boolean errorsExpected)
			throws Exception {

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
		assertTrue("Owlified ontology is error free.",owlifiedOutputResource.getErrors().size() == 0);
		assertTrue("Owlified ontology is error free.",expectedOwlResource.getErrors().size() == 0);
		
		Map<String, Object> options = new LinkedHashMap<String, Object>();
		options.put(MatchOptions.OPTION_IGNORE_ID, true);
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, true);
		options.put(MatchOptions.OPTION_SEARCH_WINDOW, Integer.valueOf(100));
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
		RESOURCE_TYPE modelResource = (RESOURCE_TYPE) rs.getResource(URI
				.createFileURI(modelInputFile.getAbsolutePath()), true);
		assertNotNull("Model input resource was not loaded.", modelResource);
		assertTrue("Model input resource is empty.", modelResource
				.getContents().size() == 1);
		return modelResource;
	}

}
