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
package org.emftext.language.owl.reasoning;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.resource.owl.IOwlBuilder;
import org.emftext.language.owl.resource.owl.IOwlProblem;
import org.emftext.language.owl.resource.owl.IOwlQuickFix;
import org.emftext.language.owl.resource.owl.IOwlTextDiagnostic;
import org.emftext.language.owl.resource.owl.OwlEProblemSeverity;
import org.emftext.language.owl.resource.owl.OwlEProblemType;
import org.emftext.language.owl.resource.owl.mopp.OwlBuilderAdapter;
import org.emftext.language.owl.resource.owl.mopp.OwlMarkerHelper;
import org.emftext.language.owl.resource.owl.mopp.OwlPlugin;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.util.OwlStreamUtil;

public class OwlReasoningBuilder extends OwlBuilderAdapter implements
		IOwlBuilder {

	private EMFTextOWLReasoner reasoner;

	public OwlReasoningBuilder() {
		super();
		this.reasoner = new EMFTextPelletReasoner();
	}

	private void mark(OwlResource resource) {
		mark(resource, resource.getErrors());
		mark(resource, resource.getWarnings());
	}

	private void mark(OwlResource resource, EList<Diagnostic> diagnostics) {
		for (Diagnostic diagnostic : diagnostics) {
			if (diagnostic instanceof IOwlTextDiagnostic) {
				new OwlMarkerHelper().mark(resource,
						(IOwlTextDiagnostic) diagnostic);
			}
		}
	}

	public void validateOWL(String content, OwlResource resource) {

		try {
			Map<String, String> inconsistentOWLObjects;
			// System.out.println(content);
			try {
				inconsistentOWLObjects = reasoner
						.getInconsistentFrames(content);
			} catch (final Exception e) {
				resource.addProblem(new IOwlProblem() {

					public OwlEProblemType getType() {
						return OwlEProblemType.BUILDER_ERROR;
					}

					public String getMessage() {
						return e.getMessage();
					}

					public Collection<IOwlQuickFix> getQuickFixes() {
						return null;
					}

					public OwlEProblemSeverity getSeverity() {
						return OwlEProblemSeverity.ERROR;
					}

				}, resource.getContents().get(0));

				return;
			}
			TreeIterator<EObject> allContents = resource.getAllContents();
			while (allContents.hasNext()) {
				EObject next = allContents.next();
				if (next instanceof IRIIdentified) {
					final IRIIdentified c = ((IRIIdentified) next);
					final String error = inconsistentOWLObjects.get(c.getIri());
					if (error != null) {
						resource.addProblem(new IOwlProblem() {

							public OwlEProblemType getType() {
								return OwlEProblemType.BUILDER_ERROR;
							}

							public String getMessage() {
								return " '" + c.getIri()
										+ "' is inconsistent: " + error;
							}

							public Collection<IOwlQuickFix> getQuickFixes() {
								return null;
							}

							public OwlEProblemSeverity getSeverity() {
								return OwlEProblemSeverity.ERROR;
							}

						}, next);
					}
				}
			}
		} catch (Exception e) {
			OwlPlugin.logError("Exception while reasoning over OWL file.", e);
		}
	}

	public boolean isBuildingNeeded(URI uri) {
		return true;
	}

	public IStatus handleDeletion(URI uri, IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

	public IOwlBuilder getBuilder() {
		return OwlReasoningBuilder.this;
	}

	public IStatus build(OwlResource resource, IProgressMonitor monitor) {
		new OwlMarkerHelper().unmark(resource, OwlEProblemType.BUILDER_ERROR);
		IFile file = WorkspaceSynchronizer.getFile(resource);
		InputStream stream;
		try {
			stream = file.getContents();
			String content = OwlStreamUtil.getContent(stream);
			validateOWL(content, resource);
			mark(resource);
		} catch (CoreException e) {
			OwlPlugin.logError("Exception while reasoning over OWL file.", e);
		} catch (IOException e) {
			OwlPlugin.logError("Exception while reasoning over OWL file.", e);
		}
		return Status.OK_STATUS;
	}

}
