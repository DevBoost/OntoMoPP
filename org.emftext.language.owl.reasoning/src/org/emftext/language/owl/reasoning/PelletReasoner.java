/*******************************************************************************
 * Copyright (c) 2006-2010 
 * Software Technology Group, Dresden University of Technology
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.owl.reasoning;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxParserFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLParser;
import org.semanticweb.owl.io.OWLParserFactoryRegistry;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyURIMapper;

import com.clarkparsia.explanation.PelletExplanation;
import com.clarkparsia.explanation.io.manchester.BlockWriter;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.explanation.io.manchester.TextBlockWriter;

public class PelletReasoner implements org.emftext.language.owl.reasoning.EMFTextOWLReasoner {

	public Set<OWLClass> getInconsistentClasses(String owlRepresentation) throws ReasoningException {

		Set<OWLClass> inconsistentClasses = new HashSet<OWLClass>();
		
		
		try {
			// prepare infrastructure
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			ManchesterOWLSyntaxParserFactory f = new ManchesterOWLSyntaxParserFactory();
			OWLParserFactoryRegistry.getInstance().registerParserFactory(f);

			OWLParser parser = f.createParser(manager);
			
			// load and parse ontology in manchester syntax
			StringInputSource inputSource = new StringInputSource(owlRepresentation);
			OWLOntology ontology = manager.createOntology(URI.create("check"));
			manager.addURIMapper(new OWLOntologyURIMapper() {
				
				public URI getPhysicalURI(URI logicalUri) {
					if (logicalUri.toString().startsWith("http")) return null;
					org.eclipse.emf.common.util.URI localURI = org.eclipse.emf.common.util.URI.createURI(logicalUri.toString());
					String platformResourcePath = localURI.toPlatformString(true);
					
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformResourcePath));
					String absoluteLocation = file.getLocation().toFile().toURI().toString();
					URI absoluteURI = URI.create(absoluteLocation);
					System.out.println("Requested: " + logicalUri + " - " + absoluteURI);
					
					return absoluteURI;
				}
			});
			parser.setOWLOntologyManager(manager);
			parser.parse(inputSource, ontology);
			
			// load ontology in pellet 
			Reasoner reasoner = new Reasoner(manager);
			Set<OWLOntology> importsClosure = manager
					.getImportsClosure(ontology);
			reasoner.loadOntologies(importsClosure);
			
			// derive inconsistent classes
			if (!reasoner.isConsistent()) {
				
			
//
//				PelletExplanation pe = new PelletExplanation(reasoner);
//				Set<Set<OWLAxiom>> inconsistencyExplanations = pe.getInconsistencyExplanations();
//				
//				for (Set<OWLAxiom> set : inconsistencyExplanations) {
//					
//				}
//			
			}
			else {
			//reasoner.realise();
			//reasoner.classify();
			//reasoner.getKB().ensureConsistency();
			inconsistentClasses = reasoner
					.getInconsistentClasses();
			}
			return inconsistentClasses;

		} catch (OWLOntologyCreationException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		} catch (InternalReasonerException e) {
			String message = "The ontology could not be checked for consistency: "+ e.getMessage();
			throw new ReasoningException(message, e);
		} 
//			catch (OWLReasonerException e) {
//			String message = "The ontology could not be checked for consistency: "+ e.getMessage();
//			throw new ReasoningException(message, e);
//		}
	

	}
	
}
