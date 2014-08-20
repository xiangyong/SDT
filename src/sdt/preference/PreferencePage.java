package sdt.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sdt.SDTPlugin;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String MYSQL_CONNECTOR = "MYSQL_CONNECTOR";

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(SDTPlugin.getDefault().getPreferenceStore());
		setDescription("SDT Preference Page");
	}

	public void createFieldEditors() {
		FileFieldEditor fe = new FileFieldEditor(MYSQL_CONNECTOR, "&MySql Connector:", getFieldEditorParent());
		fe.setFileExtensions(new String[] { "*.jar" });
		addField(fe);
	}

	public void init(IWorkbench workbench) {
	}

}