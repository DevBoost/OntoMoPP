
package math.semantics;
import org.eclipse.emf.ecore.*;
import java.util.*;
import math.*;
public class ExampleSemanticsEvaluation {

	private List<Expression> _place_dod = new ArrayList<Expression>();
	private List<Expression> _place_did = new ArrayList<Expression>();
	private List<Expression> _place_Start = new ArrayList<Expression>();

	public void intialisePlaces(EObject model) {
		// TODO implement place initialisation for places w/o incomming arcs.
	}

	public void evaluatePetriNet() {
		boolean petrinetWasUpdated = true;
		while(petrinetWasUpdated) {;
			petrinetWasUpdated = false;
			if(transition_t_canFire()) {
				transition_t_doFire();
				petrinetWasUpdated = true;
			}
		}
	}
	private boolean transition_t_canFire() {
		if (_place_dod.isEmpty() ) return false;
		return true;
	}

	private void transition_t_doFire() {
		Expression _in_dod = _place_dod.remove(0);
		int y;
		java.lang.String a;
		Expression p;
		java.lang.String xx;
		int eins;
		double zwei;
		float drei;
		boolean f;
		boolean t;
		int vd;
		{ // evaluate incomming arcs

			Expression inToken = _in_dod;
			// .getElements()
			List<Elements> __temp_1 = inToken.getElements();
			// .get()
			int __temp_2 = 1;
			Elements __temp_3 = __temp_1.get(__temp_2);
			// .getName()
			java.lang.String __temp_4 = __temp_3.getName();
			// .toLowerCase()
			java.lang.String __temp_5 = __temp_4.toLowerCase();
			// .length()
			int __temp_6 = __temp_5.length();
			y = __temp_6;

			String __temp_7 = a;
			a = __temp_7;

			Expression __temp_8 = inToken;

			p = __temp_8;

			// .getName()
			java.lang.String __temp_9 = inToken.getName();
			xx = __temp_9;

			int __temp_10 = 1;
			eins = __temp_10;

			double __temp_11 = 2.0;
			zwei = __temp_11;

			float __temp_12 = 3.0f;
			drei = __temp_12;

			boolean __temp_13 = false;
			f = __temp_13;

			boolean __temp_14 = true;
			t = __temp_14;

			// .getName()
			java.lang.String __temp_15 = inToken.getName();
			// .toUpperCase()
			java.lang.String __temp_16 = __temp_15.toUpperCase();
			// .substring()
			// .getName()
			java.lang.String __temp_17 = inToken.getName();
			// .length()
			int __temp_18 = __temp_17.length();
			int __temp_19 = 3;
			java.lang.String __temp_20 = __temp_16.substring(__temp_18, __temp_19);
			// .length()
			int __temp_21 = __temp_20.length();
			vd = __temp_21;

		}
		Expression _out_did = math.MathFactory.eINSTANCE.createExpression();
		{ // evaluate outgoing arcs

			Expression outToken = _out_did;
			// .setName()
			java.lang.String __temp_22 = xx;

			outToken.setName(__temp_22);
			_out_did = outToken;
		}
		_place_did.add(0, _out_did);
	}

}