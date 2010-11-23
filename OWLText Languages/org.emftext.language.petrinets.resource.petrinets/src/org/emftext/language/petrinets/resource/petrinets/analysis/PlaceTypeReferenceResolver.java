/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

public class PlaceTypeReferenceResolver implements org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolver<org.emftext.language.petrinets.Place, org.eclipse.emf.ecore.EClassifier> {
	
	private org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.Place, org.eclipse.emf.ecore.EClassifier> delegate = new org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.Place, org.eclipse.emf.ecore.EClassifier>();
	private ArrayList<EClassifier> candidateList;
	private boolean resolveFuzzy;
	
	public void resolve(String identifier, org.emftext.language.petrinets.Place container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult<org.eclipse.emf.ecore.EClassifier> result) {
		EList<EPackage> imports = container.getNet().getImports();
		candidateList = new ArrayList<EClassifier>();
		
		for (EPackage ePackage : imports) {
			addCandidates(ePackage);
		}
		for (EClassifier candidate : candidateList) {
			if (resolveFuzzy) {
					result.addMapping(candidate.getName(), candidate);
			} else {
				if (candidate.getName().equals(identifier)) {
					result.addMapping(identifier, candidate);
					return;
				}
			}
		}
	}
	
	private void addCandidates(EPackage ePackage) {
		addCandidates(ePackage.getEClassifiers());
		EList<EPackage> eSubpackages = ePackage.getESubpackages();
		for (EPackage subpackage : eSubpackages) {
			addCandidates(subpackage);
		}
	}

	private void addCandidates(EList<EClassifier> eClassifiers) {
		candidateList.addAll(eClassifiers);
	}

	public String deResolve(org.eclipse.emf.ecore.EClassifier element, org.emftext.language.petrinets.Place container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
