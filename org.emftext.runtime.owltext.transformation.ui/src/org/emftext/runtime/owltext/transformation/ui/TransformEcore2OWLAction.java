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
package org.emftext.runtime.owltext.transformation.ui;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.runtime.owltext.OWLTextEObjectImpl;
import org.emftext.runtime.owltext.transformation.Ecore2Owl;


public class TransformEcore2OWLAction implements IObjectActionDelegate {

	private ISelection selection;

	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IFile) {
				IFile file = (IFile) first;
				Resource resource = new ResourceSetImpl().createResource(URI
						.createFileURI(file.getLocation().toOSString()));
				if (resource != null) {
					try {
						resource.load(null);
						OntologyDocument document = transform(resource
								.getContents());
						URI targetURI = resource.getURI()
								.appendFileExtension("owl");
						Resource documentResource = resource.getResourceSet()
								.createResource(targetURI);
						documentResource.getContents().add(document);
						documentResource.save(null);
						file.getParent().refreshLocal(Integer.MAX_VALUE, null);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private OntologyDocument transform(EList<EObject> contents) {
		if (contents.size() == 1) {
			EObject eObject = contents.get(0);
			if (eObject instanceof OWLTextEObjectImpl) {
				OWLTextEObjectImpl owlTextEObjectImpl = (OWLTextEObjectImpl) eObject;
				return owlTextEObjectImpl.getOWLRepresentation();
			}
		}
		OntologyDocument ontologyDocument = new Ecore2Owl().transform(contents);
		return ontologyDocument;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
}
