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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.petrinets.Arc;
import org.emftext.language.petrinets.ArcStatement;
import org.emftext.language.petrinets.Component;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.PetriNet;
import org.emftext.language.petrinets.Place;
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
		Arc arc = getContainingArc(container);
		// outgoing arc
		if (arc.getIn() instanceof Transition) {
			// Variable outToken =
			// PetrinetsFactoryImpl.eINSTANCE.createVariable();
			// outToken.setName("outToken");
			// outToken.setType(((Place) arc.getOut()).getType());
			// candidates.add(outToken );

			EList<Arc> consumingArcs = ((Transition) arc.getIn()).getIncoming();
			if (consumingArcs.isEmpty()) {
				resolveRequired(arc);
				consumingArcs = ((Transition) arc.getIn()).getIncoming();
			}
			for (Arc consuming : consumingArcs) {
				candidates.addAll(collectVariables(consuming));
			}
		}
		if (arc.getOut() instanceof Transition) {
			Variable inToken = PetrinetsFactoryImpl.eINSTANCE.createVariable();
			inToken.setName("inToken");
			inToken.setType(((Place) arc.getIn()).getType());
			candidates.add(inToken);
		}
		List<ArcStatement> previousStatements = getPreviousStatements(container);	
		for (ArcStatement arcStatement : previousStatements) {
			if (arcStatement instanceof Variable) {
				candidates.add((Variable) arcStatement);
			}
		}
		if (resolveFuzzy) {
			List<Function> declaredFunctions = FunctionCache.getInstance()
					.getDeclaredFunctions(container);
			for (Function function : declaredFunctions) {
				Variable dummyVar = PetrinetsFactoryImpl.eINSTANCE
						.createVariable();
				String functionname = function.getName() + "(";
				EList<Parameter> parameters = function.getParameters();
				for (Parameter parameter : parameters) {
					functionname += parameter.getName() + ", ";
				}
				if (!parameters.isEmpty()) {
					functionname = functionname.substring(0,
							functionname.length() - 2);
				}
				functionname += ")";
				dummyVar.setName(functionname);
				candidates.add(dummyVar);
			}
		}
		return candidates;
	}

	private void resolveRequired(Arc container) {
		EObject eContainer = container.eContainer();
		while (eContainer != null && !(eContainer instanceof PetriNet)) {
			eContainer = eContainer.eContainer();
		}
		if (eContainer instanceof PetriNet) {
			PetriNet pn = (PetriNet) eContainer;
			EList<Arc> arcs = pn.getArcs();
			for (Arc arc : arcs) {
				Component in = arc.getIn();
				Component out = arc.getOut();
				if (in.eIsProxy()) {
					EcoreUtil.resolve(in, pn);
				}
				if (out.eIsProxy()) {
					EcoreUtil.resolve(out, pn);
				}
			}
		}
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

	private List<ArcStatement> getPreviousStatements(VariableCall call) {
		List<ArcStatement> previousStatements = new ArrayList<ArcStatement>();
		Arc containing = null;
		EObject c = call.eContainer();
		EObject containingStatement = call;
		while (!(c instanceof Arc) && c != null) {
			containingStatement = c;
			c = c.eContainer();
		}
		if (c != null) {
			containing = (Arc) c;
			int indexOfContainingStatement = containing.getArcStatements()
					.indexOf(containingStatement);
			previousStatements = containing.getArcStatements().subList(0,
					indexOfContainingStatement);
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
