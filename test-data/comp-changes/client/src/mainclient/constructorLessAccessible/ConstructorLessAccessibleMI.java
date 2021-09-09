package mainclient.constructorLessAccessible;

import main.constructorLessAccessible.ConstructorLessAccessiblePub2PackPriv;
import main.constructorLessAccessible.ConstructorLessAccessiblePub2Priv;
import main.constructorLessAccessible.ConstructorLessAccessiblePub2Pro;

public class ConstructorLessAccessibleMI {

	public void clientPublic() {
		ConstructorLessAccessiblePub2Pro c1 = new ConstructorLessAccessiblePub2Pro();
		ConstructorLessAccessiblePub2PackPriv c2 = new ConstructorLessAccessiblePub2PackPriv();
		ConstructorLessAccessiblePub2Priv c3 = new ConstructorLessAccessiblePub2Priv();
	}
	
}
