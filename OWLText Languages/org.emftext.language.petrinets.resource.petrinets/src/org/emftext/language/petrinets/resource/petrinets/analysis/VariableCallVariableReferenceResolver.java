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
import org.emftext.language.petrinets.ConsumingArc;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.InitialisedVariable;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.ProducingArc;
import org.emftext.language.petrinets.Statement;
import org.emftext.language.petrinets.Transition;
import org.emftext.language.petrinets.Variable;
import org.emftext.language.petrinets.VariableCall;
import org.emftext.language.petrinets.impl.PetrinetsFactoryImpl;
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
		List<Variable> candidates = collectCandidates(container, resolveFuzzy);
		FilterCandidates(candidates, identifier, resolveFuzzy, result,
				container);
	}

	private List<Variable> collectCandidates(
			org.emftext.language.petrinets.VariableCall container,
			boolean resolveFuzzy) {
		List<Variable> candidates = new ArrayList<Variable>();
		EList<ConsumingArc> arc = getConsumingArcs(container);
		for (ConsumingArc consuming : arc) {
			candidates.add(consuming.getFreeVariable());
		}

		List<Statement> previousStatements = getPreviousStatements(container);
		for (Statement arcStatement : previousStatements) {
			if (arcStatement instanceof Variable) {
				candidates.add((Variable) arcStatement);
			}
		}
		
		return candidates;
	}

	private void FilterCandidates(List<Variable> candidates, String identifier,
			boolean resolveFuzzy,
			IPetrinetsReferenceResolveResult<Variable> result,
			VariableCall container) {
		for (Variable candidate : candidates) {
			if (resolveFuzzy) {
				result.addMapping(candidate.getName(), candidate);
			} else {
				if (candidate.getName().equals(identifier)) {
					result.addMapping(identifier, candidate);
					container.setType(candidate.getType());

					return;
				}
			}
		}
		return;
	}

	private EList<ConsumingArc> getConsumingArcs(VariableCall container) {
		EObject c = container.eContainer();
		while (!(c instanceof ProducingArc) && !(c instanceof Transition)
				&& c != null) {
			c = c.eContainer();
		}
		if (c != null) {
			if (c instanceof Transition) {
				Transition t = (Transition) c;
				return t.getIncoming();
			}
			if (c instanceof ProducingArc) {
				ProducingArc producing = (ProducingArc) c;
				Transition t = producing.getIn();
				return t.getIncoming();
			}

		}
		return null;
	}

	private List<Statement> getPreviousStatements(VariableCall call) {
		List<Statement> previousStatements = new ArrayList<Statement>();
		EObject c = call.eContainer();
		EObject containingStatement = call;
		while (!(c instanceof Transition) && !(c instanceof ProducingArc)
				&& c != null) {
			containingStatement = c;
			c = c.eContainer();
		}
		if (c != null && c instanceof Transition) {
			Transition containing = (Transition) c;
			int indexOfContainingStatement = containing.getStatements()
					.indexOf(containingStatement);
			if (indexOfContainingStatement > 0) previousStatements = containing.getStatements().subList(0,
					indexOfContainingStatement);
		}
		if (c != null && c instanceof ProducingArc) {
			ProducingArc pa = (ProducingArc) c;
			Transition in = pa.getIn();
			previousStatements = in.getStatements();
		}
		return previousStatements;
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
