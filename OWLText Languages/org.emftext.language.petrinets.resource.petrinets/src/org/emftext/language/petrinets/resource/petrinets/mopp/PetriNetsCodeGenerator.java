package org.emftext.language.petrinets.resource.petrinets.mopp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.emftext.language.petrinets.Arc;
import org.emftext.language.petrinets.ArcStatement;
import org.emftext.language.petrinets.BooleanLiteral;
import org.emftext.language.petrinets.Component;
import org.emftext.language.petrinets.DoubleLiteral;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.FloatLiteral;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.IntegerLiteral;
import org.emftext.language.petrinets.Literal;
import org.emftext.language.petrinets.LongLiteral;
import org.emftext.language.petrinets.PList;
import org.emftext.language.petrinets.PetriNet;
import org.emftext.language.petrinets.Place;
import org.emftext.language.petrinets.StringLiteral;
import org.emftext.language.petrinets.Transition;
import org.emftext.language.petrinets.Variable;
import org.emftext.language.petrinets.VariableCall;
import org.emftext.language.petrinets.resource.petrinets.analysis.FunctionCache;

public class PetriNetsCodeGenerator {

	private static final String OUT_TOKEN_NAME = "outToken";

	private static final String IN_TOKEN_NAME = "inToken";

	private JavaStringBuffer stringBuffer;

	private String contextVariableName;

	private int counter = 0;

	private Map<String, GenClass> genClassMap = new HashMap<String, GenClass>();

	private String generateContextVariableName() {
		counter++;
		return "__temp_" + counter;
	}

