package mainclient.methodLessAccessible;

import main.methodLessAccessible.MethodLessAccessible;

public class MethodLessAccessibleMISubtype extends MethodLessAccessible {

	public int methodLessAccessiblePub2ProClientSuperKeyAccess() {
		return super.methodLessAccessiblePublic2Protected();
	}
	
	public int methodLessAccessiblePub2PackPrivClientSuperKeyAccess() {
		return super.methodLessAccessiblePublic2PackPriv();
	}
	
	public int methodLessAccessiblePub2PrivClientSuperKeyAccess() {
		return super.methodLessAccessiblePublic2Private();
	}
	
	public int methodLessAccessiblePro2PubClientSuperKeyAccess() {
		return super.methodLessAccessibleProtected2Public();
	}
	
	public int methodLessAccessiblePro2PackPrivClientSuperKeyAccess() {
		return super.methodLessAccessibleProtected2PackPriv();
	}
	
	public int methodLessAccessiblePro2PrivClientSuperKeyAccess() {
		return super.methodLessAccessibleProtected2Private();
	}
	
	public int methodLessAccessiblePub2ProClientNoSuperKeyAccess() {
		return methodLessAccessiblePublic2Protected();
	}
	
	public int methodLessAccessiblePub2PackPrivClientNoSuperKeyAccess() {
		return methodLessAccessiblePublic2PackPriv();
	}
	
	public int methodLessAccessiblePub2PrivClientNoSuperKeyAccess() {
		return methodLessAccessiblePublic2Private();
	}
	
	public int methodLessAccessiblePro2PubClientNoSuperKeyAccess() {
		return methodLessAccessibleProtected2Public();
	}
	
	public int methodLessAccessiblePro2PackPrivClientNoSuperKeyAccess() {
		return methodLessAccessibleProtected2PackPriv();
	}
	
	public int methodLessAccessiblePro2PrivClientNoSuperKeyAccess() {
		return methodLessAccessibleProtected2Private();
	}

}
