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
package org.owltext.language.petrinets.resource.petrinets.analysis;

public class ArcInReferenceResolver implements org.owltext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolver<org.owltext.language.petrinets.Arc, org.owltext.language.petrinets.Component> {
	
	private org.owltext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.owltext.language.petrinets.Arc, org.owltext.language.petrinets.Component> delegate = new org.owltext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.owltext.language.petrinets.Arc, org.owltext.language.petrinets.Component>();
	
	public void resolve(String identifier, org.owltext.language.petrinets.Arc container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.owltext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult<org.owltext.language.petrinets.Component> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(org.owltext.language.petrinets.Component element, org.owltext.language.petrinets.Arc container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
