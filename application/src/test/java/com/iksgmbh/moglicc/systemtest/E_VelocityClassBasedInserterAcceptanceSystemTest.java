package com.iksgmbh.moglicc.systemtest;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.moglicc.inserter.modelbased.velocity.VelocityModelBasedInserterStarter;
import com.iksgmbh.utils.FileUtil;

public class E_VelocityClassBasedInserterAcceptanceSystemTest extends _AbstractSystemTest {
	
	private static final String INSERTER_TEST_EXPECTED_FILE = "expectedInserterResult.txt";
	public static final String INSERTER_PLUGIN_ID = VelocityModelBasedInserterStarter.PLUGIN_ID;
	
	@Before
	@Override
	public void setup() {
		super.setup();
		applicationTempDir.mkdirs();
	}
	
	@Test
	public void createsPluginLogFile() {
		// prepare test
		FileUtil.deleteDirWithContent(applicationLogDir);
		final File pluginLogFile = new File(applicationLogDir, INSERTER_PLUGIN_ID + ".log");
		assertFileDoesNotExist(pluginLogFile);
		
		// call functionality under test
		executeMogliApplication();
		
		// verify test result
		assertFileExists(pluginLogFile);
	}
	
	
	@Test
	public void insertsInTargetFile() {
		// prepare test
		final File targetfile = new File(applicationTestDir, "example/BeanFactory.java");
		targetfile.delete();
		assertFileDoesNotExist(targetfile);

		// call functionality under test
		executeMogliApplication();
		
		// verify test result
		final File expectedFile = new File(getProjectTestResourcesDir(), INSERTER_TEST_EXPECTED_FILE);
		assertFileEquals(expectedFile, targetfile);
	}
	
	@Test
	public void createsHelpData() {
		// prepare test
		final File pluginHelpDir = new File(applicationHelpDir, INSERTER_PLUGIN_ID); 
		FileUtil.deleteDirWithContent(applicationHelpDir);
		assertFileDoesNotExist(pluginHelpDir);
		
		// call functionality under test
		executeMogliApplication();
		
		// verify test result
		assertFileExists(pluginHelpDir);
		assertChildrenNumberInDirectory(pluginHelpDir, 1);
	}
}
