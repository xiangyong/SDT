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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
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
import org.eclipse.swt.widgets.Text;
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

	@SuppressWarnings("unused")
	private IStructuredSelection selection;
	private NewSofaDalState data;
	private StatusInfo status;

	private GroupTypeField dalDbTypeField;
	private StringDialogField serverField;
	private StringDialogField portField;
	private StringDialogField usernameField;
	private StringDialogField passwordField;
	private StringButtonDialogField tableField;
	private StringButtonDialogField projectField;
	private StringButtonDialogField packageField;
	private JavaPackageCompletionProcessor packageCompletionProcessor;

	public NewSofaDalWizardPage(IStructuredSelection selection, NewSofaDalState data) {
		super("New Sofa Dal");
		this.selection = selection;
		this.data = data;
		this.status = new StatusInfo();
		wsroot = ResourcesPlugin.getWorkspace().getRoot();
		packageCompletionProcessor = new JavaPackageCompletionProcessor();

		int i = 1;
		dalDbTypeField = new GroupTypeField(SWT.RADIO);
		dalDbTypeField.setDialogFieldListener(this);
		dalDbTypeField.setLabels("MySQL");
		dalDbTypeField.setLabelText("&" + i++ + " Database Type:");

		serverField = createStringDialogField(this, "&" + i++ + " Server:");
		serverField = createStringDialogField(this, "&" + i++ + " Port:");
		serverField = createStringDialogField(this, "&" + i++ + " Username:");
		serverField = createStringDialogField(this, "&" + i++ + " Password:");
		tableField = createStringButtonDialogField(this, this, "&" + i++ + " Table Name:", "Browse &Q");
		tableField = createStringButtonDialogField(this, this, "&" + i++ + " Project Name:", "Browse &Q");
		tableField = createStringButtonDialogField(this, this, "&" + i++ + " Package Name:", "Browse &Q");

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

		dalDbTypeField.doFillIntoGrid(composite, nColumns);

		createStringDialogField(composite, nColumns, serverField);
		createStringDialogField(composite, nColumns, portField);
		createStringDialogField(composite, nColumns, usernameField);
		createStringDialogField(composite, nColumns, passwordField);

		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, tableField);

		createSeparator(composite, nColumns);

		createStringButtonDialogField(composite, nColumns, projectField);

		createStringButtonDialogField(composite, nColumns, packageField);
		Text text = packageField.getTextControl(null);
		ControlContentAssistHelper.createTextContentAssistant(text, packageCompletionProcessor);
		TextFieldNavigationHandler.install(text);

		handleFieldChanged();

		initPage();
	}

	private void initPage() {
		this.dalDbTypeField.setValue("MySQL");
		this.serverField.setText("localhost");
		this.portField.setText("3306");
		this.usernameField.setText("root");
		// this.passwordField.setText("ali88");
	}

	// TODO IStringButtonAdapter#changeControlPressed
	@Override
	public void changeControlPressed(DialogField field) {

		if (field == this.tableField) {
			Table table = chooseTable();
			if (table != null) {
				this.tableField.setText(table.toStr());
				this.data.fTable = table;
			}
		} else if (field == this.projectField) {
			chooseProject(this.projectField);
		} else if (field == this.packageField) {
			choosePackage(this.packageField);
		}
	}

	public Connection getConnection() {

		Connection conn = null;
		try {

			String jar = SDTPlugin.getPreference(PreferencePage.MYSQL_CONNECTOR);
			if (jar == null || jar.isEmpty())
				return null;

			String driverName = "com.mysql.jdbc.Driver";
			String server = this.serverField.getText();
			String port = this.portField.getText();
			String username = this.usernameField.getText();
			String password = this.passwordField.getText();

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

	public Table chooseTable() {
		List<Table> tables = new ArrayList<Table>();
		Statement statement = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
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
			return null;
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
			return (Table) dialog.getFirstResult();
		}

		return null;
	}

	protected List<IProject> filterProjects(IProject[] projects) {
		List<IProject> f = new ArrayList<IProject>();
		for (IProject p : super.filterProjects(projects)) {
			if (p.getName().endsWith("common-dal"))
				f.add(p);
		}
		return f;
	}

	// TODO IDialogFieldListener#dialogFieldChanged
	@Override
	public void dialogFieldChanged(DialogField field) {
		if (field == this.projectField) {
			if (this.packageField.getText().isEmpty()) {
				String system = NameUtil.firstString(this.projectField.getText(), '-');
				this.packageField.setText("com.alipay." + system + ".common.dal");
			}
		}
		handleFieldChanged();
		doStatusUpdate();
	}

	@SuppressWarnings("unchecked")
	public void handleFieldChanged() {

		if (this.tableField.getText().isEmpty()) {
			status = new StatusInfo(IStatus.ERROR, "请选择 Table");
			return;
		} else if (this.projectField.getText().isEmpty()) {
			status = new StatusInfo(IStatus.ERROR, "请选择 Project");
			return;
		} else if (this.packageField.getText().isEmpty()) {
			status = new StatusInfo(IStatus.ERROR, "请选择 Package");
			return;
		} else if (this.packageField.getText().endsWith(".")) {
			status = new StatusInfo(IStatus.WARNING, "Package 不合法");
			return;
		}

		String project = this.projectField.getText();
		Map context = new HashMap();
		context.put("projectName", project);
		context.put("systemName", NameUtil.firstString(project, '-'));
		context.put("packageRoot", this.packageField.getText());
		context.put("packageRootDir", this.packageField.getText().replaceAll("[.]", "/"));
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

	private void doStatusUpdate() {
		if (this.status == null) {
			return;
		}
		updateStatus(this.status);
	}

	public void refreshData() {
		this.data.fProject = this.projectField.getText();
		this.data.fPackage = this.packageField.getText();

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
		if (this.data == null || this.tableField.getText().isEmpty() || this.projectField.getText().isEmpty()) {
			return null;
		}
		return super.getNextPage();
	}

	public JavaPackageCompletionProcessor getJavaPackageCompletionProcessor(StringButtonDialogField field) {
		if (field == this.projectField) {
			return this.packageCompletionProcessor;
		}

		return null;
	}

	public StringButtonDialogField getProjFieldByPkgField(StringButtonDialogField field) {
		if (field == this.packageField) {
			return this.projectField;
		} else {
			return null;
		}
	}
}
