
package org.emftext.languages.petrinets.lib;
import org.eclipse.emf.ecore.*;
import java.util.*;
import org.emftext.language.petrinets.*;
import org.eclipse.emf.ecore.*;
public class StandardlibSemanticsEvaluation {


	List<PendingChange> pendingChanges = new LinkedList<PendingChange>();

	private class PendingChange {
		private String transitionName;
		private List<Object> arguments;
		public PendingChange(String transitionName, List<Object> arguments) {
			this.transitionName = transitionName;
			this.arguments = arguments;
		}
	}

	private void addPendingChange(String transitionName, List<Object> arguments) {
		this.pendingChanges.add(new PendingChange(transitionName, arguments));
	}

	public void intialisePlaces(EObject model) {
		// TODO implement place initialisation for places w/o incomming arcs.
	}

	public void evaluatePetriNet() {
		while(this.pendingChanges.size() > 0) {;
			PendingChange pc = pendingChanges.remove(0);
		}
	}

}