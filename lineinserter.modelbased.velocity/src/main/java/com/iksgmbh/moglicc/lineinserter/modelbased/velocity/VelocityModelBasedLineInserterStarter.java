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
package com.iksgmbh.moglicc.lineinserter.modelbased.velocity;

import static com.iksgmbh.moglicc.generator.utils.GeneratorReportUtil.REPORT_TAB;
import static com.iksgmbh.moglicc.lineinserter.modelbased.velocity.TextConstants.TEXT_END_REPLACE_INDICATOR_NOT_FOUND;
import static com.iksgmbh.moglicc.lineinserter.modelbased.velocity.TextConstants.TEXT_START_REPLACE_INDICATOR_NOT_FOUND;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.iksgmbh.helper.IOEncodingHelper;
import com.iksgmbh.moglicc.core.InfrastructureService;
import com.iksgmbh.moglicc.exceptions.MOGLiPluginException;
import com.iksgmbh.moglicc.generator.GeneratorResultData;
import com.iksgmbh.moglicc.generator.utils.ArtefactListUtil;
import com.iksgmbh.moglicc.generator.utils.EncodingUtils;
import com.iksgmbh.moglicc.generator.utils.GeneratorReportUtil;
import com.iksgmbh.moglicc.generator.utils.GeneratorReportUtil.GeneratorStandardReportData;
import com.iksgmbh.moglicc.generator.utils.ModelMatcherGeneratorUtil;
import com.iksgmbh.moglicc.generator.utils.TemplateUtil;
import com.iksgmbh.moglicc.generator.utils.helper.PluginDataUnpacker;
import com.iksgmbh.moglicc.generator.utils.helper.PluginPackedData;
import com.iksgmbh.moglicc.plugin.subtypes.generators.Inserter;
import com.iksgmbh.moglicc.plugin.subtypes.providers.ModelBasedEngineProvider;
import com.iksgmbh.moglicc.plugin.subtypes.providers.ModelProvider;
import com.iksgmbh.moglicc.provider.engine.velocity.BuildUpVelocityEngineData;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.MetaInfoValidator;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.MetaInfoValidatorVendor;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.validation.MetaInfoValidationUtil;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.validator.ConditionalMetaInfoValidator;
import com.iksgmbh.utils.FileUtil;
import com.iksgmbh.utils.ImmutableUtil;
import com.iksgmbh.utils.StringUtil;

public class VelocityModelBasedLineInserterStarter implements Inserter, MetaInfoValidatorVendor {

	public static final String XML_BUILDER_DIR = "XMLBuilder";
	public static final String CONSOLE_COMIC_STRIP_DIR = "ConsoleComicStrip";
	public static final String PLUGIN_ID = "VelocityModelBasedLineInserter";
	public static final String MODEL_PROVIDER_ID = "StandardModelProvider";
	public static final String ENGINE_PROVIDER_ID = "VelocityEngineProvider";
	public static final String MAIN_TEMPLATE_IDENTIFIER = "Main";
	public static final String PLUGIN_PROPERTIES_FILE = "generator.properties";

	private InfrastructureService infrastructure;
	private ModelBasedEngineProvider velocityEngineProvider;
	private IOEncodingHelper encodingHelper;
	private String targetFileOfCurrentArtefact;
	private int insertCounter = 0;
	private int createdCounter = 0;
	private StringBuffer reportForCurrentArtefact = new StringBuffer();
	private StringBuffer generationReport = new StringBuffer();
	private GeneratorStandardReportData standardReportData = new GeneratorStandardReportData();
	private HashSet<String> artefactsInReport = new HashSet<String>();

	@Override
	public void setInfrastructure(final InfrastructureService infrastructure) {
		this.infrastructure = infrastructure;
	}

