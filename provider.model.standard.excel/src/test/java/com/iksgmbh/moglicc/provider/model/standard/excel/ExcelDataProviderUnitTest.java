/*
 * Copyright 2016 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.moglicc.provider.model.standard.excel;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.moglicc.exceptions.MOGLiPluginException;
import com.iksgmbh.moglicc.provider.model.standard.excel.ExcelDataProvider.ExcelData;
import com.iksgmbh.moglicc.test.ExcelStandardModelProviderTestParent;
import com.iksgmbh.utils.FileUtil;

public class ExcelDataProviderUnitTest extends ExcelStandardModelProviderTestParent
{
	
	@Before
	public void setup()
	{
		super.setup();
		createPropertiesFileWith("");
		final File testdatafile = new File(PROJECT_ROOT_DIR + "src/test/resources/" + TESTFILENAME);
		System.out.println(testdatafile.getAbsolutePath());
		assertFileExists(testdatafile);
	}
	
	@Test
	public void parsesCellDataWithNumbers() throws Exception
	{
		// arrange
		final ExcelStandardModelProviderStarter excelStandardModelProviderStarter = new ExcelStandardModelProviderStarter();
		excelStandardModelProviderStarter.setInfrastructure(infrastructure);
		final ExcelDataProvider excelDataProvider = new ExcelDataProvider(excelStandardModelProviderStarter.readPluginProperties());
		final ExcelData excelMetaData = new ExcelData();
		
		// act 
		excelDataProvider.parseCellData("1:2", excelMetaData);
		
		// assert 
		assertEquals("colNo", 1, excelMetaData.firstCells.get(new Integer(0)).colNo);
		assertEquals("rowNo", 2, excelMetaData.firstCells.get(new Integer(0)).rowNo);
	}
	
	@Test
	public void parsesCellDataWithLetters() throws Exception
	{
		// arrange
		final ExcelStandardModelProviderStarter excelStandardModelProviderStarter = new ExcelStandardModelProviderStarter();
		excelStandardModelProviderStarter.setInfrastructure(infrastructure);
		final ExcelDataProvider excelDataProvider = new ExcelDataProvider(excelStandardModelProviderStarter.readPluginProperties());
		final ExcelData excelMetaData = new ExcelData();
		
		// act 
		excelDataProvider.parseCellData("F:12", excelMetaData);
		
		// assert 
		assertEquals("colNo", 6, excelMetaData.firstCells.get(new Integer(0)).colNo);
		assertEquals("rowNo", 12, excelMetaData.firstCells.get(new Integer(0)).rowNo);
	}
	
	
	@Test
	public void readsExcelMetaDataFromPluginProperties() throws MOGLiPluginException
	{
		// arrange
		final String filename = "TestDataExample.xlsx";
		propertiesFile = new File(infrastructure.getPluginInputDir(), ExcelStandardModelProviderStarter.PLUGIN_PROPERTIES_FILE);
		final ExcelStandardModelProviderStarter excelStandardModelProviderStarter = new ExcelStandardModelProviderStarter();
		excelStandardModelProviderStarter.setInfrastructure(infrastructure);
		FileUtil.copyBinaryFile(PROJECT_ROOT_DIR + "src/test/resources/" + filename, excelStandardModelProviderStarter.getInfrastructure().getPluginInputDir().getAbsolutePath());

		createPropertiesFileWith("TestModel.filename=" + filename + System.getProperty("line.separator") +  
				                 "TestModel.matrix.1=1#1:1" + System.getProperty("line.separator") + 
				                 "TestModel.matrix.2=1#2:10" + System.getProperty("line.separator") + 
				                 "TestModel.matrix.3=2#2:3");
		excelStandardModelProviderStarter.doYourJob();
		final ExcelDataProvider excelDataProvider = new ExcelDataProvider(excelStandardModelProviderStarter.readPluginProperties());
		
		// act
		final ExcelData result = excelDataProvider.readExcelMetaDataFromPluginProperties("TestModel", 
				                                   excelStandardModelProviderStarter.getInfrastructure().getPluginInputDir());

		// assert
		assertNotNull("Not null expected ", result);
		assertEquals("excelFileName", "TestDataExample.xlsx", result.excelFile.getName());
		
		assertEquals("sheetNumbers size", 3, result.sheetNumbers.size());
		assertEquals("sheetNumber of matrix 1", 1, result.sheetNumbers.get(0).intValue());
		assertEquals("sheetNumber of matrix 2", 1, result.sheetNumbers.get(1).intValue());
		assertEquals("sheetNumber of matrix 3", 2, result.sheetNumbers.get(2).intValue());
		assertEquals("sheetNumbers size", 3, result.sheetNames.size());
		assertEquals("sheetName of matrix 3", "SecondSheet", result.sheetNames.get(2));
		
		assertEquals("cell coordinate col of matrix 1", 1, result.firstCells.get(1).colNo);
		assertEquals("cell coordinate row of matrix 1", 1, result.firstCells.get(1).rowNo);
		assertEquals("cell coordinate col of matrix 2", 2, result.firstCells.get(2).colNo);
		assertEquals("cell coordinate row of matrix 2", 10, result.firstCells.get(2).rowNo);
		assertEquals("cell coordinate col of matrix 3", 2, result.firstCells.get(3).colNo);
		assertEquals("cell coordinate row of matrix 3", 3, result.firstCells.get(3).rowNo);
	}
	
	@Test
	public void readsSubsetOfAttributesFromMatrix() throws MOGLiPluginException
	{
		// arrange
		final String filename = "TestDataExample.xlsx";
		propertiesFile = new File(infrastructure.getPluginInputDir(), ExcelStandardModelProviderStarter.PLUGIN_PROPERTIES_FILE);
		final ExcelStandardModelProviderStarter excelStandardModelProviderStarter = new ExcelStandardModelProviderStarter();
		excelStandardModelProviderStarter.setInfrastructure(infrastructure);
		FileUtil.copyBinaryFile(PROJECT_ROOT_DIR + "src/test/resources/" + filename, excelStandardModelProviderStarter.getInfrastructure().getPluginInputDir().getAbsolutePath());

		createPropertiesFileWith("TestModel.filename=" + filename + System.getProperty("line.separator") +  
				                 "TestModel.matrix.3=2#2:3>21-34");
		excelStandardModelProviderStarter.doYourJob();
		final ExcelDataProvider excelDataProvider = new ExcelDataProvider(excelStandardModelProviderStarter.readPluginProperties());
		
		// act
		final ExcelData result = excelDataProvider.readExcelMetaDataFromPluginProperties("TestModel", 
				                                   excelStandardModelProviderStarter.getInfrastructure().getPluginInputDir());

		// assert
		assertNotNull("Not null expected ", result);
		assertEquals("minIndex", 21, result.attributeSubset.get(1).minIndex);
		assertEquals("maxIndex", 34, result.attributeSubset.get(1).maxIndex);
	}

	@Test
	public void readsSingleAttributeAsSubsetFromMatrix() throws MOGLiPluginException
	{
		// arrange
		final String filename = "TestDataExample.xlsx";
		propertiesFile = new File(infrastructure.getPluginInputDir(), ExcelStandardModelProviderStarter.PLUGIN_PROPERTIES_FILE);
		final ExcelStandardModelProviderStarter excelStandardModelProviderStarter = new ExcelStandardModelProviderStarter();
		excelStandardModelProviderStarter.setInfrastructure(infrastructure);
		FileUtil.copyBinaryFile(PROJECT_ROOT_DIR + "src/test/resources/" + filename, excelStandardModelProviderStarter.getInfrastructure().getPluginInputDir().getAbsolutePath());

		createPropertiesFileWith("TestModel.filename=" + filename + System.getProperty("line.separator") +  
				                 "TestModel.matrix.3=2#2:3>29");
		excelStandardModelProviderStarter.doYourJob();
		final ExcelDataProvider excelDataProvider = new ExcelDataProvider(excelStandardModelProviderStarter.readPluginProperties());
		
		// act
		final ExcelData result = excelDataProvider.readExcelMetaDataFromPluginProperties("TestModel", 
				                                   excelStandardModelProviderStarter.getInfrastructure().getPluginInputDir());

		// assert
		assertNotNull("Not null expected ", result);
		assertEquals("minIndex", 29, result.attributeSubset.get(1).minIndex);
		assertEquals("maxIndex", 29, result.attributeSubset.get(1).maxIndex);
	}
	
}