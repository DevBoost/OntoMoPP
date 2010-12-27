/*******************************************************************************
 * Copyright (c) 2006-2010 
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
package org.emftext.language.owl.resource.owl.analysis;

import java.util.ArrayList;
import java.util.List;

import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.loading.OntologyLoadExeption;
import org.emftext.language.owl.loading.RemoteLoader;
import org.emftext.language.owl.resource.owl.OwlEProblemType;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;

public class NamespaceImportedOntologyReferenceResolver
		implements
		org.emftext.language.owl.resource.owl.IOwlReferenceResolver<org.emftext.language.owl.Namespace, org.emftext.language.owl.Ontology> {

	private RemoteLoader remoteLoader = CrossResourceIRIResolver.theInstance()
			.getRemoteLoader();

	public void resolve(
			java.lang.String identifier,
			org.emftext.language.owl.Namespace container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<org.emftext.language.owl.Ontology> result) {
		OntologyDocument ontologyDocument = (OntologyDocument) container
				.eContainer();
		List<Ontology> imports = new ArrayList<Ontology>();
		imports.addAll(ontologyDocument.getOntology().getImports());
		imports.addAll(CrossResourceIRIResolver.theInstance()
				.calculateTransitiveImports(ontologyDocument.getOntology()));
		if (!identifier.endsWith("#")) {
			((OwlResource) container.eResource())
					.addWarning(
							"URIs of imported namespaces should end with \"#\", to allow for resolving its declarations by iri",
							OwlEProblemType.ANALYSIS_PROBLEM,
							container);
		}
		OntologyDocument document = (OntologyDocument) container.eContainer();
		if ((document.getOntology().getUri() + "#").equals(identifier)) {
			result.addMapping(identifier, document.getOntology());
			return;
		}
		for (Ontology ontology : imports) {

			if (identifier.equals(ontology.getUri())
					|| identifier.equals(ontology.getUri() + "#")) {
				result.addMapping(identifier, ontology);
				return;
			}
		}
		if (result.getMappings() == null || result.getMappings().isEmpty()) {
			Ontology loadedOntology;
			try {
				loadedOntology = remoteLoader.loadOntology(identifier,
						container);
			} catch (OntologyLoadExeption e) {
				result.setErrorMessage(e.getMessage());
				return;
			}
			if (loadedOntology != null) {
				ontologyDocument.getOntology().getImports().add(loadedOntology);
				result.addMapping(identifier, loadedOntology);
			}
		}
	}

	public java.lang.String deResolve(
			org.emftext.language.owl.Ontology element,
			org.emftext.language.owl.Namespace container,
			org.eclipse.emf.ecore.EReference reference) {
		String uri = element.getUri();

		if (!uri.endsWith("#")) {
			uri = uri.substring(0, uri.length()) + "#";
		}
		if (!uri.startsWith("<") && !uri.endsWith(">"))
			uri = "<" + uri + ">";
		return uri;
	}

	public void setOptions(java.util.Map<?, ?> options) {
		// TODO save options in a field or leave method empty if this resolver
		// does not depend on any option
	}

}
