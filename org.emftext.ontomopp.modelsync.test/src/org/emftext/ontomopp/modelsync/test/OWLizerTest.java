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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;
import org.emftext.runtime.owltext.transformation.Ecore2Owl;
import org.emftext.runtime.owltext.transformation.Ecore2OwlOptions;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public class OWLizerTest {

	private static final String TESTCASE_NAME = "owlizer-test";
	private OWLOntologyManager manager;

	@Before
	public void setUp() {
		manager = OWLManager.createOWLOntologyManager();
	}

	@Test
	public void transformMetamodels() {
		transformAndCheck("petrinet");
		transformAndCheck("rails");
	}

	private void transformAndCheck(String name) {
		transformToOWL(name);
		loadAndCheckForConsistency(name);
	}

	private void loadAndCheckForConsistency(String name) {
		URI uri = getOutputModelURI(TESTCASE_NAME, getOutFileName(name), "owl");
		// load ontologies and check for consistency
		OWLOntology loadedOntology = new OWLTestHelper().loadOntology(manager, uri);
		PelletReasoner reasoner = new OWLTestHelper().createReasoner(loadedOntology);
		assertTrue("Ontologies must be consistent", reasoner.isConsistent());
	}

	private void transformToOWL(String language) {
		ResourceSet rs = getResourceSet();
		Ecore2Owl transformer = new Ecore2Owl();
		Map<Ecore2OwlOptions, Object> options = new LinkedHashMap<Ecore2OwlOptions, Object>();
		options.put(Ecore2OwlOptions.PREFIX_PROPERTIES_WITH_CLASSNAME, Boolean.TRUE);
		String relativePath = "../org.emftext.language." + language + "/metamodel/" + language + ".ecore";
		File metamodelFile = new File(relativePath).getAbsoluteFile();
		Resource metamodelResource = rs.getResource(URI.createFileURI(metamodelFile.getAbsolutePath()), true);
		EPackage metamodel = (EPackage) metamodelResource.getContents().get(0);
		URI targetURI = getOutputModelURI(TESTCASE_NAME, getOutFileName(language), "owl");
		transformer.transformMetamodel(metamodel, targetURI);
	}

	private String getOutFileName(String language) {
		return language + "-metamodel";
	}

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
}
