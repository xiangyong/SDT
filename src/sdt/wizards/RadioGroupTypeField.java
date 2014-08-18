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

/**
 * @author ronghai.ma 这个类可以座位模板了，只要修改 getValue 方法即可。
 */
@SuppressWarnings("restriction")
public class RadioGroupTypeField extends DialogField {

	private Button[] buttons;
	private String[] labels;

	public void setLabels(String... labels) {
		this.labels = labels;
	}

	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));

		Composite checkboxContainer = new Composite(parent, SWT.NULL);
		RowLayout checkboxContainerLayout = new RowLayout();
		checkboxContainer.setLayout(checkboxContainerLayout);
		checkboxContainer.setLayoutData(gridDataForCheckbox(nColumns - 1));
		int length = labels.length;
		buttons = new Button[length];

		for (int i = 0; i < length; i++) {
			Button button = new Button(checkboxContainer, SWT.RADIO);
			button.setText(labels[i]);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialogFieldChanged();
				}
			});
			buttons[i] = button;
		}
		return new Control[] { label, checkboxContainer };
	}

	public int getNumberOfControls() {
		return labels.length;
	}

	protected GridData gridDataForCheckbox(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		return gd;
	}

	public int getValue() {
		int l = buttons.length;
		for (int i = 0; i < l; i++) {
			if (buttons[i].getSelection()) {
				return (1 << i);
			}
		}
		return 0;
	}

	public void setValue(int i) {
		if (i >= labels.length) {
			return;
		}
		buttons[i].setSelection(true);
	}

	public void setValue(String label) {
		for (Button b : this.buttons) {
			b.setSelection(label.equals(b.getText()));
		}
	}
}
