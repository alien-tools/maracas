package main.unused.interfaceAdded;

import java.util.ArrayList;
import java.util.List;

public interface IInterfaceAdded {

	public static final int CTE = 0;
	public static final List<String> LIST = new ArrayList<String>();
	
	int methodAbs();
	
	static int staticMeth() {
		return 0;
	}
	
	default int defaultMeth() {
		return 1;
	}
}