	public void generateJavaCode(PetrinetsResource resource) {
		IFile file = WorkspaceSynchronizer.getFile(resource);

		stringBuffer = new JavaStringBuffer();
		if (resource.getContents().isEmpty())
			return;
		if (resource.getContents().get(0) instanceof PetriNet) {
			PetriNet pn = (PetriNet) resource.getContents().get(0);
			EcoreUtil.resolveAll(pn);
			initialiseGenClassMap(pn);
			generateCode(pn);
			try {

				String name = toFirstUpper(trimQuotes(pn.getName()));
				name = name + "PetriNetsSemanticsEvaluation.java";

				IPath currentDir = file.getLocation().removeLastSegments(1)
						.append("" + IPath.SEPARATOR);

				EList<String> pkg = pn.getPkg();
				for (String p : pkg) {
					currentDir = currentDir.append(p + IPath.SEPARATOR);
					File folder = currentDir.toFile();
					if (!folder.exists()) {
						folder.mkdir();
					}
				}
				File f = currentDir.append(name).toFile();

				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
				FileOutputStream fout = new FileOutputStream(f);
				Writer out = new OutputStreamWriter(fout);
				try {
					out.write(stringBuffer.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		}

	}

	private void initialiseGenClassMap(PetriNet pn) {
		EList<GenModel> genModels = pn.getGenModels();
		for (GenModel genModel : genModels) {
			TreeIterator<EObject> eAllContents = genModel.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = (EObject) eAllContents.next();
				if (eObject instanceof GenClass) {
					GenClass gc = (GenClass) eObject;
					EClass ecoreClass = gc.getEcoreClass();
					this.genClassMap.put(ecoreClass.getName(), gc);
				}

			}
		}

	}

	private String trimQuotes(String name) {
		return name.substring(1, name.length() - 1);
	}

	private void generateCode(PetriNet pn) {
		String name = toFirstUpper(trimQuotes(pn.getName()));
		EList<Component> components = pn.getComponents();
		if (!pn.getPkg().isEmpty()) {
			stringBuffer.appendLine("package " + pn.getPkg().get(0));
			for (String p : pn.getPkg().subList(1, pn.getPkg().size())) {
				stringBuffer.append("." + p);
			}
			stringBuffer.append(";");
		}
		stringBuffer.appendLine("import org.eclipse.emf.ecore.*;");
		stringBuffer.appendLine("import java.util.*;");
		EList<GenModel> imports = pn.getGenModels();
		for (GenModel i : imports) {
			EList<GenPackage> genPackages = i.getGenModel().getGenPackages();
			for (GenPackage genPackage : genPackages) {
				stringBuffer.appendLine("import "
						+ genPackage.getInterfacePackageName() + ".*;");
			}
		}
		stringBuffer.appendLine("public class " + name
				+ "PetriNetsSemanticsEvaluation {");
		stringBuffer.indent();
		stringBuffer.newline();
		for (Component component : components) {
			if (component instanceof Place)
				generateCode((Place) component);
		}
		stringBuffer.newline();
		stringBuffer.appendLine("public void intialisePlaces(EObject model) {");
		stringBuffer.indent();
		stringBuffer
				.appendLine("// TODO implement place initialisation for places w/o incomming arcs.");
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		stringBuffer.newline();

		stringBuffer.appendLine("public void evalutePetriNet() {");
		stringBuffer.indent();
		stringBuffer.appendLine("boolean petrinetWasUpdated = true;");
		stringBuffer.appendLine("while(petrinetWasUpdated) {;");
		stringBuffer.indent();
		stringBuffer.appendLine("petrinetWasUpdated = false;");
		for (Component component : components) {
			if (component instanceof Transition) {
				Transition t = (Transition) component;
				stringBuffer.appendLine("if(transition_"
						+ trimQuotes(t.getName()) + "_canFire()) {");
				stringBuffer.indent();
				stringBuffer.appendLine("transition_" + trimQuotes(t.getName())
						+ "_doFire();");
				stringBuffer.appendLine("petrinetWasUpdated = true;");
				stringBuffer.unIndent();
				stringBuffer.appendLine("}");
			}
		}
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		for (Component component : components) {
			if (component instanceof Transition) {
				Transition t = (Transition) component;
				generateCode(t);
			}
		}

		stringBuffer.newline();
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");
	}

	private String toFirstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private void generateCode(Place p) {
		stringBuffer.appendLine("private List<" + printType(p.getType()) + "> "
				+ trimQuotes(p.getName()) + " = new ArrayList<"
				+ printType(p.getType()) + ">();");
	}

	private void generateCode(Transition t) {
		stringBuffer.appendLine("private boolean transition_"
				+ trimQuotes(t.getName()) + "_canFire() {");
		stringBuffer.indent();
		EList<Arc> incomingArcs = t.getIncoming();
		for (Arc arc : incomingArcs) {
			Place in = (Place) arc.getIn();
			stringBuffer.appendLine("if (" + trimQuotes(in.getName())
					+ ".isEmpty() ) return false;");
		}

		stringBuffer.appendLine("return true;");

		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		stringBuffer.newline();

		stringBuffer.appendLine("private void transition_"
				+ trimQuotes(t.getName()) + "_doFire() {");
		stringBuffer.indent();
		Map<Place, String> placeMap = new HashMap<Place, String>();
		for (Arc arc : incomingArcs) {
			Place in = (Place) arc.getIn();
			if (placeMap.get(in) == null) {
				String placeContext = "_place_" + trimQuotes(in.getName());
				stringBuffer.appendLine(printType(in.getType()) + " "
						+ placeContext + " = " + trimQuotes(in.getName())
						+ ".remove(0);");
				placeMap.put(in, placeContext);
			}
		}
		for (Arc arc : incomingArcs) {
			EList<ArcStatement> arcStatements = arc.getArcStatements();

			for (ArcStatement arcStatement : arcStatements) {
				if (arcStatement instanceof Variable) {
					Variable v = (Variable) arcStatement;
					Expression e = v.getInitialisation();
					while (e.getNextExpression() != null) {
						e = e.getNextExpression();
					}
					if (e instanceof Literal) {
						FunctionCache.getInstance().calculateType(e);
					}
					stringBuffer.appendLine(printType(e.getType()) + " "
							+ v.getName() + ";");
				}
			}
			Place in = (Place) arc.getIn();

			stringBuffer.appendLine("{ // evaluate incomming arcs");
			stringBuffer.newline();
			stringBuffer.indent();

			stringBuffer.appendLine(printType(in.getType()) + " "
					+ IN_TOKEN_NAME + " = " + placeMap.get(in) + ";");
			for (ArcStatement arcStatement : arcStatements) {
				generateCode(arcStatement);
			}
			stringBuffer.unIndent();
			stringBuffer.appendLine("}");
		}
		placeMap = new HashMap<Place, String>();
		EList<Arc> outgoingArcs = t.getOutgoing();
		for (Arc arc : outgoingArcs) {
			Place out = (Place) arc.getOut();
			if (placeMap.get(out) == null) {
				String placeContext = "_place_" + trimQuotes(out.getName());
				stringBuffer.appendLine(printType(out.getType()) + " "
						+ placeContext + " = " + printCreation(out.getType())
						+ ";");
				placeMap.put(out, placeContext);
			}
		}

		for (Arc arc : outgoingArcs) {
			stringBuffer.appendLine("{ // evaluate outgoing arcs");
			stringBuffer.newline();
			stringBuffer.indent();
			Place out = (Place) arc.getOut();
			stringBuffer.appendLine(printType(out.getType()) + " "
					+ OUT_TOKEN_NAME + " = " + placeMap.get(out) + ";");

			EList<ArcStatement> arcStatements = arc.getArcStatements();
			for (ArcStatement arcStatement : arcStatements) {
				generateCode(arcStatement);
			}
			stringBuffer.appendLine(placeMap.get(out) + " = " + OUT_TOKEN_NAME
					+ ";");
			stringBuffer.unIndent();
			stringBuffer.appendLine("}");
		}
		Set<Place> keySet = placeMap.keySet();
		for (Place place : keySet) {
			String placeContext = placeMap.get(place);
			stringBuffer.appendLine(trimQuotes(place.getName()) + ".add(0, "
					+ placeContext + ");");
		}
		
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		
	}

	private String printCreation(EClassifier type) {
		if (type.getDefaultValue() != null) {
			return type.getDefaultValue().toString();
		} else {
			GenClass genClass = findGenClass(type);
			GenPackage genPackage = genClass.getGenPackage();
			return genPackage.getQualifiedFactoryInterfaceName()
					+ ".eINSTANCE.create" + genClass.getName() + "()";
		}

	}

	private GenClass findGenClass(EClassifier type) {
		return genClassMap.get(type.getName());
	}

	private String printType(EClassifier type) {
		if (type == null)
			return null;
		if (type instanceof PList) {
			PList list = (PList) type;
			return "List<" + printType(list.getType()) + ">";
		}
		if (type.getInstanceClassName() != null) {
			return type.getInstanceClassName();
		}
		return type.getName();
	}

	private void generateCode(Object o) {
		if (o instanceof Variable)
			generateCode((Variable) o);
		else if (o instanceof FunctionCall)
			generateCode((FunctionCall) o);
		else if (o instanceof VariableCall)
			generateCode((VariableCall) o);
		else if (o instanceof StringLiteral)
			generateCode((StringLiteral) o);
		else if (o instanceof IntegerLiteral)
			generateCode((IntegerLiteral) o);
		else if (o instanceof DoubleLiteral)
			generateCode((DoubleLiteral) o);
		else if (o instanceof LongLiteral)
			generateCode((LongLiteral) o);
		else if (o instanceof FloatLiteral)
			generateCode((FloatLiteral) o);
		else if (o instanceof BooleanLiteral)
			generateCode((BooleanLiteral) o);
		else if (o instanceof LongLiteral)
			generateCode((LongLiteral) o);
		else
			throw new RuntimeException("No codegeneration routine found for "
					+ o);

	}

	private void generateCode(VariableCall vc) {
		String contextVar = generateContextVariableName();
		stringBuffer.appendLine(printType(vc.getType()) + " " + contextVar
				+ " = ");
		stringBuffer.append(vc.getVariable().getName() + ";");
		stringBuffer.newline();
		this.contextVariableName = contextVar;
	}

	private void generateCode(FunctionCall fc) {
		String prevContextVarName = this.contextVariableName;
		EList<Expression> parameters = fc.getParameters();
		Map<Expression, String> argumentVars = new HashMap<Expression, String>();
		for (Expression expression : parameters) {
			generateCode(expression);
			argumentVars.put(expression, this.contextVariableName);
		}
		String contextVar = null;
		if (fc.getType() != null && !"PVoid".equals(fc.getType().getName())) {
			contextVar = generateContextVariableName();
			stringBuffer.appendLine(printType(fc.getType()) + " " + contextVar
					+ " = ");
		} else {
			stringBuffer.appendLine("");
		}
		// library function
		if ((fc.getFunction().getParameters().size() + 1) == fc.getParameters()
				.size()) {
			stringBuffer.appendLine(fc.getFunction().getName() + "(");
		}

		if (fc.getPreviousExpression() == null) {
			stringBuffer.append(getContextName(fc));
		} else {

			stringBuffer.append(prevContextVarName);
		}
		// api function
		if ((fc.getFunction().getParameters().size()) == fc.getParameters()
				.size()) {
			stringBuffer.append("." + fc.getFunction().getName() + "(");
		} else {
			stringBuffer.append(", ");
		}
		int i = 0;
		for (Expression expression : parameters) {
			i++;
			if (i == parameters.size()) {
				stringBuffer.append(argumentVars.get(expression));
			} else {
				stringBuffer.append(argumentVars.get(expression) + ", ");
			}
		}
		stringBuffer.append(");");
		// stringBuffer.newline();

		this.contextVariableName = contextVar;
		if (fc.getNextExpression() != null)
			generateCode(fc.getNextExpression());
	}

	private String getContextName(FunctionCall fc) {
		EObject eContainer = fc.eContainer();
		while (eContainer != null && !(eContainer instanceof Arc)) {
			eContainer = eContainer.eContainer();
		}
		if (((Arc) eContainer).getIn() instanceof Place) {
			return IN_TOKEN_NAME;
		} else
			return OUT_TOKEN_NAME;
	}

	private void generateCode(Variable v) {
		Expression expression = v.getInitialisation();
		generateCode(expression);
		String contextVariableName = this.contextVariableName;
		stringBuffer
				.appendLine(v.getName() + " = " + contextVariableName + ";");
		stringBuffer.newline();
	}

	private void generateCode(StringLiteral sl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("String " + this.contextVariableName + " = "
				+ sl.getValue() + ";");

	}

	private void generateCode(IntegerLiteral il) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("int " + this.contextVariableName + " = "
				+ il.getValue() + ";");

	}

	private void generateCode(LongLiteral ll) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("long " + this.contextVariableName + " = "
				+ ll.getValue() + ";");

	}

	private void generateCode(DoubleLiteral dl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("double " + this.contextVariableName + " = "
				+ dl.getValue() + ";");

	}

	private void generateCode(FloatLiteral fl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("float " + this.contextVariableName + " = "
				+ fl.getValue() + "f;");

	}

	private void generateCode(BooleanLiteral bl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("boolean " + this.contextVariableName + " = "
				+ bl.isValue() + ";");

	}
}
