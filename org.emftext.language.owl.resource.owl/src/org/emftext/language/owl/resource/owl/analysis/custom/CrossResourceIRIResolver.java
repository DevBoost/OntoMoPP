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
package org.emftext.language.owl.resource.owl.analysis.custom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.Namespace;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.loading.OntologyLoadExeption;
import org.emftext.language.owl.loading.RemoteLoader;
import org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult;

public class CrossResourceIRIResolver {

	private static CrossResourceIRIResolver instance;
	static public Map<String, String> standardNamespaces = new HashMap<String, String>();
	static {
		standardNamespaces.put("rdf:",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		standardNamespaces
				.put("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
		standardNamespaces.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
		standardNamespaces.put("owl:", "http://www.w3.org/2002/07/owl#");

	}

	private RemoteLoader remoteLoader;

	private CrossResourceIRIResolver() {
		remoteLoader = new RemoteLoader();
	}

	public static CrossResourceIRIResolver theInstance() {
		if (instance == null) {
			instance = new CrossResourceIRIResolver();
		}
		return instance;
	}

	public <RESULT extends IRIIdentified> void doResolve(String identifier,
			EObject containerObject, boolean resolveFuzzy,
			IOwlReferenceResolveResult<RESULT> result, Class<RESULT> c) {
		RESULT r = null;
		String iriPrefix;
		if (!hasPrefix(identifier)) {
			iriPrefix = "";
		}
		else {
			iriPrefix = getPrefix(identifier);
			identifier = getId(identifier);
		}
		IRIIdentified entity;
		try {
			entity = getOntologyEntity(iriPrefix, containerObject, identifier);
		} catch (OntologyLoadExeption e) {
			result.setErrorMessage(e.getMessage());
			return;
		}
		if (entity != null) {
			if (entity.eIsProxy()) {
				entity = (IRIIdentified) EcoreUtil.resolve(entity,
						containerObject);
			}
			r = (RESULT) entity;
			result.addMapping(entity.getIri(), r);

		}
	}

	public String getId(String identifier) {
		int startIdx = identifier.indexOf(":");
		if ((startIdx == -1))
			return "";
		else
			return identifier.substring(startIdx + 1);

	}

	public IRIIdentified getOntologyEntity(String iriPrefix,
			EObject containerObject, String identifier)
			throws OntologyLoadExeption {

		EList<EObject> contents = containerObject.eResource().getContents();
		for (EObject object : contents) {
			if (object instanceof OntologyDocument) {
				IRIIdentified entity = searchOntologyEntity(iriPrefix,
						(OntologyDocument) object, identifier);
				return entity;
			}
		}
		return null;
	}

	private IRIIdentified searchOntologyEntity(String iriPrefix,
			OntologyDocument ontologyDocument, String identifier)
			throws OntologyLoadExeption {
		String uri = standardNamespaces.get(iriPrefix);
		if (uri != null) {
			remoteLoader.loadOntology(uri, ontologyDocument);
			IRIIdentified entity = remoteLoader.getOntologyElement(identifier);
			return entity;
		}

		EList<Namespace> namespaces = ontologyDocument.getNamespace();
		for (Namespace namespace : namespaces) {
			if (iriPrefix.equals(namespace.getPrefix())) {
				uri = namespace.getImportedOntology().getUri();
				if (uri == null)
					return null;
				IRIIdentified entity;
				if (namespace.getImportedOntology() == null || namespace.getImportedOntology().eIsProxy()) {
					remoteLoader.loadOntology(uri, ontologyDocument);
					entity = remoteLoader
					.getOntologyElement(identifier);
					
				} else {
					remoteLoader.addUriMapping(uri, namespace.getImportedOntology());
					entity = remoteLoader
					.getOntologyElement(identifier, namespace.getImportedOntology());
				}
			
				return entity;
			}
		}
		return null;
	}

	public String getPrefix(String identifier) {
		int startIdx = identifier.indexOf(":");
		if ((startIdx == -1))
			return "";
		else
			return identifier.substring(0, startIdx + 1);
	}

	public boolean hasPrefix(String identifier) {
		return (!(identifier.indexOf(":") == -1));
	}

	public RemoteLoader getRemoteLoader() {
		return this.remoteLoader;
	}

	public String deResolve(IRIIdentified element, EObject container,
			EStructuralFeature reference) {
		if (element.eIsProxy()) {
			java.lang.String fragment = ((org.eclipse.emf.ecore.InternalEObject) element)
					.eProxyURI().fragment();
			if (fragment != null
					&& fragment
							.startsWith(org.emftext.language.owl.resource.owl.IOwlContextDependentURIFragment.INTERNAL_URI_FRAGMENT_PREFIX)) {
				fragment = fragment
						.substring(org.emftext.language.owl.resource.owl.IOwlContextDependentURIFragment.INTERNAL_URI_FRAGMENT_PREFIX
								.length());
				fragment = fragment.substring(fragment.indexOf("_") + 1);
			}
			return fragment;
		}
		Ontology containingOntologyElement = getContainingOntology(element);
		Ontology containingOntologyContainer = getContainingOntology(container);
		if (containingOntologyContainer == containingOntologyElement) {
			return element.getIri();
		} else if (containingOntologyElement != null) {
			EList<Namespace> namespaces = ((OntologyDocument) containingOntologyContainer
					.eContainer()).getNamespace();
			for (Namespace namespace : namespaces) {
				if (namespace.getImportedOntology().getUri().equals(
						containingOntologyElement.getUri())) {
					return namespace.getPrefix() + element.getIri();
				}
			}
			for (String prefix : standardNamespaces.keySet()) {
				String standardOntologyUri = standardNamespaces.get(prefix);
				if (standardOntologyUri.equals(containingOntologyElement
						.getUri())) {
					return prefix + element.getIri();
				}
			}
		}
		return element.getIri();
	}

	private Ontology getContainingOntology(EObject element) {
		Resource eResource = element.eResource();
		EObject parent = element.eContainer();
		while (parent != null && !(parent instanceof Ontology)) {
			parent = parent.eContainer();
		}
		return (Ontology) parent;

	}

}
