package com.iksgmbh.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class StringUtilUnitTest {
	
	@Test
	public void testExtractCommaSeparatedPluginList() {
		final String sep = FileUtil.getSystemLineSeparator();
		final String s = "# comment 1" + sep + "plugin1, plugin2" + sep + sep + "# comment 2" + sep + "plugin 3" + sep + "#comment 3";
		final String actual = StringUtil.removeBlankAndCommentLines(s, '#');
		final String expected = "plugin1, plugin2, plugin 3";
		assertEquals("Comment line removed incorrectly", expected, actual);		
	}
	
	@Test
	public void testGetListFromLineWithCommaSeparatedElements() {
		// test 1
		String[] pluginList = StringUtil.getListFromLineWithCommaSeparatedElements(" ");
		assertEquals(pluginList.length, 0);
		
		// test 2
		final String sep = FileUtil.getSystemLineSeparator();
		final String s = "# comment 1" + sep + "plugin1, plugin2" + sep + sep + "# comment 2" + sep + "plugin 3" + sep + "#comment 3";
		final String actual = StringUtil.removeBlankAndCommentLines(s, '#');
		pluginList = StringUtil.getListFromLineWithCommaSeparatedElements(actual);
		assertEquals(pluginList.length, 3);
		assertEquals("Unexpected plugin list. ", "plugin1", pluginList[0]);
		assertEquals("Unexpected plugin list. ", "plugin2", pluginList[1]);
		assertEquals("Unexpected plugin list. ", "plugin3", pluginList[2]);
	}

	@Test
	public void testConcat() {
		final List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		final String result = StringUtil.concat(list);
		assertEquals("result ", "a" + FileUtil.getSystemLineSeparator() + "b", result );
	}
}