	@Override
	public void doYourJob() throws MOGLiPluginException 
	{
		standardReportData.jobStarted = true;
		infrastructure.getPluginLogger().logInfo("Doing my job...");
		velocityEngineProvider = (ModelBasedEngineProvider) infrastructure.getProvider(ENGINE_PROVIDER_ID);
		encodingHelper = null;
		final List<String> list = getArtefactList();  // read before possible exception for model access may be thrown
		standardReportData.numberOfAllInputArtefacts = list.size();
		
		if (standardReportData.numberOfAllInputArtefacts == 0) {
			infrastructure.getPluginLogger().logInfo("No input artefacts . Nothing to do.");
			return;
		}
		
		final ModelProvider modelProvider = (ModelProvider) infrastructure.getProvider(MODEL_PROVIDER_ID);
		
		try
		{
			standardReportData.model = modelProvider.getModel(PLUGIN_ID);
		} catch (MOGLiPluginException e)
		{
			standardReportData.modelError = e.getMessage();
			throw e;
		}		
		
		infrastructure.getPluginLogger().logInfo("Model '" + standardReportData.model.getName() + "' retrieved from " + MODEL_PROVIDER_ID);

		for (final String artefact : list) 
		{
			reportForCurrentArtefact = new StringBuffer(); 
			final File templateDir = new File(infrastructure.getPluginInputDir(), artefact);
			final List<String> mainTemplates = findMainTemplates(templateDir);
			targetFileOfCurrentArtefact = null;
			for (final String mainTemplate : mainTemplates) {
				infrastructure.getPluginLogger().logInfo("-");
				final String targetFileReadFromMainTemplate = applyModelToArtefactTemplate(artefact, templateDir, mainTemplate);				
				validateTargetFileOfCurrentArtefact(artefact, targetFileReadFromMainTemplate);
			}
			generationReport.append(reportForCurrentArtefact);
			infrastructure.getPluginLogger().logInfo("Doing my job...");
		}

		infrastructure.getPluginLogger().logInfo("Done!");
	}

	protected void validateTargetFileOfCurrentArtefact(final String artefact,
			                                           final String targetFileReadFromMainTemplate)
			                                           throws MOGLiPluginException {
		if (targetFileReadFromMainTemplate != null) {
			if (targetFileOfCurrentArtefact == null) {
				targetFileOfCurrentArtefact = targetFileReadFromMainTemplate; // init for first main template
			} else {
				if (! targetFileOfCurrentArtefact.equals(targetFileReadFromMainTemplate)) {
					final String errorMessage = "There are main templates for artefact '" + artefact +
		                                        "' that differ in there targetFileName or targetDir!";
					standardReportData.invalidInputArtefacts.add("Error: " + errorMessage);
					throw new MOGLiPluginException(errorMessage);
				}
			}
		}
	}

	private void generateArtefactReportHeader(final String artefact) 
	{
		standardReportData.numberOfOutputArtefacts++;
		reportForCurrentArtefact.append(FileUtil.getSystemLineSeparator());
		reportForCurrentArtefact.append(GeneratorReportUtil.getArtefactReportLine(artefact));
		reportForCurrentArtefact.append(FileUtil.getSystemLineSeparator());
	}

	private String applyModelToArtefactTemplate(final String artefact, 
			                                    final File templateDir,
			                                    final String mainTemplate) throws MOGLiPluginException
	{
		final boolean doesModelMatch = ModelMatcherGeneratorUtil.doesModelAndTemplateMatch(standardReportData.model.getName(),  
                                                            new File(templateDir, mainTemplate),
                                                            infrastructure.getPluginLogger(), artefact);
		if (! doesModelMatch) 
		{
			if (! standardReportData.nonModelMatchingInputArtefacts.contains(artefact))
					standardReportData.nonModelMatchingInputArtefacts.add(artefact);
			return null;
		}

		
		infrastructure.getPluginLogger().logInfo("Creating code for artefact " + artefact + "...");
		
		final VelocityLineInserterResultData velocityResult = doGenerate(artefact, templateDir, mainTemplate);			

		if (velocityResult.isGenerationToSkip()) {
			infrastructure.getPluginLogger().logInfo("Generation of file '" + velocityResult.getTargetFileName()
							                         + "' was skipped as configured for artefact " + artefact + ".");
			standardReportData.skippedArtefacts.add(velocityResult.getTargetFileName());
			reportForCurrentArtefact= new StringBuffer();
			return null;
		}

		validateResult(artefact, velocityResult);
		handleNumberSignReplacements(velocityResult, velocityResult.getNumberSignReplacement());
		generateOutput(artefact, velocityResult);
		generateReportForResult(artefact, velocityResult);
		infrastructure.getPluginLogger().logInfo("Generated content for artefact '" + artefact
				+ "' inserted into " + velocityResult.getTargetDir() + "/" + velocityResult.getTargetFileName());
		
		return velocityResult.getTargetDir() + "/" + velocityResult.getTargetFileName();
	}

