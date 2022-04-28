package membersClazzNestedClazzDelete;

import testing_lib.membersClazzNestedClazzDelete.MembersClazzNestedClazzDelete;

public class Main {

	public static void main(String[] args) {
		MembersClazzNestedClazzDelete constr = new MembersClazzNestedClazzDelete();
		MembersClazzNestedClazzDelete.NestedClazz constrNestedClazz = constr.new NestedClazz();
		
		constrNestedClazz.toString();
	}
	
}
