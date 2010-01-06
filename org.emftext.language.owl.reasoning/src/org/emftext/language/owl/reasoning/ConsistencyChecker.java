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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.owl.Frame;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.loading.OntologyLoadExeption;
import org.emftext.language.owl.resource.owl.IOwlOptionProvider;
import org.emftext.language.owl.resource.owl.IOwlOptions;
import org.emftext.language.owl.resource.owl.IOwlResourcePostProcessor;
import org.emftext.language.owl.resource.owl.IOwlResourcePostProcessorProvider;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;

public class ConsistencyChecker implements IOwlResourcePostProcessor,
		IOwlResourcePostProcessorProvider, IOwlOptionProvider {

	public Map<?, ?> getOptions() {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(IOwlOptions.RESOURCE_POSTPROCESSOR_PROVIDER,
				new ConsistencyChecker());
		return options;
	}

	public ConsistencyChecker() {
		super();
	}

	public void process(OwlResource resource) {
		checkImportedElements(resource);
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
					IRIIdentified entity;
					try {
						entity = iriResolver.getOntologyEntity(
								prefix, od, iriResolver.getId(iri));
						if(entity == null) {
							resource.addWarning("The referenced iri-identified element could not be resolved in the imported ontology", frame);
						}
					} catch (OntologyLoadExeption e) {
						resource.addWarning(e.getMessage(), frame);	
					}
					
				}

			}
		}
	}

	public IOwlResourcePostProcessor getResourcePostProcessor() {
		return new ConsistencyChecker();
	}

}