	private void generateOutput(final String artefact,
							    final VelocityLineInserterResultData velocityResult)
							    throws MOGLiPluginException 
	{
		encodingHelper = IOEncodingHelper.getInstance(EncodingUtils.getValidOutputEncodingFormat(velocityResult.getOutputEncodingFormat(),
				                                      infrastructure.getPluginLogger()));
		try
		{
			writeResultIntoPluginOutputDir(velocityResult, artefact);
			writeResultIntoTargetDefinedInTemplate(velocityResult);
		} catch (MOGLiPluginException e)
		{
			standardReportData.invalidInputArtefacts.add("Cannot write output artefact for input artefact '" + artefact + "': " + e.getMessage());
			throw e;
		}
	}

	private VelocityLineInserterResultData doGenerate(final String artefact, 
			                                                final File templateDir,
			                                                final String mainTemplate) throws MOGLiPluginException 
	{
		standardReportData.numberOfModelMatchingInputArtefacts++;
		
		final VelocityLineInserterResultData velocityResult;
		try
		{
			velocityResult = callVelocityEngineProvider(artefact, templateDir, mainTemplate);
		} catch (MOGLiPluginException e)
		{
			standardReportData.invalidInputArtefacts.add("Error parsing artefact properties for artefact '" + artefact + "': " + e.getMessage());
			throw e;
		}
		
		return velocityResult;
	}
	
	private void handleNumberSignReplacements(final VelocityLineInserterResultData velocityResult,
		                                      final String numberSignReplacement)
	{
		if (numberSignReplacement != null)
		{
			String generatedContent = velocityResult.getGeneratedContent();
			generatedContent = StringUtils.replace(generatedContent, numberSignReplacement, "#");
			velocityResult.setGeneratedContent(generatedContent);
		}
	}
	

	private void validateResult(final String artefact,
							    final VelocityLineInserterResultData velocityResult)
							    throws MOGLiPluginException 
	{
		try
		{
			velocityResult.validatePropertyKeys(artefact);
		} catch (MOGLiPluginException e)
		{
			standardReportData.invalidInputArtefacts.add(artefact + " has invalid property keys: " + e.getMessage());
			throw e;
		}
		
		try
		{
			velocityResult.validatePropertyForMissingMetaInfoValues(artefact);
		} catch (MOGLiPluginException e)
		{
			standardReportData.invalidInputArtefacts.add(artefact + " uses invalid metainfos: " + e.getMessage());
			throw e;
		}
	}

	private VelocityLineInserterResultData callVelocityEngineProvider(final String artefact, 
			                                                          final File templateDir,
			                                                          final String mainTemplate) 
			                                                          throws MOGLiPluginException 
	{
		final BuildUpVelocityEngineData engineData = new BuildUpVelocityEngineData(artefact, standardReportData.model, PLUGIN_ID);
		engineData.setTemplateDir(templateDir);
		engineData.setTemplateFileName(mainTemplate);
		
		infrastructure.getPluginLogger().logInfo("Starting velocity engine for artefact '"
													+ engineData.getArtefactType() + " and with '"
													+ engineData.getMainTemplateSimpleFileName() 
													+ "'...");

		velocityEngineProvider.setEngineData(engineData);
		
		final GeneratorResultData generatorResultData;
		try {
			generatorResultData = velocityEngineProvider.startEngineWithModel(); // this does the actual insert job
		} 
		catch (MOGLiPluginException e)
		{
			standardReportData.invalidInputArtefacts.add("Velocity Engine Provider Error:" + e.getMessage());
			throw e;
		}
				
		return new VelocityLineInserterResultData(generatorResultData);
	}

