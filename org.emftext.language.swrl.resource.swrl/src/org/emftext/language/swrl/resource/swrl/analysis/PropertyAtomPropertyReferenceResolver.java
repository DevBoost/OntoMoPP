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
