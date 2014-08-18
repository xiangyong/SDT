package sdt.wizards;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ResourceItemLabelProvider extends LabelProvider implements ILabelProviderListener,
		IStyledLabelProvider {

	private ListenerList listeners = new ListenerList();

	private WorkbenchLabelProvider provider = new WorkbenchLabelProvider();

	private List<IResource> list;

	public ResourceItemLabelProvider(List<IResource> list) {
		super();
		provider.addListener(this);
		this.list = list;
	}

	private boolean isDuplicateElement(Object element) {
		return this.list.contains(element);
	}

	public Image getImage(Object element) {
		if (!(element instanceof IResource)) {
			return super.getImage(element);
		}

		IResource res = (IResource) element;

		return provider.getImage(res);
	}

	public String getText(Object element) {
		if (!(element instanceof IResource)) {
			return super.getText(element);
		}

		IResource res = (IResource) element;

		String str = res.getName();

		if (isDuplicateElement(element))
			str = str + " - " + res.getParent().getFullPath().makeRelative().toString(); //$NON-NLS-1$

		return str;
	}

	public StyledString getStyledText(Object element) {
		if (!(element instanceof IResource)) {
			return new StyledString(super.getText(element));
		}

		IResource res = (IResource) element;

		StyledString str = new StyledString(res.getName());

		if (isDuplicateElement(element)) {
			str.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			str.append(res.getParent().getFullPath().makeRelative().toString(), StyledString.QUALIFIER_STYLER);
		}
		return str;
	}

	public void dispose() {
		provider.removeListener(this);
		provider.dispose();

		super.dispose();
	}

	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	public void labelProviderChanged(LabelProviderChangedEvent event) {
		Object[] l = listeners.getListeners();
		for (int i = 0; i < listeners.size(); i++) {
			((ILabelProviderListener) l[i]).labelProviderChanged(event);
		}
	}

}