	private void generateReportForResult(final String artefact, 
			                             final VelocityLineInserterResultData resultData) 
	{
		if ( ! artefactsInReport.contains(artefact) )
		{
			generateArtefactReportHeader(artefact);
			artefactsInReport.add(artefact);
		}		
		
		final String generatedContent = resultData.getGeneratedContent().trim();
		final int numLines = StringUtil.getLinesFromText(generatedContent).size();

		if (resultData.isTargetToBeCreatedNewly()) {
			createdCounter++;
			reportForCurrentArtefact.append("      ");
			reportForCurrentArtefact.append(resultData.getTargetFileName());
			reportForCurrentArtefact.append(" was created in ");
			reportForCurrentArtefact.append(GeneratorReportUtil.getTargetDirToDisplay(infrastructure, resultData.getTargetDir()));
			reportForCurrentArtefact.append(FileUtil.getSystemLineSeparator());
			return;
		} else if (resultData.wasExistingTargetPreserved()) {
			reportForCurrentArtefact.append("      ");
			reportForCurrentArtefact.append(resultData.getTargetFileName());
			reportForCurrentArtefact.append(" did already exist and was NOT overwritten in ");
			reportForCurrentArtefact.append(resultData.getTargetDir());
			reportForCurrentArtefact.append(FileUtil.getSystemLineSeparator());
			return;
		} else if (resultData.getInsertAboveIndicator() != null) {
			insertCounter++;
			reportForCurrentArtefact.append("      " + numLines + " line(s) inserted by a above-instruction into "); 
		} else if (resultData.getInsertBelowIndicator() != null) {
			insertCounter++;
			reportForCurrentArtefact.append("      " + numLines + " line(s) inserted by a below-instruction into ");
		} else if (resultData.getReplaceStartIndicator() != null) {
			insertCounter++;
			reportForCurrentArtefact.append("      " + numLines + " line(s) inserted by a replace-instruction into "); 
		}
		reportForCurrentArtefact.append(resultData.getTargetFileName());
		reportForCurrentArtefact.append(" in directory ");
		reportForCurrentArtefact.append(resultData.getTargetDir());
		reportForCurrentArtefact.append(FileUtil.getSystemLineSeparator());
	}

	private void writeResultIntoPluginOutputDir(final VelocityLineInserterResultData resultData, final String subDir)
	             throws MOGLiPluginException {
		final File targetdir = new File(infrastructure.getPluginOutputDir(), subDir);
		targetdir.mkdirs();
		final File outputFile = new File(targetdir, resultData.getTargetFileName());
		try {
			FileUtil.createNewFileWithContent(encodingHelper, outputFile, resultData.getGeneratedContent());
		} catch (Exception e) {
			throw new MOGLiPluginException("Error creating file\n" + outputFile.getAbsolutePath());
		}
	}

	private void writeResultIntoTargetDefinedInTemplate(final VelocityLineInserterResultData resultData)
	             throws MOGLiPluginException {
		final File outputFile = resultData.getTargetFile(infrastructure.getApplicationRootDir().getAbsolutePath(), null);
		infrastructure.getPluginLogger().logInfo("Creating file: " + outputFile.getAbsolutePath());
		final String buildOutputFileContent = buildOutputFileContent(outputFile, resultData);
		if (buildOutputFileContent !=  null) {
			try {
				FileUtil.createNewFileWithContent(encodingHelper, outputFile, buildOutputFileContent);
			} catch (Exception e) {
				throw new MOGLiPluginException("Error creating file\n" + outputFile.getAbsolutePath(), e);
			}
		}
	}

