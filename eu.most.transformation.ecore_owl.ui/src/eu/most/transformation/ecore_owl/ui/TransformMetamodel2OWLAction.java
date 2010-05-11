package eu.most.transformation.ecore_owl.ui;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.emftext.language.owl.OntologyDocument;

import eu.most.transformation.ecore_owl.Ecore2Owl;

public class TransformMetamodel2OWLAction implements IObjectActionDelegate {

	private ISelection selection;

	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object first = structuredSelection.getFirstElement();
			if (first instanceof IFile) {
				IFile file = (IFile) first;
				Resource resource = new ResourceSetImpl().createResource(URI
						.createFileURI(file.getLocation().toOSString()));
				if (resource != null) {
					try {
						resource.load(null);
						if (resource.getContents().get(0) instanceof EPackage) {
							OntologyDocument document = new Ecore2Owl()
									.transformMetamodel((EPackage) resource
											.getContents().get(0));
							URI targetURI = resource.getURI()
									.trimFileExtension().appendFileExtension(
											"owl");
							Resource documentResource = resource
									.getResourceSet().createResource(targetURI);
							documentResource.getContents().add(document);
							documentResource.save(null);
							file.getParent().refreshLocal(Integer.MAX_VALUE,
									null);
						}

					} catch (IOException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
}
