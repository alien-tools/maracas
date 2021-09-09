package mainclient.fieldLessAccessible;

import main.fieldLessAccessible.FieldLessAccessible;

public class FieldLessAccessibleFA {

	private FieldLessAccessible f;

	public FieldLessAccessibleFA() {
		f = new FieldLessAccessible();
	}

	public int fieldLessAccessibleClientPub2Pro() {
		return f.public2protected;
	}

	public int fieldLessAccessibleClientPub2PackPriv() {
		return f.public2packageprivate;
	}

	public int fieldLessAccessibleClientPub2Priv() {
		return f.public2private;
	}

	public int fieldLessAccessibleSuperPublic2Private() {
		return f.superPublic2Private;
	}

	public int fieldLessAccessibleSuperPublic2Protected() {
		return f.superPublic2Protected;
	}

	public int fieldLessAccessibleSuperPublic2PackagePrivate() {
		return f.superPublic2PackagePrivate;
	}
}