	private String buildOutputFileContent(final File outputFile, final VelocityLineInserterResultData resultData)
	               throws MOGLiPluginException {

		if (resultData.isTargetToBeCreatedNewly()) {
			if (outputFile.exists()) {
				infrastructure.getPluginLogger().logInfo("Old output file will be overwritten:\n"
						+ outputFile.getAbsolutePath());
			} else {
				infrastructure.getPluginLogger().logInfo("Output file will be created:\n"
						+ outputFile.getAbsolutePath());
			}
			return resultData.getGeneratedContent();
		}

		if (resultData.mustGeneratedContentBeMergedWithExistingTargetFile()) {
			if (! outputFile.exists()) {
				throw new MOGLiPluginException("File does not exist:\n" + outputFile.getAbsolutePath());
			}
			infrastructure.getPluginLogger().logInfo("Generated content merged with content of\n"
					                                 + outputFile.getAbsolutePath());
			return mergeOldAndNewContent(outputFile, resultData);
		}

		if (! outputFile.exists()) {
			infrastructure.getPluginLogger().logInfo("An old output file does not exist:\n"
					+ outputFile.getAbsolutePath());
			return resultData.getGeneratedContent();
		}

		infrastructure.getPluginLogger().logWarning("Output file already exists and will not be overwritten:\n"
                + outputFile.getAbsolutePath());
		resultData.setExistingTargetPreserved(true);
		return null;
	}

	private String mergeOldAndNewContent(final File outputFile,
			                           final VelocityLineInserterResultData resultData) throws MOGLiPluginException {
		final List<String> oldContent;
		try {
			oldContent = FileUtil.getFileContentAsList(outputFile);
		} catch (IOException e) {
			throw new MOGLiPluginException("Error creating file\n" + outputFile.getAbsolutePath(), e);
		}

		String generatedContent = resultData.getGeneratedContent();
		final String insertAboveIndicator = resultData.getInsertAboveIndicator();
		if (insertAboveIndicator != null) {
			generatedContent = insertAbove(oldContent, generatedContent, insertAboveIndicator);
			infrastructure.getPluginLogger().logInfo("Generated Content inserted above '"
		            + insertAboveIndicator + "' in\n" + outputFile.getAbsolutePath());
			return generatedContent;
		}

		final String insertBelowIndicator = resultData.getInsertBelowIndicator();
		if (insertBelowIndicator != null) {
			generatedContent = insertBelow(oldContent, generatedContent, insertBelowIndicator);
			infrastructure.getPluginLogger().logInfo("Generated Content inserted below '"
					                                 + insertBelowIndicator + "' in\n" + outputFile.getAbsolutePath());
			return generatedContent;
		}

		final String replaceStartIndicator = resultData.getReplaceStartIndicator();
		if (replaceStartIndicator != null) {
			generatedContent = replace(oldContent, generatedContent, replaceStartIndicator, resultData.getReplaceEndIndicator());
			infrastructure.getPluginLogger().logInfo("Generated Content replaced in\n" + outputFile.getAbsolutePath());
			return generatedContent;
		}

		throw new MOGLiPluginException("Invalid VelocityLineInserterResultData Setting");
	}

	private String replace(final List<String> oldContent, final String contentToInsert,
			final String ReplaceStartIndicator, final String ReplaceEndIndicator) throws MOGLiPluginException {
		final StringBuffer sb = new StringBuffer();
		boolean lineMustBeReplaced = false;
		boolean ReplaceStartFound = false;
		boolean ReplaceEndFound = false;
		for (final String line : oldContent) {
			if (! lineMustBeReplaced) {
				sb.append(line);
				sb.append(FileUtil.getSystemLineSeparator());
			}
			if (line.contains(ReplaceStartIndicator)) {
				lineMustBeReplaced = true;
				sb.append(contentToInsert);
				sb.append(FileUtil.getSystemLineSeparator());
				ReplaceStartFound = true;
			}
			if (line.contains(ReplaceEndIndicator)) {
				lineMustBeReplaced = false;
				sb.append(line);
				sb.append(FileUtil.getSystemLineSeparator());
				ReplaceEndFound = true;
			}
		}
		if (! ReplaceStartFound) {
			throw new MOGLiPluginException(TEXT_START_REPLACE_INDICATOR_NOT_FOUND + ReplaceStartIndicator);
		}
		if (! ReplaceEndFound) {
			throw new MOGLiPluginException(TEXT_END_REPLACE_INDICATOR_NOT_FOUND + ReplaceStartIndicator);
		}

		return sb.toString();
	}

