package sdt.wizards.change;

import sdt.SDTPlugin;

public class CreatePackageChange extends CreateDirChange {

	private String root;
	private String pkg;

	public CreatePackageChange(String root, String pkg) {
		super(null);
		folder = SDTPlugin.getFolder(root + "/" + pkg.replaceAll("[.]", "/"));
		this.root = root;
		this.pkg = pkg;
	}

	public String getName() {
		return pkg + " - " + root;
	}

}
