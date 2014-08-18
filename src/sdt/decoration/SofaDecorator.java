package sdt.decoration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerProblemsDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

import sdt.SDTPlugin;

@SuppressWarnings("restriction")
public class SofaDecorator extends PackageExplorerProblemsDecorator implements ILabelDecorator {

	@Override
	public Image decorateImage(Image image, Object element) {
		Image f = getImage(image, element);
		return super.decorateImage(f, element);
	}

	private Image getImage(Image image, Object element) {
		if (element instanceof IWorkingSet) {
			IWorkingSet ws = (IWorkingSet) element;
			IAdaptable[] as = ws.getElements();
			for (IAdaptable a : as) {
				Object o = a.getAdapter(IProject.class);
				if (o == null)
					continue;
				IProject p = (IProject) o;
				if (p.getName().contains("-assembly-")) {
					return SDTPlugin.getImageDescriptor("icons/node/group.png").createImage();
				}
			}
		} else if (element instanceof IProject) {
			IProject p = (IProject) element;
			String name = p.getName();
			if (name.endsWith("-htdocs")) {
				return SDTPlugin.getImageDescriptor("icons/node/blue.png").createImage();
			}

		} else if (element instanceof IJavaProject) {
			IJavaProject jp = (IJavaProject) element;
			String name = jp.getElementName();

			if (name.endsWith("-htdocs")) {
				return SDTPlugin.getImageDescriptor("icons/node/blue.png").createImage();
			}

			IFile file = SDTPlugin.getFile(name + "/pom.xml");
			if (!file.exists())
				return null;

			if (name.contains("-assembly-")) {
				return SDTPlugin.getImageDescriptor("icons/node/red.png").createImage();
			} else if (name.contains("-biz-")) {
				return SDTPlugin.getImageDescriptor("icons/node/green.png").createImage();
			} else if (name.contains("-common-")) {
				return SDTPlugin.getImageDescriptor("icons/node/orange.png").createImage();
			} else if (name.contains("-core-")) {
				return SDTPlugin.getImageDescriptor("icons/node/green.png").createImage();
			} else if (name.endsWith("-test") || name.endsWith("-tester")) {
				return SDTPlugin.getImageDescriptor("icons/node/red.png").createImage();
			} else if (name.contains("-web-")) {
				return SDTPlugin.getImageDescriptor("icons/node/blue.png").createImage();
			} else {
				return SDTPlugin.getImageDescriptor("icons/node/orange.png").createImage();
			}

		} else if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (file.getFileExtension().equals("acf")) {
				return SDTPlugin.getImageDescriptor("icons/SecurityRole.png").createImage();
			}
		}
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
