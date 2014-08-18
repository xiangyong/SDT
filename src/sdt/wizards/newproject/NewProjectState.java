package sdt.wizards.newproject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.corext.refactoring.changes.CreatePackageChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import sdt.NameUtil;
import sdt.SDTPlugin;
import sdt.wizards.NewWizardState;

@SuppressWarnings("restriction")
public class NewProjectState implements NewWizardState {
	public String dir;
	public String system;
	public String name;
	public String type;

	@SuppressWarnings("unchecked")
	@Override
	public Change[] computeChanges() {
		VelocityContext context = new VelocityContext();
		context.put("system", this.system);
		context.put("project", this.name);

		String projectSimple = this.name.substring(this.name.lastIndexOf("-") + 1);
		context.put("projectSimple", projectSimple);
		context.put("security", "security-" + projectSimple);
		context.put("projectBundleName", NameUtil.cap(this.name.replaceAll("-", " ")));

		String defaultPackage = "com.alipay." + this.name.replaceAll("-", ".");
		context.put("projectSymbolicName", defaultPackage);
		context.put("projectSpring", this.name.substring(this.name.indexOf("-") + 1));
		context.put("projectBundleWebName", this.name.substring(this.name.lastIndexOf("-") + 1));

		createProject();

		Properties ps = new Properties();
		try {
			String conf = SDTPlugin.getTpl(context, "tpl/proj/" + this.type + "/conf.vm");
			ps.load(new ByteArrayInputStream(conf.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		int i = 0, l = ps.size();
		if (l == 0)
			return new Change[] { new NullChange() };

		Change[] f = new Change[l + 1 + 1];
		for (Map.Entry entry : ps.entrySet()) {
			String key = entry.getKey().toString();
			IFile file = SDTPlugin.getTargetFile(entry.getValue().toString());
			String txt = SDTPlugin.getTpl(context, "tpl/proj/" + this.type + "/" + key + ".vm");
			TextFileChange change = SDTPlugin.createNewFileChange(file, txt);
			f[i++] = change;
		}

		// {
		// String tName = getTemplateProjectName();
		// IFile tPom = SDTPlugin.getTargetFile(tName + "/pom.xml");
		// String tPomTxt = SDTPlugin.readFile(tPom);
		// String txt = tPomTxt.replaceAll(tName, this.name);
		//
		// IFile file = SDTPlugin.getTargetFile(this.name + "/pom.xml");
		// TextFileChange change = SDTPlugin.createNewFileChange(file, txt);
		// f[i++] = change;
		// }

		{
			IProject project = SDTPlugin.getProject(this.name);
			IJavaProject jp = JavaCore.create(project);
			try {
				final IPackageFragmentRoot root = jp.findPackageFragmentRoot(new Path("/" + this.name + "/"
						+ SDTPlugin.D_JAVA));
				final IPackageFragment pkg = root.createPackageFragment(defaultPackage, true, null);
				CreatePackageChange change = new CreatePackageChange(pkg) {
					public String getName() {
						return pkg.getElementName() + " - " + root.getPath().makeRelative();
					}
				};
				f[i++] = change;
			} catch (JavaModelException e) {
				e.printStackTrace();
			}

		}

		return f;
	}

	private void createProject() {
		IProject project = SDTPlugin.getProject(this.name);

		String tplProjectName = getTemplateProjectName();
		IProject tProject = SDTPlugin.getProject(tplProjectName);

		try {
			IProjectDescription desc = tProject.getDescription();
			URI uri = new File(this.dir).toURI();
			desc.setLocationURI(uri);
			project.create(desc, null);
			project.open(IResource.BACKGROUND_REFRESH, null);

			tProject.getFile(".classpath").copy(project.getFile(".classpath").getFullPath(), true, null);
			IJavaProject jp = JavaCore.create(project);
			if (this.name.contains("-web-")) {
				IClasspathEntry[] jpEntries = jp.getRawClasspath();

				IClasspathEntry home = new ClasspathEntry( //
						IPackageFragmentRoot.K_SOURCE,// contentKind
						ClasspathEntry.CPE_PROJECT, // entryKind
						new Path("/" + NameUtil.firstString(this.name, '-') + "-web-home"), // path
						new IPath[0], // inclusionPatterns
						new IPath[0], // exclusionPatterns
						null, // sourceAttachmentPath
						null, // sourceAttachmentRootPath
						null, // specificOutputLocation
						false, // isExported
						new IAccessRule[0], // accessRules
						true,// combineAccessRules
						new IClasspathAttribute[0] // extraAttributes
				);
				IClasspathEntry[] entries = new IClasspathEntry[jpEntries.length + 1];
				for (int i = 0; i < jpEntries.length; i++) {
					entries[i] = jpEntries[i];
				}
				entries[jpEntries.length] = home;

				jp.setRawClasspath(entries, null);
			}

			project.getFolder("src").create(true, true, null);
			project.getFolder("src/main").create(true, true, null);
			project.getFolder("src/main/java").create(true, true, null);
			project.getFolder("src/main/resources").create(true, true, null);
			project.getFolder("target").create(true, true, null);
			project.getFolder("target/classes").create(true, true, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getTemplateProjectName() {
		return this.system + "-" + this.type;
	}

	public NewProjectState() {
		Velocity.init();
	}

	public String toString() {
		return "dir:" + dir + ", name:" + name + ", system:" + system + ", type:" + type;
	}

}
