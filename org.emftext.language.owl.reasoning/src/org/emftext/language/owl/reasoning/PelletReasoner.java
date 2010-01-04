/*******************************************************************************
 * Copyright (c) 2006-2009 
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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxParserFactory;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.mindswap.pellet.exceptions.InternalReasonerException;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.io.OWLParser;
import org.semanticweb.owl.io.OWLParserFactoryRegistry;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.MissingImportEvent;
import org.semanticweb.owl.model.MissingImportListener;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyURIMapper;

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
			OWLOntologyInputSource inputSource = new StringInputSource(owlRepresentation);
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
			reasoner.realise();
			reasoner.classify();
			inconsistentClasses = reasoner
					.getInconsistentClasses();
			return inconsistentClasses;

		} catch (OWLOntologyCreationException e) {
			String message = "The ontology could not be checked for consistency: "
					+ e.getMessage();
			throw new ReasoningException(message, e);
		} catch (InternalReasonerException e) {
			String message = "The ontology could not be checked for consistency: "+ e.getMessage();
			throw new ReasoningException(message, e);
		} 
			catch (OWLReasonerException e) {
			String message = "The ontology could not be checked for consistency: "+ e.getMessage();
			throw new ReasoningException(message, e);
		}
	

	}
	
}
