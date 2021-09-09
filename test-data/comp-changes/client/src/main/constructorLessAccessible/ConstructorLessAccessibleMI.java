package main.constructorLessAccessible;

import main.constructorLessAccessible.ConstructorLessAccessiblePub2PackPriv;
import main.constructorLessAccessible.ConstructorLessAccessiblePub2Priv;
import main.constructorLessAccessible.ConstructorLessAccessiblePub2Pro;

public class ConstructorLessAccessibleMI {

	public void clientPublic() {
		ConstructorLessAccessiblePub2PackPriv c = new ConstructorLessAccessiblePub2PackPriv();
	}
	
	public void anonymousAccess() {
		ConstructorLessAccessiblePub2PackPriv c = new ConstructorLessAccessiblePub2PackPriv() {};
	}
}
