package main.fieldLessAccessible;

public class FieldLessAccessibleMI {

	public void clientPublic() {
		FieldLessAccessible fla = new FieldLessAccessible();
		int c = fla.public2private;
		int a = fla.public2packageprivate;
		int b = fla.public2protected;
		int d = fla.superPublic2Private;
		int e = fla.superPublic2PackagePrivate;
		int f = fla.superPublic2Protected;
		int g = fla.packageprivate2private;
		int h = fla.packageprivate2protected;
		int i = fla.packageprivate2public;
		int j = fla.superPackagePrivate2Private;
		int k = fla.superPackagePrivateToProtected;
	}
}
