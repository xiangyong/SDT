package sdt.wizards;

import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class CheckboxGroupTypeField extends DialogField {
	Button[] checkbox;
	String[] checkboxLabels;

	public CheckboxGroupTypeField() {
		super();
	}

	public void setLabels(String... labels) {
		checkboxLabels = labels;
	}

	@Override
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));

		Composite checkboxContainer = new Composite(parent, SWT.NULL);
		RowLayout checkboxContainerLayout = new RowLayout();
		checkboxContainer.setLayout(checkboxContainerLayout);
		checkboxContainer.setLayoutData(gridDataForCheckbox(nColumns - 1));
		int length = checkboxLabels.length;
		checkbox = new Button[length];

		for (int i = 0; i < length; i++) {
			Button button = new Button(checkboxContainer, SWT.CHECK);
			button.setText(checkboxLabels[i]);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialogFieldChanged();
				}
			});
			checkbox[i] = button;
		}
		return new Control[] { label, checkboxContainer };
	}

	public int getNumberOfControls() {
		return 2;
	}

	protected GridData gridDataForCheckbox(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		return gd;
	}

	public int getValue() {
		int f = 0;
		int l = checkbox.length;
		for (int i = 0; i < l; i++) {
			if (checkbox[i].getSelection()) {
				f |= (1 << i);
			}
		}
		return f;
	}

}
