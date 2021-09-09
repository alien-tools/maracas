package main.tests.methodRemoved;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class MethodRemoved {

	@Test
	public int methodTest() {
		return 0;
	}
	
	@Ignore
	public int methodIgnore() {
		return 1;
	}
	
	@After
	public int methodAfter() {
		return 2;
	}
	
	@AfterClass
	public int methodAfterClass() {
		return 3;
	}
	
	@Before
	public int methodBefore() {
		return 4;
	}
	
	@BeforeClass
	public int methodBeforeClass() {
		return 5;
	}
}