	String insertBelow(final List<String> oldContent, 
			           final String contentToInsert,
			           final String insertBelowIndicator) throws MOGLiPluginException 
    {
		final StringBuffer toReturn = new StringBuffer();
		final StringBuffer contentBelowTheIndicator = new StringBuffer();
		boolean indicatorFound = false;
		
		for (final String line : oldContent) 
		{
			if (indicatorFound)
			{
				contentBelowTheIndicator.append(line);
				contentBelowTheIndicator.append(FileUtil.getSystemLineSeparator());
			}
			toReturn.append(line);
			toReturn.append(FileUtil.getSystemLineSeparator());
			if (line.contains(insertBelowIndicator)) 
			{
				toReturn.append(contentToInsert);
				toReturn.append(FileUtil.getSystemLineSeparator());
				indicatorFound = true;
			}
		}
		
		if (! indicatorFound) {
			throw new MOGLiPluginException(TextConstants.TEXT_INSERT_BELOW_INDICATOR_NOT_FOUND + insertBelowIndicator);
		}
		
		if (contentBelowTheIndicator.toString().contains(contentToInsert))
		{
			// contentToInsert is already present / do not create insertion doubles
			return StringUtil.concat(oldContent).trim();
		}
		
		return toReturn.toString();
	}

	String insertAbove(final List<String> oldContent, 
			           final String contentToInsert,
			           final String insertAboveIndicator) throws MOGLiPluginException 
	{
		final StringBuffer sb = new StringBuffer();
		boolean indicatorFound = false;
		final StringBuffer contentAboveTheIndicator = new StringBuffer();
		
		for (final String line : oldContent) 
		{
			if (! indicatorFound)
			{
				contentAboveTheIndicator.append(line);
				contentAboveTheIndicator.append(FileUtil.getSystemLineSeparator());
			}
			
			if (line.contains(insertAboveIndicator)) {
				sb.append(contentToInsert);
				sb.append(FileUtil.getSystemLineSeparator());
				indicatorFound = true;
			}
			sb.append(line);
			sb.append(FileUtil.getSystemLineSeparator());
		}
		
		if (! indicatorFound) {
			throw new MOGLiPluginException(TextConstants.TEXT_INSERT_ABOVE_INDICATOR_NOT_FOUND + insertAboveIndicator);
		}
		
		if (contentAboveTheIndicator.toString().contains(contentToInsert))
		{
			// contentToInsert is already present / do not create insertion doubles
			return StringUtil.concat(oldContent).trim();
		}
		
		return sb.toString();
	}

	List<String> getArtefactList() throws MOGLiPluginException {
		final File generatorPropertiesFile = new File(infrastructure.getPluginInputDir(), PLUGIN_PROPERTIES_FILE);
		return ArtefactListUtil.getArtefactListFrom(infrastructure.getPluginInputDir(), generatorPropertiesFile);
	}

	List<String> findMainTemplates(final File templateDir) throws MOGLiPluginException {
		return TemplateUtil.findMainTemplates(templateDir, MAIN_TEMPLATE_IDENTIFIER);
	}


	@Override
	public boolean unpackDefaultInputData() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("initDefaultInputData");

		final PluginPackedData defaultData = new PluginPackedData(this.getClass(), DEFAULT_DATA_DIR, PLUGIN_ID);

		final String[] xmlBuilderTemplates = {"CreateTargetFileMain.tpl", "InsertCustomerDataMain.tpl",
				                              "InsertItemsMain.tpl",      "ReplaceAdvertisingDataMain.tpl",
				                              "metaInfoListToXmlAttributes.tpl"};
		defaultData.addFlatFolder(XML_BUILDER_DIR, xmlBuilderTemplates);

