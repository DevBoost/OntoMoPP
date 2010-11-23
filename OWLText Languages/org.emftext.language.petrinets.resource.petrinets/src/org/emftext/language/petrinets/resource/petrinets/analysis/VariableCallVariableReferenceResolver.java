/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.petrinets.Arc;
import org.emftext.language.petrinets.ArcStatement;
import org.emftext.language.petrinets.Place;
import org.emftext.language.petrinets.Transition;
import org.emftext.language.petrinets.Variable;
import org.emftext.language.petrinets.VariableCall;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult;

public class VariableCallVariableReferenceResolver
		implements
		org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolver<org.emftext.language.petrinets.VariableCall, org.emftext.language.petrinets.Variable> {

	private org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.VariableCall, org.emftext.language.petrinets.Variable> delegate = new org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.VariableCall, org.emftext.language.petrinets.Variable>();

	public void resolve(
			String identifier,
			org.emftext.language.petrinets.VariableCall container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult<org.emftext.language.petrinets.Variable> result) {
		List<Variable> candidates = new ArrayList<Variable>();
		Arc arc = getContainingArc(container);
		if (arc.getIn() instanceof Transition) {
			EList<Arc> consumingArcs = ((Transition) arc.getIn()).getIncoming();
			for (Arc consuming : consumingArcs) {
				candidates.addAll(collectVariables(consuming));
			}
		}
		FilterCandidates(candidates, identifier, resolveFuzzy, result);
	}

	private void FilterCandidates(List<Variable> candidates, String identifier,
			boolean resolveFuzzy,
			IPetrinetsReferenceResolveResult<Variable> result) {
		for (Variable candidate : candidates) {
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

	private List<Variable> collectVariables(Arc consuming) {
		List<Variable> vs = new ArrayList<Variable>();
		if (consuming.getIn() instanceof Place) {
			EList<ArcStatement> arcStatements = consuming.getArcStatements();
			for (ArcStatement arcStatement : arcStatements) {
				if (arcStatement instanceof Variable) {
					vs.add((Variable) arcStatement);
				}
			}
		}
		return vs;
	}

	private Arc getContainingArc(VariableCall container) {
		Arc containing = null;
		EObject c = container.eContainer();
		while (!(c instanceof Arc) && c != null) {
			c = c.eContainer();
		}
		if (c != null) {
			containing = (Arc) c;
		}
		return containing;
	}

	public String deResolve(org.emftext.language.petrinets.Variable element,
			org.emftext.language.petrinets.VariableCall container,
			org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}

	public void setOptions(java.util.Map<?, ?> options) {
		// save options in a field or leave method empty if this resolver does
		// not depend
		// on any option
	}

}
