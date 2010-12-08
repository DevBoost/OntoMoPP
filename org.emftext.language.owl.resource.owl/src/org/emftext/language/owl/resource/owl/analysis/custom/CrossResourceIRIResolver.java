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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
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
	
	public List<Ontology> calculateTransitiveImports(Ontology o) {
		List<Ontology> transitiveImports = new ArrayList<Ontology>();
		EObject eContainer = o.eContainer();
		if (eContainer != null && eContainer instanceof OntologyDocument) {
			OntologyDocument od = (OntologyDocument) eContainer;
			EList<Ontology> imports = od.getOntology().getImports();
			for (Ontology imported : imports) {
				transitiveImports.add(imported);
				transitiveImports.addAll(calculateTransitiveImports(imported));
			}
		}
		return transitiveImports;
	}

	@SuppressWarnings("unchecked")
	public <RESULT extends IRIIdentified> void doResolve(String identifier,
			EObject containerObject, boolean resolveFuzzy,
			IOwlReferenceResolveResult<RESULT> result, Class<RESULT> c) {
		RESULT r = null;
		String iriPrefix;
		if (!hasPrefix(identifier)) {
			iriPrefix = "";
		} else {
			iriPrefix = getPrefix(identifier);
			identifier = getId(identifier);
		}
		List<IRIIdentified> entityList;
		try {
			entityList = getOntologyEntity(iriPrefix, containerObject,
					identifier, resolveFuzzy);
		} catch (OntologyLoadExeption e) {
			result.setErrorMessage(e.getMessage());
			return;
		}
		if (entityList != null && entityList.size() != 0) {
			for (IRIIdentified iriIdentified : entityList) {
				if (iriIdentified.eIsProxy()) {
					iriIdentified = (IRIIdentified) EcoreUtil.resolve(
							iriIdentified, containerObject);
				}
				if (c.isInstance(iriIdentified)) {
					r = (RESULT) iriIdentified;
					result.addMapping(iriIdentified.getIri(), r);
					if (!resolveFuzzy)
						return;
				}

			}

		}
	}

	public String getId(String identifier) {
		int startIdx = identifier.indexOf(":");
		if ((startIdx == -1))
			return "";
		else
			return identifier.substring(startIdx + 1);

	}

	public List<IRIIdentified> getOntologyEntity(String iriPrefix,
			EObject containerObject, String identifier, boolean resolveFuzzy)
			throws OntologyLoadExeption {

		EList<EObject> contents = containerObject.eResource().getContents();
		for (EObject object : contents) {
			if (object instanceof OntologyDocument) {
				List<IRIIdentified> entityList = searchOntologyEntity(
						iriPrefix, (OntologyDocument) object, identifier,
						resolveFuzzy);
				return entityList;
			}
		}
		return Collections.emptyList();
	}

	private List<IRIIdentified> searchOntologyEntity(String iriPrefix,
			OntologyDocument ontologyDocument, String identifier,
			boolean resolveFuzzy) throws OntologyLoadExeption {
		String uri = standardNamespaces.get(iriPrefix);
		if (uri != null) {
			remoteLoader.loadOntology(uri, ontologyDocument);
			List<IRIIdentified> entity = remoteLoader.getOntologyElement(
					identifier, resolveFuzzy);
			return entity;
		}
		if ("".equals(iriPrefix)) {
			return remoteLoader.getOntologyElement(identifier,
					ontologyDocument.getOntology(), resolveFuzzy);

		}
		EList<Namespace> namespaces = ontologyDocument.getNamespace();
		for (Namespace namespace : namespaces) {
			if (iriPrefix.equals(namespace.getPrefix())) {
				uri = namespace.getImportedOntology().getUri();
				if (uri == null)
					return null;
				List<IRIIdentified> entity;
				if (namespace.getImportedOntology() == null
						|| namespace.getImportedOntology().eIsProxy()) {
					remoteLoader.loadOntology(uri, ontologyDocument);
					entity = remoteLoader.getOntologyElement(identifier,
							resolveFuzzy);
				} else {
					remoteLoader.addUriMapping(uri,
							namespace.getImportedOntology());
					entity = remoteLoader.getOntologyElement(identifier,
							namespace.getImportedOntology(), resolveFuzzy);
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
		Ontology elementContainingOntology = getContainingOntology(element);
		Ontology containerContainingOntology = getContainingOntology(container);
		if (containerContainingOntology == elementContainingOntology) {
			return element.getIri();
		} else if (elementContainingOntology != null) {
			EList<Namespace> namespaces = getContainingOntologyDocument(
					container).getNamespace();
			for (Namespace namespace : namespaces) {
				if (namespace.getImportedOntology().getUri()
						.equals(elementContainingOntology.getUri())) {
					return namespace.getPrefix() + element.getIri();
				}
			}
			for (String prefix : standardNamespaces.keySet()) {
				String standardOntologyUri = standardNamespaces.get(prefix);
				if (standardOntologyUri.equals(elementContainingOntology
						.getUri())) {
					return prefix + element.getIri();
				}
			}
		}
		return element.getIri();
	}

	private OntologyDocument getContainingOntologyDocument(EObject element) {
		EObject parent = element.eContainer();
		while (parent != null && !(parent instanceof OntologyDocument)) {
			parent = parent.eContainer();
		}
		return (OntologyDocument) parent;
	}

	private Ontology getContainingOntology(EObject element) {
		EObject parent = element.eContainer();
		while (parent != null && !(parent instanceof Ontology)) {
			parent = parent.eContainer();
		}
		return (Ontology) parent;

	}

}
