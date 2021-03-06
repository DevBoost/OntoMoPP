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

import java.util.Map;

import org.eclipse.emf.ecore.EReference;
import org.emftext.language.owl.Feature;
import org.emftext.language.swrl.PropertyAtom;
import org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolveResult;
import org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolver;

public class PropertyAtomPropertyReferenceResolver implements ISwrlReferenceResolver<PropertyAtom, Feature> {
	
	private SwrlDefaultResolverDelegate<PropertyAtom, Feature> delegate = new SwrlDefaultResolverDelegate<PropertyAtom, Feature>();
	
	public void resolve(String identifier, PropertyAtom container, EReference reference, int position, boolean resolveFuzzy, final ISwrlReferenceResolveResult<Feature> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(Feature element, PropertyAtom container, EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(Map<?,?> options) {
		// no needed
	}
	
}
