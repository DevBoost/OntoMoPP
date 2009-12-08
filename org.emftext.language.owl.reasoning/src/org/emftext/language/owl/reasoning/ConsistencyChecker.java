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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.loading.RemoteLoader;
import org.emftext.language.owl.resource.owl.IOwlOptionProvider;
import org.emftext.language.owl.resource.owl.IOwlOptions;
import org.emftext.language.owl.resource.owl.IOwlProblem;
import org.emftext.language.owl.resource.owl.IOwlResourcePostProcessor;
import org.emftext.language.owl.resource.owl.IOwlResourcePostProcessorProvider;
import org.emftext.language.owl.resource.owl.OwlEProblemType;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.semanticweb.owl.model.OWLClass;

public class ConsistencyChecker implements IOwlResourcePostProcessor,
		IOwlResourcePostProcessorProvider, IOwlOptionProvider {

	private EMFTextOWLReasoner reasoner;

	public Map<?, ?> getOptions() {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(IOwlOptions.RESOURCE_POSTPROCESSOR_PROVIDER,
				new ConsistencyChecker());
		return options;
	}

	public ConsistencyChecker() {
		this.reasoner = new PelletReasoner();

	}

	public void process(OwlResource resource) {
		checkImportedElements(resource);
		IFile file = WorkspaceSynchronizer.getFile(resource);
		try {
			InputStream stream = file.getContents();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(stream));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			String content = sb.toString();
			Set<OWLClass> inconsistentClasses;
			System.out.println(content);
			try {
				inconsistentClasses = reasoner.getInconsistentClasses(content);
			} catch (final ReasoningException e) {
				resource.addProblem(new IOwlProblem() {

					public OwlEProblemType getType() {
						return OwlEProblemType.ERROR;
					}

					public String getMessage() {
						return e.getMessage();
					}
				}, resource.getContents().get(0));

				return;
			}
			Set<String> invalidIris = new HashSet<String>();
			for (OWLClass class1 : inconsistentClasses) {
				invalidIris.add(class1.getURI().getFragment());
			}
			TreeIterator<EObject> allContents = resource.getAllContents();
			while (allContents.hasNext()) {
				EObject next = allContents.next();
				if (next instanceof Class) {
					final Class c = ((Class) next);
					if (invalidIris.contains(c.getIri())) {
						resource.addProblem(new IOwlProblem() {

							public OwlEProblemType getType() {
								return OwlEProblemType.ERROR;
							}

							public String getMessage() {
								return "The class '" + c.getIri()
										+ "' is inconsistent.";
							}
						}, next);
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkImportedElements(OwlResource resource) {
		EObject root = resource.getContents().get(0);
		if (root instanceof OntologyDocument) {
			OntologyDocument od = (OntologyDocument) root;
			CrossResourceIRIResolver iriResolver = CrossResourceIRIResolver
					.theInstance();
			EList<Frame> frames = od.getOntology().getFrames();
			for (Frame frame : frames) {
				String iri = frame.getIri();
				if (iriResolver.hasPrefix(frame.getIri())) {
					String prefix = iriResolver.getPrefix(iri);
					IRIIdentified entity = iriResolver.getOntologyEntity(
							prefix, od, iriResolver.getId(iri));
					if(entity == null) {
						resource.addWarning("The referenced iri-identified element could not be resolved in the imported ontology", frame);
					}
				}

			}
		}
	}

	public IOwlResourcePostProcessor getResourcePostProcessor() {
		return new ConsistencyChecker();
	}

}
