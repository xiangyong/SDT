package sdt.wizards.newdal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.eclipse.swt.SWT;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.preference.PreferencePage;
import sdt.wizards.GroupTypeField;
import sdt.wizards.NewWizardPage;
import sdt.wizards.change.ChangeEngine;
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
		this.status = new StatusInfo(IStatus.OK, "");

		int i = 1;
		fDalDbTypeField = createGroupTypeField("Database:", SWT.RADIO, "MySQL", "MySQL");

		fServerField = createStringDialogField(this, "&" + i++ + " Server:", "localhost");
		fPortField = createStringDialogField(this, "&" + i++ + " Port:", "3306");
		fUsernameField = createStringDialogField(this, "&" + i++ + " Username:", "root");
		fPasswordField = createStringDialogField(this, "&" + i++ + " Password:", null);
		createSeparator();
		fTableField = createStringButtonDialogField("&" + i++ + " Table:", "Browse &Q", this.OTHER, null, null,
				true, null);
		createSeparator();
		fProjectField = createStringButtonDialogField("&" + i++ + " Project:", "Browse &W", this.PROJECT,
				"-common-dal$", null, false, null);
		fPackageField = createStringButtonDialogField("&" + i++ + " Package:", "Browse &E", this.PACKAGE, null,
				null, true, fProjectField);

	}

	private Connection fConn;

	public Connection getConnection() {
		if (fConn != null) {
			return fConn;
		}
		Connection conn = null;
		try {

			String jar = SDTPlugin.getPreference(PreferencePage.MYSQL_CONNECTOR);
			System.err.println("jar: " + new File(jar).exists());
			if (jar == null || jar.isEmpty() || !new File(jar).exists()) {
				updateStatus(new StatusInfo(IStatus.ERROR,
						"MySql Connector Is Invalid, See Window/Preferences/Sofa/MySql Connector"));
				return null;
			}
			String driverName = "com.mysql.jdbc.Driver";
			String server = this.fServerField.getText();
			String port = this.fPortField.getText();
			String username = this.fUsernameField.getText();
			String password = this.fPasswordField.getText();

			URLClassLoader classloader = getClassLoader();
			Class<?> clazz = classloader.loadClass(driverName);
			Driver driver = (Driver) clazz.newInstance();
			Properties ps = new Properties();
			ps.put("user", username);
			ps.put("password", password);

			conn = driver.connect("jdbc:mysql://" + server + ":" + port, ps);

			if (conn == null || conn.isClosed())
				return null;
		} catch (Exception e) {
			closeDB(conn, null, null);
			e.printStackTrace();
			updateStatus(new StatusInfo(IStatus.ERROR, e.getClass() + ": " + e.getMessage()));
		}
		fConn = conn;
		return conn;
	}

	private String fJar = null;
	private URLClassLoader cachedURLClassLoader;

	private URLClassLoader getClassLoader() {
		String jar = SDTPlugin.getPreference(PreferencePage.MYSQL_CONNECTOR);
		if (fJar != null && cachedURLClassLoader != null && !fJar.equals(jar))
			return cachedURLClassLoader;

		URLClassLoader classloader = null;
		try {
			classloader = new URLClassLoader(new URL[] { new File(jar).toURI().toURL() });
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return classloader;
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
			e.printStackTrace();
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
			this.data.fTable = table;
			this.fTableField.setText(table.toStr());
		}
	}

	// TODO IDialogFieldListener#dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == fDalDbTypeField || field == fServerField || field == fPortField || field == fUsernameField
				|| field == fPasswordField) {
			return;
		}
		if (field == this.fProjectField && this.fPackageField.getText().isEmpty()) {
			String system = NameUtil.firstString(this.fProjectField.getText(), '-');
			this.fPackageField.setText("com.alipay." + system + ".common.dal");
		}
		updateStatus();

	}

	@SuppressWarnings("unchecked")
	protected IStatus getStatus() {
		if (this.status != null && this.status.isError()) {
			return this.status;
		}

		IStatus f = getStatus(fTableField, fProjectField, fPackageField);
		if (f != null)
			return f;

		if (this.fPackageField.getText().endsWith(".")) {
			return new StatusInfo(IStatus.WARNING, "Package 不合法");
		}

		String project = this.fProjectField.getText();
		Map context = new HashMap();
		context.put("projectName", project);
		context.put("systemName", NameUtil.firstString(project, '-'));
		context.put("packageRoot", this.fPackageField.getText());
		context.put("packageRootDir", this.fPackageField.getText().replace('.', '/'));
		context.put("table", this.data.fTable);
		Properties ps = new Properties();
		ByteArrayInputStream is = null;
		try {
			String dalConf = SDTPlugin.getTpl(context, "tpl/dal/conf.vm");
			is = new ByteArrayInputStream(dalConf.getBytes());
			ps.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
					is = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		for (Map.Entry entry : ps.entrySet()) {
			List<String> args = ChangeEngine.getArgs(entry.getValue().toString());
			char changeAction = args.get(0).charAt(0);
			if (changeAction == 'F' || changeAction == 'M') {
				IFile file = SDTPlugin.getFile(args.get(1));
				if (file != null && file.exists()) {
					return new StatusInfo(IStatus.ERROR, "\"" + file.getName() + "\" is already exists");
				}
			}
		}

		return new StatusInfo(IStatus.OK, "");
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
			e.printStackTrace();
		}
		closeDB(null, statement, rs);
	}

}
