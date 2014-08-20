package sdt.decoration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

import sdt.SDTPlugin;

public class SofaDecorator implements ILabelDecorator {

	@SuppressWarnings("unused")
	private ImageDescriptor getImage(Object element) {
		if (element instanceof IWorkingSet) {
			IWorkingSet ws = (IWorkingSet) element;
			IAdaptable[] as = ws.getElements();
			for (IAdaptable a : as) {
				Object o = a.getAdapter(IProject.class);
				if (o == null)
					continue;
				IProject p = (IProject) o;
				if (p.getName().contains("-assembly-")) {
					return SDTPlugin.getImageDescriptor("icons/group.png");
				}
			}
		} else if (element instanceof IProject) {
			IProject p = (IProject) element;
			String name = p.getName();
			if (name.endsWith("-htdocs")) {
				return SDTPlugin.getImageDescriptor("icons/blue.png");
			}

		} else if (element instanceof IJavaProject) {
			IJavaProject jp = (IJavaProject) element;
			String name = jp.getElementName();

			if (name.endsWith("-htdocs")) {
				return SDTPlugin.getImageDescriptor("icons/blue.png");
			}

			IFile file = SDTPlugin.getFile(name + "/pom.xml");
			if (!file.exists())
				return null;
			if (name.contains("-assembly-")) {
				return SDTPlugin.getImageDescriptor("icons/red.png");
			} else if (name.contains("-biz-")) {
				return SDTPlugin.getImageDescriptor("icons/green.png");
			} else if (name.contains("-common-")) {
				return SDTPlugin.getImageDescriptor("icons/orange.png");
			} else if (name.contains("-core-")) {
				return SDTPlugin.getImageDescriptor("icons/green.png");
			} else if (name.endsWith("-test") || name.endsWith("-tester")) {
				return SDTPlugin.getImageDescriptor("icons/red.png");
			} else if (name.contains("-web-")) {
				return SDTPlugin.getImageDescriptor("icons/blue.png");
			} else {
				return SDTPlugin.getImageDescriptor("icons/orange.png");
			}

		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
