package de;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
* Tests basic JavaBean functionality for JavaBean 'Test.java'
*
* @author generated by MOGLiCC
*/
public class TestUnitTest {

	private Test test1;
	private Test test2;

	@Before
	public void setup() {
		final TestBuilder builder = new TestBuilder();
		test1 = builder
		          .build();
		test2 = builder.build();
	}

	@Test
	public void returnsTrueWhenCallingEqualsMethodWithEqualTestInstance() {
		assertTrue("unequal instances", test1.equals(test1));
		assertTrue("unequal instances", test1.equals(test2));
	}

	@Test
	public void buildsEqualHashCodesForEqualTestInstances() {
		assertTrue("unequal instances", test1.hashCode() == test1.hashCode());
		assertTrue("unequal instances", test2.hashCode() == test2.hashCode());
	}





}