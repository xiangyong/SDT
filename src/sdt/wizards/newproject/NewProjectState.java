package sdt.wizards.newproject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;
import sdt.wizards.change.CreateDirChange;
import sdt.wizards.change.CreatePackageChange;

@SuppressWarnings("restriction")
public class NewProjectState implements NewWizardState {
	public String dir;
	public String system;
	public String name;
	public String type;

	@SuppressWarnings("unchecked")
	@Override
	public Change[] computeChanges() {
		Map context = new HashMap();
		context.put("system", this.system);
		context.put("project", this.name);

		String projectSimple = this.name.substring(this.name.lastIndexOf("-") + 1);
		context.put("projectSimple", projectSimple);
		context.put("projectBundleWebName", projectSimple);
		context.put("security", "security-" + projectSimple);
		context.put("projectBundleName", NameUtil.cap(this.name.replaceAll("-", " ")));

		String defaultPackage = "com.alipay." + this.name.replaceAll("-", ".");
		context.put("projectSymbolicName", defaultPackage);
		context.put("projectSpring", this.name.substring(this.name.indexOf("-") + 1));

		Properties ps = new Properties();
		try {
			String conf = SDTPlugin.getTpl(context, "tpl/proj/" + this.type + "/conf.vm");
			ps.load(new ByteArrayInputStream(conf.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ps.isEmpty())
			return new Change[] { new NullChange() };

		createProjectLight();

		Collection<Change> f = new ArrayList<Change>();

		{
			String fromSystem = null;
			if (this.type.equals("web-home")) {
				fromSystem = system + "-web-home";
			} else if (this.type.equals("core-service")) {
				fromSystem = system + "-core-model";
			} else if (this.type.equals("biz-service-impl")) {
				fromSystem = system + "-biz-shared";
			}
			if (fromSystem != null) {
				IFile fromFile = SDTPlugin.getFile("/" + fromSystem + "/.classpath");
				String txt = SDTPlugin.readFile(fromFile);
				StringBuffer b = new StringBuffer(txt);
				int p = txt.indexOf("</classpath>");
				b.insert(p, "  <classpathentry kind=\"src\" path=\"/" + fromSystem + "\"/>\n");

				IFile file = SDTPlugin.getFile("/" + this.name + "/.classpath");
				TextFileChange change = SDTPlugin.createNewFileChange(file, b.toString());

				f.add(change);
			}
		}
		for (Map.Entry entry : ps.entrySet()) {
			String key = entry.getKey().toString();
			IFile file = SDTPlugin.getFile(entry.getValue().toString());
			String txt = SDTPlugin.getTpl(context, "tpl/proj/" + this.type + "/" + key + ".vm");
			TextFileChange change = SDTPlugin.createNewFileChange(file, txt);
			f.add(change);
		}
		{
			CreatePackageChange change = new CreatePackageChange("/" + this.name + "/" + SDTPlugin.D_JAVA,
					defaultPackage);
			f.add(change);
		}
		{
			CreateDirChange change = new CreateDirChange("/" + this.name + "/target/classes");
			f.add(change);
		}
		return f.toArray(new Change[0]);
	}

	private void createProjectLight() {
		IProject project = SDTPlugin.getProject(this.name);
		if (project != null && project.exists()) {
			return;
		}
		try {
			IProjectDescription desc = new ProjectDescription();
			desc.setName(this.name);
			URI uri = new File(this.dir).toURI();
			desc.setLocationURI(uri);
			project.create(desc, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

	}

	public String toString() {
		return "dir:" + dir + ", name:" + name + ", system:" + system + ", type:" + type;
	}

}
