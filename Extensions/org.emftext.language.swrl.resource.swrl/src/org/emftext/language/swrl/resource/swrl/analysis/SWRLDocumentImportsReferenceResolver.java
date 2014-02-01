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
/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.swrl.resource.swrl.analysis;

import org.eclipse.emf.ecore.EReference;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.swrl.SWRLDocument;
import org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolveResult;
import org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolver;

public class SWRLDocumentImportsReferenceResolver implements ISwrlReferenceResolver<SWRLDocument, OntologyDocument> {
	
	private SwrlDefaultResolverDelegate<SWRLDocument, OntologyDocument> delegate = new SwrlDefaultResolverDelegate<SWRLDocument, OntologyDocument>();
	
	public void resolve(String identifier, SWRLDocument container, EReference reference, int position, boolean resolveFuzzy, final ISwrlReferenceResolveResult<OntologyDocument> result) {
		if (identifier.startsWith("<") && identifier.endsWith(">")) {
			identifier = identifier.substring(1, identifier.length() - 1);
		}
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(OntologyDocument element, SWRLDocument container, EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
