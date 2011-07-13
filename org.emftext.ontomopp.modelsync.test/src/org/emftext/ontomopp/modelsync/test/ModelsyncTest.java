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

        setObjectProperty(mOnto, package1, typesProperty, type1);
        
        assertIsInstance(mOnto, entryClass, type1);
	}

	@Test
	public void testSWRLRuleLoading() {
		String testcaseName = "petrinet2toytrain";
		String ruleFileName = "rules";
		List<SWRLRule> rules = loadSWRLRules(null, testcaseName, ruleFileName);
		assertFalse(rules.isEmpty());
	}

	private List<SWRLRule> loadSWRLRules(OWLOntology ontology, String testcaseName, String ruleFileName) {
		new OwlMetaInformation().registerResourceFactory();
		SwrlResource resource = SwrlResourceUtil.getResource(getInputModelURI(testcaseName, ruleFileName, "swrl"));
		SWRLDocument document = (SWRLDocument) resource.getContents().get(0);
		Set<EObject> unresolvedProxies = SwrlResourceUtil.findUnresolvedProxies(resource);
		for (EObject proxy : unresolvedProxies) {
			System.out.println("Unresolved proxy: " + proxy);
		}
		assertTrue("All proxies must be resolved.", unresolvedProxies.isEmpty());
		
		SWRLRuleBuilder builder = new SWRLRuleBuilder();
		List<SWRLRule> rules = builder.getRules(document);
		if (ontology != null) {
			for (SWRLRule swrlRule : rules) {
				manager.addAxiom(ontology, swrlRule);
			}
		}
		clearReasoner();
		return rules;
	}

	@Test
	public void testSwrlObjectPropertyRule() {
		String testcaseName = "swrl-object-property";
		OWLOntology mOnto = loadOntology(testcaseName);

		OWLClass classA = findClass("A");
		OWLClass classB = findClass("B");
		OWLClass classC = findClass("C");
		OWLClass classD = findClass("D");
		
		// create a,b,prop(a,b)
		OWLIndividual a = addIndividual(mOnto, classA, "a");
		OWLIndividual b = addIndividual(mOnto, classB, "b");
		OWLObjectProperty property1 = findObjectProperty("prop1");
		setObjectProperty(mOnto, a, property1, b);
		
		loadSWRLRules(mOnto, testcaseName, "rules");

		assertIsInstance(mOnto, classA, a);
		assertObjectPropertyValue(mOnto, "a", "prop1", "b");
		assertIsInstance(mOnto, classB, b);

		assertIsInstance(mOnto, classC, a);
		assertObjectPropertyValue(mOnto, "a", "prop2", "b");
		assertIsInstance(mOnto, classD, b);
	}
	
	@Test
	public void testPetrinet2Toytrain() {
		String testcaseName = "petrinet2toytrain";
		OWLOntology mOnto = loadOntology(testcaseName);
		loadSWRLRules(mOnto, testcaseName, "rules");

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
		OWLClass connectionClass = findClass("Connection");
		
		OWLClass dummyClass = findClass("Dummy");
		
		OWLObjectProperty sourceProperty = findObjectProperty("source");
		OWLObjectProperty targetProperty = findObjectProperty("target");
		OWLObjectProperty portsProperty = findObjectProperty("ports");
		OWLObjectProperty fromProperty = findObjectProperty("from");
		OWLObjectProperty toProperty = findObjectProperty("to");

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
			// add out port, check mapping to transition
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
			// test arc,place,transition -> track,in,out
			OWLIndividual arc2 = addIndividual(mOnto, arcClass, "arc2");
			OWLIndividual place2 = addIndividual(mOnto, placeClass, "place2");
			OWLIndividual transition2 = addIndividual(mOnto, transitionClass, "transition2");
			setObjectProperty(mOnto, arc2, sourceProperty, transition2);
			setObjectProperty(mOnto, arc2, targetProperty, place2);
			setDataProperty(mOnto, transition2, "hasMultipleOutgoingArcs", false);
			assertDataPropertyValue(mOnto, "transition2", "hasMultipleOutgoingArcs", false);

			assertObjectPropertyValue(mOnto, "arc2", "source", "transition2");
			assertObjectPropertyValue(mOnto, "arc2", "target", "place2");

			assertIsInstance(mOnto, arcClass, arc2);
			assertIsInstance(mOnto, placeClass, place2);
			assertIsInstance(mOnto, transitionClass, transition2);

			assertIsInstance(mOnto, outClass, place2);
			assertIsInstance(mOnto, inClass, transition2);
			assertIsInstance(mOnto, trackClass, arc2);
		}

		{
			// test track,in,out -> arc,place,transition 
			OWLIndividual track5 = addIndividual(mOnto, trackClass, "track5");
			OWLIndividual in5 = addIndividual(mOnto, inClass, "in5");
			OWLIndividual out5 = addIndividual(mOnto, outClass, "out5");
			setObjectProperty(mOnto, track5, portsProperty, in5);
			setObjectProperty(mOnto, track5, portsProperty, out5);
			//setDataProperty(mOnto, transition2, "hasMultipleOutgoingArcs", false);
			//assertDataPropertyValue(mOnto, "transition2", "hasMultipleOutgoingArcs", false);

			assertObjectPropertyValue(mOnto, "track5", "ports", "in5");
			assertObjectPropertyValue(mOnto, "track5", "ports", "out5");

			assertIsInstance(mOnto, trackClass, track5);
			assertIsInstance(mOnto, inClass, in5);
			assertIsInstance(mOnto, outClass, out5);

			assertIsInstance(mOnto, transitionClass, in5);
			assertIsInstance(mOnto, placeClass, out5);
			assertIsInstance(mOnto, arcClass, track5);
			assertObjectPropertyValue(mOnto, "track5", "source", "in5");
			assertObjectPropertyValue(mOnto, "track5", "target", "out5");
		}

		{
			// test place-arc-transition -> out-connection-in
			OWLIndividual arc8 = addIndividual(mOnto, arcClass, "arc8");
			OWLIndividual place8 = addIndividual(mOnto, placeClass, "place8");
			OWLIndividual transition8 = addIndividual(mOnto, transitionClass, "transition8");
			setObjectProperty(mOnto, arc8, targetProperty, transition8);
			setObjectProperty(mOnto, arc8, sourceProperty, place8);

			assertObjectPropertyValue(mOnto, "arc8", "source", "place8");
			assertObjectPropertyValue(mOnto, "arc8", "target", "transition8");

			assertIsInstance(mOnto, arcClass, arc8);
			assertIsInstance(mOnto, placeClass, place8);
			assertIsInstance(mOnto, transitionClass, transition8);

			assertIsInstance(mOnto, outClass, place8);
			assertIsInstance(mOnto, inClass, transition8);
			assertIsInstance(mOnto, connectionClass, arc8);
		}

		{
			// test out-connection-in -> place-arc-transition
			OWLIndividual connection9 = addIndividual(mOnto, connectionClass, "connection9");
			OWLIndividual in9 = addIndividual(mOnto, inClass, "in9");
			OWLIndividual out9 = addIndividual(mOnto, outClass, "out9");
			setObjectProperty(mOnto, connection9, toProperty, in9);
			setObjectProperty(mOnto, connection9, fromProperty, out9);

			assertObjectPropertyValue(mOnto, "connection9", "to", "in9");
			assertObjectPropertyValue(mOnto, "connection9", "from", "out9");

			assertIsInstance(mOnto, connectionClass, connection9);
			assertIsInstance(mOnto, inClass, in9);
			assertIsInstance(mOnto, outClass, out9);

			assertIsInstance(mOnto, transitionClass, in9);
			assertIsInstance(mOnto, placeClass, out9);
			assertIsInstance(mOnto, arcClass, connection9);
			assertObjectPropertyValue(mOnto, "connection9", "source", "out9");
			assertObjectPropertyValue(mOnto, "connection9", "target", "in9");
		}

		{
			// test arcA,arcB,placeA,placeB,transition -> switch,outA,outB,in
			// (switch type one)
			OWLIndividual arc3a = addIndividual(mOnto, arcClass, "arc3a");
			OWLIndividual arc3b = addIndividual(mOnto, arcClass, "arc3b");
			OWLIndividual place3a = addIndividual(mOnto, placeClass, "place3a");
			OWLIndividual place3b = addIndividual(mOnto, placeClass, "place3b");
			OWLIndividual transition3 = addIndividual(mOnto, transitionClass, "transition3");
			setObjectProperty(mOnto, arc3a, targetProperty, place3a);
			setObjectProperty(mOnto, arc3b, targetProperty, place3b);
			setObjectProperty(mOnto, arc3a, sourceProperty, transition3);
			setObjectProperty(mOnto, arc3b, sourceProperty, transition3);

			assertObjectPropertyValue(mOnto, "arc3a", "source", "transition3");
			assertObjectPropertyValue(mOnto, "arc3a", "target", "place3a");
			assertObjectPropertyValue(mOnto, "arc3b", "source", "transition3");
			assertObjectPropertyValue(mOnto, "arc3b", "target", "place3b");
			assertDataPropertyValue(mOnto, "transition3", "hasMultipleOutgoingArcs", true);

			assertIsInstance(mOnto, arcClass, arc3a);
			assertIsInstance(mOnto, arcClass, arc3b);
			assertIsInstance(mOnto, placeClass, place3a);
			assertIsInstance(mOnto, placeClass, place3b);
			assertIsInstance(mOnto, transitionClass, transition3);

			assertIsInstance(mOnto, outClass, place3a);
			assertIsInstance(mOnto, outClass, place3b);
			assertIsInstance(mOnto, inClass, transition3);
			assertTrue(isInstanceOf(mOnto, switchClass, arc3a) || isInstanceOf(mOnto, switchClass, arc3b));
		}

		{
			// test place,arcA,transition,arcB,arcC -> switch,out,inA,inB
			// (switch type two)
			OWLIndividual arc4a = addIndividual(mOnto, arcClass, "arc4a");
			OWLIndividual arc4b = addIndividual(mOnto, arcClass, "arc4b");
			OWLIndividual arc4c = addIndividual(mOnto, arcClass, "arc4c");
			OWLIndividual place4 = addIndividual(mOnto, placeClass, "place4");
			OWLIndividual transition4 = addIndividual(mOnto, transitionClass, "transition4");
			setObjectProperty(mOnto, arc4a, sourceProperty, transition4);
			setObjectProperty(mOnto, arc4a, targetProperty, place4);
			setObjectProperty(mOnto, arc4b, targetProperty, transition4);
			setObjectProperty(mOnto, arc4c, targetProperty, transition4);

			assertObjectPropertyValue(mOnto, "arc4a", "source", "transition4");
			assertObjectPropertyValue(mOnto, "arc4a", "target", "place4");
			assertObjectPropertyValue(mOnto, "arc4b", "target", "transition4");
			assertObjectPropertyValue(mOnto, "arc4c", "target", "transition4");
			assertDataPropertyValue(mOnto, "transition4", "hasMultipleIncomingArcs", true);

			assertIsInstance(mOnto, arcClass, arc4a);
			assertIsInstance(mOnto, arcClass, arc4b);
			assertIsInstance(mOnto, arcClass, arc4c);
			assertIsInstance(mOnto, placeClass, place4);
			assertIsInstance(mOnto, transitionClass, transition4);

			assertIsInstance(mOnto, outClass, place4);
			assertIsInstance(mOnto, switchClass, arc4a);
			assertIsInstance(mOnto, inClass, transition4);
		}

		{
			// test switch,outA,outB,in -> arcA,arcB,placeA,placeB,transition 
			// (switch type one)
			OWLIndividual out6a = addIndividual(mOnto, outClass, "out6a");
			OWLIndividual out6b = addIndividual(mOnto, outClass, "out6b");
			OWLIndividual in6 = addIndividual(mOnto, inClass, "in6");
			OWLIndividual switch6 = addIndividual(mOnto, switchClass, "switch6");
			
			// we must create dummy objects to bind free variables
			addIndividual(mOnto, dummyClass, "dummy6");
			
			setObjectProperty(mOnto, switch6, portsProperty, out6a);
			setObjectProperty(mOnto, switch6, portsProperty, out6b);
			setObjectProperty(mOnto, switch6, portsProperty, in6);

			assertObjectPropertyValue(mOnto, "switch6", "ports", "out6a");
			assertObjectPropertyValue(mOnto, "switch6", "ports", "out6b");
			assertObjectPropertyValue(mOnto, "switch6", "ports", "in6");
			//assertDataPropertyValue(mOnto, "transition3", "hasMultipleOutgoingArcs", true);

			assertIsInstance(mOnto, outClass, out6a);
			assertIsInstance(mOnto, outClass, out6b);
			assertIsInstance(mOnto, inClass, in6);
			assertIsInstance(mOnto, switchClass, switch6);

			assertIsInstance(mOnto, placeClass, out6a);
			assertIsInstance(mOnto, placeClass, out6b);
			assertIsInstance(mOnto, transitionClass, in6);
			assertIsInstance(mOnto, arcClass, switch6);
		}

		{
			// test switch,inA,inB,out -> arcA,arcB,placeA,placeB,transition 
			// (switch type two)
			OWLIndividual in7a = addIndividual(mOnto, inClass, "in7a");
			OWLIndividual in7b = addIndividual(mOnto, inClass, "in7b");
			OWLIndividual out7 = addIndividual(mOnto, outClass, "out7");
			OWLIndividual switch7 = addIndividual(mOnto, switchClass, "switch7");
			
			// we must create dummy objects to bind free variables
			addIndividual(mOnto, dummyClass, "dummy7");
			
			setObjectProperty(mOnto, switch7, portsProperty, in7a);
			setObjectProperty(mOnto, switch7, portsProperty, in7b);
			setObjectProperty(mOnto, switch7, portsProperty, out7);

			assertObjectPropertyValue(mOnto, "switch7", "ports", "in7a");
			assertObjectPropertyValue(mOnto, "switch7", "ports", "in7b");
			assertObjectPropertyValue(mOnto, "switch7", "ports", "out7");

			assertIsInstance(mOnto, inClass, in7a);
			assertIsInstance(mOnto, inClass, in7b);
			assertIsInstance(mOnto, outClass, out7);
			assertIsInstance(mOnto, switchClass, switch7);

			assertIsInstance(mOnto, placeClass, out7);
			assertTrue(
				isInstanceOf(mOnto, transitionClass, in7a) ||
				isInstanceOf(mOnto, transitionClass, in7b)
			);
			assertIsInstance(mOnto, arcClass, switch7);
			// check references
			assertTrue(
				hasObjectPropertyValue(mOnto, "switch7", "source", "in7a") ||
				hasObjectPropertyValue(mOnto, "switch7", "source", "in7b")
			);
			assertObjectPropertyValue(mOnto, "switch7", "target", "out7");
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
        assertIsInstance(mOnto, sectionClass, package1);
        // check sectionName of package1
        assertDataPropertyValue(mOnto, "package1", "packageName", "p1");
        assertDataPropertyValue(mOnto, "package1", "sectionName", "prefix_p1");
        
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

        assertIsInstance(mOnto, packageClass, section1);
        assertDataPropertyValue(mOnto, "section1", "packageName", "s1");
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
		assertIsInstance(mOnto, fieldClass, field1Object);
		assertNotIsInstance(mOnto, methodClass, field1Object);
		assertIsInstance(mOnto, basicEntryClass, field1Object);

		{
			// add a full entry and see what it corresponds to
	        OWLIndividual entry1 = addIndividual(mOnto, fullEntryClass, "entry1");
			setDataProperty(mOnto, entry1, "isBold", false);
			setDataProperty(mOnto, entry1, "isItalic", false);
			assertIsInstance(mOnto, fullEntryClass, entry1);
			assertDataPropertyValue(mOnto, "entry1", "isAbstract", false);
			assertIsInstance(mOnto, methodClass, entry1);
			assertNotIsInstance(mOnto, fieldClass, entry1);
		}

		{
			// make basic entry italic - result: method gets abstract
	        OWLIndividual entry2 = addIndividual(mOnto, basicEntryClass, "entry2");
			setDataProperty(mOnto, entry2, "isBold", false);
			setDataProperty(mOnto, entry2, "isItalic", true);
			
			assertIsInstance(mOnto, basicEntryClass, entry2);
			assertIsInstance(mOnto, methodClass, entry2);
			assertDataPropertyValue(mOnto, "entry2", "isAbstract", true);
			assertNotIsInstance(mOnto, fieldClass, entry2);
		}

		{
			// make basic entry bold - result: concrete field
	        OWLIndividual entry3 = addIndividual(mOnto, basicEntryClass, "entry3");
			setDataProperty(mOnto, entry3, "isBold", true);
			setDataProperty(mOnto, entry3, "isItalic", false);

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
        OWLIndividual enumObject = addIndividual(mOnto, enumClass, "enum1");
        assertNotIsInstance(mOnto, entityClass, enumObject);
		assertIsInstance(mOnto, paragraphClass, enumObject);
		assertIsInstance(mOnto, enumClass, enumObject);
		assertNotIsInstance(mOnto, otherClass, enumObject);

		// add an instance of Paragraph and check whether it is recognized as instance
		// of Entity or Enum too
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
}
