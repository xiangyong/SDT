package sdt.wizards.newdal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.preference.PreferencePage;
import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;
import sdt.wizards.newdal.NewSofaDalState.Table;
import sdt.wizards.newdal.NewSofaDalState.Table.Column;

@SuppressWarnings("restriction")
public class NewSofaDalWizardPage extends NewWizardPage implements IStringButtonAdapter, IDialogFieldListener {

	private NewSofaDalState data;
	private StatusInfo status;

	private GroupTypeField fDalDbTypeField;
	private StringDialogField fServerField;
	private StringDialogField fPortField;
	private StringDialogField fUsernameField;
	private StringDialogField fPasswordField;
	private StringButtonDialogField fTableField;
	private StringButtonDialogField fProjectField;
	private StringButtonDialogField fPackageField;

	public NewSofaDalWizardPage(IStructuredSelection selection, NewSofaDalState data) {
		super("New Dal");
		this.data = data;
		this.status = new StatusInfo();

		int i = 1;
		fDalDbTypeField = createGroupTypeField("Database:", SWT.RADIO, "MySQL");

		fServerField = createStringDialogField(this, "&" + i++ + " Server:");
		fPortField = createStringDialogField(this, "&" + i++ + " Port:");
		fUsernameField = createStringDialogField(this, "&" + i++ + " Username:");
		fPasswordField = createStringDialogField(this, "&" + i++ + " Password:");

		fTableField = createStringButtonDialogField("&" + i++ + " Table:", "Browse &Q", this.OTHER, null, null,
				null);
		fProjectField = createStringButtonDialogField("&" + i++ + " Project:", "Browse &W", this.PROJECT,
				"-common-dal$", null, null);
		fPackageField = createStringButtonDialogField("&" + i++ + " Package:", "Browse &E", this.PACKAGE, null,
				null, fProjectField);

	}

	// TODO createControl
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		setControl(composite);

		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		int nColumns = 4;
		layout.numColumns = nColumns;

		fDalDbTypeField.doFillIntoGrid(composite, nColumns);

