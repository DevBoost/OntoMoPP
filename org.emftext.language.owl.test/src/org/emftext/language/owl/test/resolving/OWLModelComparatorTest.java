/*******************************************************************************
 * Copyright (c) 2006-2011
 * Software Technology Group, Dresden University of Technology
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.owl.test.resolving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Conjunction;
import org.emftext.language.owl.Disjunction;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlPackage;
import org.junit.Before;
import org.junit.Test;

public class OWLModelComparatorTest {

	private ModelComparator modelComparator;
	
	private void registerFactories() {
		EPackage.Registry.INSTANCE.put(OwlPackage.eNS_URI, OwlPackage.eINSTANCE);
		Map<String, Object> extensionToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionToFactoryMap.put("owl",new org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory());
		extensionToFactoryMap.put("xmi", new XMIResourceFactoryImpl());
	}
	
	@Before
	public void setUp() {
		modelComparator = new ModelComparator();
		registerFactories();
	}

	@Test
	public void testOntologiesAreEqualUsingDiff() {
		assertTrue(modelComparator.areModelsEqualRegardingToDiff("simpleOntology_1.owl",
				"simpleOntology_2.owl"));
	}

	@Test
	public void testOntologiesAreEqualUsingMatch() {
		assertTrue(modelComparator.areModelsEqualRegardingToMatch("simpleOntology_1.owl",
				"simpleOntology_2.owl"));
	}

	@Test
	public void testReferencedClassIsEqualToRealClass() {
		OntologyDocument ontologyDocument = (OntologyDocument) 
				ModelStorageUtil.loadModelFromFileName("simpleOntology_1.owl");
		Ontology ontology = ontologyDocument.getOntology();
		Class superClass = (Class) ontology.getFrames().get(0);
		Class subClass = (Class) ontology.getFrames().get(1);
		Class referencedSuperClass = getFirstSuperClass(subClass);
		assertEquals("Superclass and referenced superclass should be equal", superClass,
				referencedSuperClass);
	}

	

	private Class getFirstSuperClass(Class subClass) {
		Disjunction disjunction = (Disjunction) subClass.getSuperClassesDescriptions().get(0);
		Conjunction conjunction = (Conjunction) disjunction.getConjunctions().get(0);
		ClassAtomic classAtomic = (ClassAtomic) conjunction.getPrimaries().get(0);
		return classAtomic.getClazz();
	}

}
