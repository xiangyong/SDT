package sdt.wizards.change;

public class CreatePackageChange extends CreateDirChange {

	private String root;
	private String pkg;

	public CreatePackageChange(String root, String pkg) {
		super(root + "/" + pkg.replaceAll("[.]", "/"));
		this.root = root;
		this.pkg = pkg;
	}

	public String getName() {
		return pkg + " - " + root;
	}

}