		createStringDialogField(composite, nColumns, fServerField);
		createStringDialogField(composite, nColumns, fPortField);
		createStringDialogField(composite, nColumns, fUsernameField);
		createStringDialogField(composite, nColumns, fPasswordField);

		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, fTableField, true);

		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, fProjectField, false);

		createStringButtonDialogField(composite, nColumns, fPackageField, false);

		handleFieldChanged();

		initPage();
	}

	private void initPage() {
		this.fDalDbTypeField.setValue("MySQL");
		this.fServerField.setText("localhost");
		this.fPortField.setText("3306");
		this.fUsernameField.setText("root");
		// this.passwordField.setText("ali88");
	}

	public Connection getConnection() {

		Connection conn = null;
		try {

			String jar = SDTPlugin.getPreference(PreferencePage.MYSQL_CONNECTOR);
			if (jar == null || jar.isEmpty())
				return null;

			String driverName = "com.mysql.jdbc.Driver";
			String server = this.fServerField.getText();
			String port = this.fPortField.getText();
			String username = this.fUsernameField.getText();
			String password = this.fPasswordField.getText();

			URLClassLoader classloader = new URLClassLoader(new URL[] { new File(jar).toURI().toURL() });
			Class<?> clazz = classloader.loadClass(driverName);
			Driver driver = (Driver) clazz.newInstance();
			Properties ps = new Properties();
			ps.put("user", username);
			ps.put("password", password);

			conn = driver.connect("jdbc:mysql://" + server + ":" + port, ps);

			if (conn == null || conn.isClosed()) {
				this.status.setError("连接已关闭");
				this.doStatusUpdate();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.status.setError(e.getMessage());
			this.doStatusUpdate();
			closeDB(conn, null, null);
		}
		return conn;
	}

	private void closeDB(Connection conn, Statement statement, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (statement != null) {
				statement.close();
				statement = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	protected void chooseOther(StringButtonDialogField tableField) {
		List<Table> tables = new ArrayList<Table>();
		Statement statement = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		if (conn == null)
			return;

		try {
			statement = conn.createStatement();
			String sql = "select f.TABLE_NAME, f.TABLE_SCHEMA" + " from information_schema.tables f"
					+ " WHERE f.TABLE_SCHEMA <> 'information_schema'"
					+ " AND f.TABLE_SCHEMA <> 'performance_schema'" + " AND f.TABLE_SCHEMA <> 'mysql'"
					+ " AND f.TABLE_SCHEMA <> 'test'";
			rs = statement.executeQuery(sql);

			while (rs.next()) {
				Table table = new Table();
				table.name = rs.getString(1);
				table.schema = rs.getString(2);

				tables.add(table);
			}
		} catch (SQLException e) {
			System.err.println("chooseTable 连接出错");
		} finally {
			closeDB(conn, statement, rs);
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setIgnoreCase(false);
		dialog.setTitle("选 common-dal 工程");
		dialog.setMessage("选择 common-dal 工程");
		dialog.setEmptyListMessage("没有 common-dal 工程");
		dialog.setElements(tables.toArray());
		dialog.setHelpAvailable(false);
		if (dialog.open() == Window.OK) {
			Table table = (Table) dialog.getFirstResult();
			this.fTableField.setText(table.toStr());
			this.data.fTable = table;
		}
	}

	// TODO IDialogFieldListener#dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == this.fProjectField) {
			if (this.fPackageField.getText().isEmpty()) {
				String system = NameUtil.firstString(this.fProjectField.getText(), '-');
				this.fPackageField.setText("com.alipay." + system + ".common.dal");
			}
		}
		handleFieldChanged();
		doStatusUpdate();
	}

	@SuppressWarnings("unchecked")
	public void handleFieldChanged() {

		if (this.fTableField.getText().isEmpty()) {
			status = new StatusInfo(IStatus.ERROR, "请选择 Table");
			return;
		} else if (this.fProjectField.getText().isEmpty()) {
			status = new StatusInfo(IStatus.ERROR, "请选择 Project");
			return;
		} else if (this.fPackageField.getText().isEmpty()) {
			status = new StatusInfo(IStatus.ERROR, "请选择 Package");
			return;
		} else if (this.fPackageField.getText().endsWith(".")) {
			status = new StatusInfo(IStatus.WARNING, "Package 不合法");
			return;
		}

		String project = this.fProjectField.getText();
		Map context = new HashMap();
		context.put("projectName", project);
		context.put("systemName", NameUtil.firstString(project, '-'));
		context.put("packageRoot", this.fPackageField.getText());
		context.put("packageRootDir", this.fPackageField.getText().replaceAll("[.]", "/"));
		context.put("table", this.data.fTable);
		Properties ps = new Properties();
		try {
			String dalConf = SDTPlugin.getTpl(context, "tpl/dal/conf.vm");
			ps.load(new ByteArrayInputStream(dalConf.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Map.Entry entry : ps.entrySet()) {
			IFile file = SDTPlugin.getFile(entry.getValue().toString());
			if (file != null && file.exists()) {
				status = new StatusInfo(IStatus.ERROR, "\"" + file.getName() + "\" is already exists");
				return;
			}
		}

		status = new StatusInfo(IStatus.OK, "");
	}

	// TODO
	private void doStatusUpdate() {
		if (this.status == null) {
			return;
		}
		updateStatus(this.status);
	}

	public void refreshData() {
		this.data.fProject = this.fProjectField.getText();
		this.data.fPackage = this.fPackageField.getText();

		if (this.data == null || this.data.fTable == null) {
			return;
		}

		Connection conn = getConnection();
		if (conn == null) {
			return;
		}

		Statement statement = null;
		ResultSet rs = null;
		List<Column> columns = new ArrayList<Column>();
		try {
			statement = conn.createStatement();
			String sql = "SELECT f.COLUMN_NAME, f.DATA_TYPE, f.COLUMN_KEY, f.EXTRA" //
					+ " FROM information_schema.`COLUMNS` f" //
					+ " WHERE f.TABLE_NAME = '" + this.data.fTable.name + "'" //
					+ " AND f.TABLE_SCHEMA = '" + this.data.fTable.schema + "'";
			rs = statement.executeQuery(sql);

			while (rs.next()) {
				Column column = new Column();
				column.name = rs.getString(1);
				column.type = rs.getString(2);
				String key = rs.getString(3);
				String ai = rs.getString(4);
				if (key != null && key.equalsIgnoreCase("PRI")) {
					column.pk = true;
				}
				if (ai != null && ai.equalsIgnoreCase("auto_increment")) {
					column.ai = true;
				}

				columns.add(column);
			}

			this.data.fTable.columns = columns.toArray(new Column[0]);

		} catch (SQLException e) {

			System.err.println("refreshData 连接出错");
			e.printStackTrace();
		} finally {
			closeDB(conn, statement, rs);
		}
	}

	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		if (this.data == null || this.fTableField.getText().isEmpty() || this.fProjectField.getText().isEmpty()) {
			return null;
		}
		return super.getNextPage();
	}

}
