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
public class GroupTypeField extends DialogField {
	private Button[] buttons;
	private String[] labels;
	private int type;

	public GroupTypeField(int type) {
		super();
		this.type = type;
	}

	public void setLabels(String... labels) {
		this.labels = labels;
	}

	@Override
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
			Button button = new Button(checkboxContainer, this.type);
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

	public boolean getSelection(String f) {
		int l = buttons.length;
		for (int i = 0; i < l; i++) {
			if (buttons[i].getText().equals(f)) {
				return buttons[i].getSelection();
			}
		}
		return false;

	}

	public int getValue() {
		switch (this.type) {
		case SWT.CHECK:
			return getCheckValue();
		case SWT.RADIO:
			return getRadioValue();
		default:
			return 0;
		}
	}

	public int getCheckValue() {
		int f = 0;
		int l = buttons.length;
		for (int i = 0; i < l; i++) {
			if (buttons[i].getSelection()) {
				f |= (1 << i);
			}
		}
		return f;
	}

	public int getRadioValue() {
		int l = buttons.length;
		for (int i = 0; i < l; i++) {
			if (buttons[i].getSelection()) {
				return (1 << i);
			}
		}
		return 0;
	}

	public void setValue(String label) {
		for (Button b : this.buttons) {
			b.setSelection(label.equals(b.getText()));
		}
	}

}