		final String[] consoleComicStripTemplates = {"CreateMainScriptTemplate.tpl", "InsertIntoMainTemplate.tpl"};
		defaultData.addFlatFolder(CONSOLE_COMIC_STRIP_DIR, consoleComicStripTemplates);
		
		defaultData.addRootFile(PLUGIN_PROPERTIES_FILE);
		defaultData.addRootFile(MetaInfoValidationUtil.FILENAME_VALIDATION);

		PluginDataUnpacker.doYourJob(defaultData, infrastructure.getPluginInputDir(), infrastructure.getPluginLogger());
		return true;
	}

	@Override
	public PluginType getPluginType() {
		return PluginType.GENERATOR;
	}

	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public List<String> getDependencies() {
		return ImmutableUtil.getImmutableListOf(MODEL_PROVIDER_ID, ENGINE_PROVIDER_ID);
	}

	@Override
	public List<MetaInfoValidator> getMetaInfoValidatorList() throws MOGLiPluginException {
		final File validationInputFile = new File(infrastructure.getPluginInputDir(), MetaInfoValidationUtil.FILENAME_VALIDATION);
		final List<MetaInfoValidator> metaInfoValidatorList = MetaInfoValidationUtil.getMetaInfoValidatorList(validationInputFile, getId());

		for (final MetaInfoValidator metaInfoValidator : metaInfoValidatorList) {
			if (metaInfoValidator instanceof ConditionalMetaInfoValidator) {
				readConditionFileIfNecessary(metaInfoValidator);
			}
		}

		infrastructure.getPluginLogger().logInfo(metaInfoValidatorList.size() + " MetaInfoValidators found.");
		return metaInfoValidatorList;
	}

	private void readConditionFileIfNecessary(final MetaInfoValidator metaInfoValidator) throws MOGLiPluginException {
		final ConditionalMetaInfoValidator conditionalMetaInfoValidator = (ConditionalMetaInfoValidator) metaInfoValidator;
		if (conditionalMetaInfoValidator.getConditionFilename() != null) {
			final File conditionInputFile = new File(infrastructure.getPluginInputDir(), conditionalMetaInfoValidator.getConditionFilename());
			conditionalMetaInfoValidator.setConditionList(MetaInfoValidationUtil.getConditionList(conditionInputFile));
		}
	}

	@Override
	public InfrastructureService getInfrastructure() {
		return infrastructure;
	}

	@Override
	public boolean unpackPluginHelpFiles() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("unpackPluginHelpFiles");
		final PluginPackedData helpData = new PluginPackedData(this.getClass(), HELP_DATA_DIR, PLUGIN_ID);

		helpData.addRootFile(ARTEFACT_PROPERTIES_HELP_FILE);

		PluginDataUnpacker.doYourJob(helpData, infrastructure.getPluginHelpDir(), infrastructure.getPluginLogger());
		return true;
	}

	@Override
	public String getGeneratorReport() 
	{
		standardReportData.additionalReport = FileUtil.getSystemLineSeparator() 
				                              + REPORT_TAB + generationReport.toString().trim();
		return GeneratorReportUtil.getReport(standardReportData); 
	}

	@Override
	public int getNumberOfGenerations() {
		return insertCounter + createdCounter;
	}

	@Override
	public int getNumberOfGeneratedArtefacts() {
		return standardReportData.numberOfOutputArtefacts;
	}
	
	@Override
	public String getShortReport()
	{
		final String standardReport = GeneratorReportUtil.getShortReport(standardReportData);

		if (! StringUtils.isEmpty(standardReport)) {
			return standardReport;
		}
		
		return "From " + standardReportData.numberOfAllInputArtefacts + " input artefact(s), have been " 
		        + standardReportData.numberOfOutputArtefacts + " used to create " + createdCounter + " file(s) and to perform " 
		        + insertCounter + " insertion(s).";
	}

	@Override
	public int getSuggestedPositionInExecutionOrder()
	{
		return 500;
	}
}