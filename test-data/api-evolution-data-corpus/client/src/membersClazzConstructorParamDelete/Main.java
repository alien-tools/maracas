package membersClazzConstructorParamDelete;

import testing_lib.membersClazzConstructorParamDelete.MembersClazzConstructorParamDelete;

public class Main {

	public static void main(String[] args) {
		Integer param1 = 5;
		String param2 = "";
		MembersClazzConstructorParamDelete constr = new MembersClazzConstructorParamDelete(param1, param2);
		constr.toString();
	}
	
}
