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
package org.emftext.language.owlcl.resource.owlcl.analysis;

public class DataPropertyEquivalentPropertiesReferenceResolver implements org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolver<org.emftext.language.owl.DataProperty, org.emftext.language.owl.DataProperty> {

	private org.emftext.language.owl.resource.owl.analysis.DataPropertyEquivalentPropertiesReferenceResolver delegate = new org.emftext.language.owl.resource.owl.analysis.DataPropertyEquivalentPropertiesReferenceResolver();

	public void resolve(String identifier, org.emftext.language.owl.DataProperty container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult<org.emftext.language.owl.DataProperty> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, new org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<org.emftext.language.owl.DataProperty>() {

			public boolean wasResolvedUniquely() {
				return result.wasResolvedUniquely();
			}

			public boolean wasResolvedMultiple() {
				return result.wasResolvedMultiple();
			}

			public boolean wasResolved() {
				return result.wasResolved();
			}

			public void setErrorMessage(String message) {
				result.setErrorMessage(message);
			}

			public java.util.Collection<org.emftext.language.owl.resource.owl.IOwlReferenceMapping<org.emftext.language.owl.DataProperty>> getMappings() {
				throw new UnsupportedOperationException();
			}

			public String getErrorMessage() {
				return result.getErrorMessage();
			}

			public void addMapping(String identifier, org.eclipse.emf.common.util.URI newIdentifier) {
				result.addMapping(identifier, newIdentifier);
			}

			public void addMapping(String identifier, org.eclipse.emf.common.util.URI newIdentifier, String warning) {
				result.addMapping(identifier, newIdentifier, warning);
			}

			public void addMapping(String identifier, org.emftext.language.owl.DataProperty target) {
				result.addMapping(identifier, target);
			}

			public void addMapping(String identifier, org.emftext.language.owl.DataProperty target, String warning) {
				result.addMapping(identifier, target, warning);
			}

			public java.util.Collection<org.emftext.language.owl.resource.owl.IOwlQuickFix> getQuickFixes() {
				return java.util.Collections.emptySet();
			}

			public void addQuickFix(final org.emftext.language.owl.resource.owl.IOwlQuickFix quickFix) {
				result.addQuickFix(new org.emftext.language.owlcl.resource.owlcl.IOwlclQuickFix() {

					public String getImageKey() {
						return quickFix.getImageKey();
					}

					public String getDisplayString() {
						return quickFix.getDisplayString();
					}

					public java.util.Collection<org.eclipse.emf.ecore.EObject> getContextObjects() {
						return quickFix.getContextObjects();
					}

					public String getContextAsString() {
						return quickFix.getContextAsString();
					}

					public String apply(String currentText) {
						return quickFix.apply(currentText);
					}
				});
			}
		});

	}

	public String deResolve(org.emftext.language.owl.DataProperty element, org.emftext.language.owl.DataProperty container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}

	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}

}
