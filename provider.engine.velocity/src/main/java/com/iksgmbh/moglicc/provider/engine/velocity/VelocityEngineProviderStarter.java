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
package com.iksgmbh.moglicc.provider.engine.velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

import com.iksgmbh.moglicc.core.InfrastructureService;
import com.iksgmbh.moglicc.data.BuildUpGeneratorResultData;
import com.iksgmbh.moglicc.exceptions.MOGLiPluginException;
import com.iksgmbh.moglicc.generator.GeneratorResultData;
import com.iksgmbh.moglicc.generator.utils.helper.PluginDataUnpacker;
import com.iksgmbh.moglicc.generator.utils.helper.PluginPackedData;
import com.iksgmbh.moglicc.plugin.subtypes.providers.ClassBasedEngineProvider;
import com.iksgmbh.moglicc.plugin.subtypes.providers.ModelBasedEngineProvider;
import com.iksgmbh.moglicc.provider.engine.velocity.helper.MergeResultAnalyser;
import com.iksgmbh.moglicc.provider.engine.velocity.helper.VelocityBugCorrector;
import com.iksgmbh.moglicc.provider.model.standard.ClassDescriptor;
import com.iksgmbh.moglicc.provider.model.standard.Model;
import com.iksgmbh.utils.FileUtil;
import com.iksgmbh.utils.ImmutableUtil;

public class VelocityEngineProviderStarter implements ClassBasedEngineProvider, ModelBasedEngineProvider {

	public static final String ENGINE_STARTED_WITHOUT_DATA = "Velocity Engine started without VelocityEngineData set.";
	public static final String TEMPLATE_REFERENCE_MODEL = "model";
	public static final String TEMPLATE_REFERENCE_CLASS_DESCRIPTOR = "classDescriptor";
	public static final String PLUGIN_ID = "VelocityEngineProvider";
	public static final String DEFAULT_FILE_EXTENSION = ".txt";
	
	private final static String[] API_IMAGES = { "background.gif", "titlebar_end.gif", "titlebar.gif", "tab.gif" };
	private final static String[] JAVA_DOCS = { "TemplateJavaUtility.html", "TemplateStringUtility.html" };
	
	private InfrastructureService infrastructure;
	private VelocityEngineData velocityEngineData;
	private Model model;
	private int callCountsModelBasedFullGeneration = 0;
	private int callCountsClassBasedFullGeneration = 0;
	final StringBuffer modelBasedReportEntries = new StringBuffer();
	final StringBuffer classBasedReportEntries = new StringBuffer();

	@Override
	public PluginType getPluginType() {
		return PluginType.PROVIDER;
	}

	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public List<String> getDependencies() {
		return ImmutableUtil.getImmutableListOf();
	}

	@Override
	public void setInfrastructure(InfrastructureService infrastructure) {
		this.infrastructure = infrastructure;
	}

	@Override
	public Object startEngine() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("startEngine called");
		return startEngineWithClassList();
	}

	@Override
	public void doYourJob() throws MOGLiPluginException {
		// engine providers have nothing to do here (see startEngine)
	}

	@Override
	public GeneratorResultData startEngineWithModel() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("startEngineAllClassesIntoSingleTargetFile called");
		prepareStart(true);

		final VelocityContext context = getVelocityContextWith(model);
		final String mergeResult = mergeTemplateWith(context);

		final BuildUpGeneratorResultData buildUpGeneratorResultData;
		try {
			buildUpGeneratorResultData = MergeResultAnalyser.doYourJob(mergeResult, getVelocityEngineData().getArtefactType());
			VelocityBugCorrector.doYourJob(buildUpGeneratorResultData, getMainTemplateFileContentAsList());
		} catch (MOGLiPluginException e) {
			infrastructure.getPluginLogger().logError(e.getMessage());
			throw e;
		}

        infrastructure.getPluginLogger().logInfo("GeneratorResultData created for model merged with template '"
        		                                  + velocityEngineData.getMainTemplateSimpleFileName() + "'");

		infrastructure.getPluginLogger().logInfo("-----");
		return buildUpGeneratorResultData;
	}

	private List<String> getMainTemplateFileContentAsList() throws MOGLiPluginException {
		final File f = new File(velocityEngineData.getTemplateDir(), velocityEngineData.getMainTemplateSimpleFileName());		
		try {
			return FileUtil.getFileContentAsList(f);
		} catch (IOException e) {
			throw new MOGLiPluginException("Error reading " + f.getAbsolutePath(), e);
		}
	}

	@Override
	public List<GeneratorResultData> startEngineWithClassList() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("startEngineEachClassIntoSeparateTargetFiles called");
		prepareStart(false);

		final List<GeneratorResultData> toReturn = new ArrayList<GeneratorResultData>();
		for (int i = 0; i < model.getSize(); i++) {
			final ClassDescriptor clDescr = (ClassDescriptor) model.getClassDescriptorList().get(i);
			final VelocityContext context = getVelocityContextWith(clDescr, model);
			final String mergeResult = mergeTemplateWith(context);
			final BuildUpGeneratorResultData buildUpGeneratorResultData = MergeResultAnalyser.doYourJob(mergeResult,
					                                                 getVelocityEngineData().getArtefactType());
			VelocityBugCorrector.doYourJob(buildUpGeneratorResultData, getMainTemplateFileContentAsList());
	        infrastructure.getPluginLogger().logInfo("GeneratorResultData created for merging class '"
	        		 + clDescr.getSimpleName() + "' with template '"
	        		 + velocityEngineData.getMainTemplateSimpleFileName() + "'");
	        toReturn.add(buildUpGeneratorResultData);
		}
		infrastructure.getPluginLogger().logInfo("-----");
		return toReturn;
	}

	private void prepareStart(final boolean modelBased) throws MOGLiPluginException {
		if (velocityEngineData == null) {
			throw new MOGLiPluginException(ENGINE_STARTED_WITHOUT_DATA);
		}
		
		infrastructure.getPluginLogger().logInfo("-");
		infrastructure.getPluginLogger().logInfo("Engine started from " + velocityEngineData.getGeneratorPluginId());
		infrastructure.getPluginLogger().logInfo("MainTemplate: " + velocityEngineData.getTemplateDir().getAbsolutePath() 
				                                  + "/" + velocityEngineData.getMainTemplateSimpleFileName());
		
		model = velocityEngineData.getModel();
		
		if (modelBased) {
			modelBasedReportEntries.append("from " + velocityEngineData.getGeneratorPluginId() + " on main template '" 
					+ velocityEngineData.getArtefactType() + "' on template '" 
					+ velocityEngineData.getMainTemplateSimpleFileName() + "'");
			modelBasedReportEntries.append(FileUtil.getSystemLineSeparator());
			callCountsModelBasedFullGeneration++;
		} else {
			classBasedReportEntries.append("from " + velocityEngineData.getGeneratorPluginId() + " for artefact '"
					+ velocityEngineData.getArtefactType() + "' on main template '" 
					+ velocityEngineData.getMainTemplateSimpleFileName() + "'");
			classBasedReportEntries.append(FileUtil.getSystemLineSeparator());
			callCountsClassBasedFullGeneration++;
		}

		infrastructure.getPluginLogger().logInfo("Model " + model.getName() + " received from "
				                                 + velocityEngineData.getGeneratorPluginId());
	}

	VelocityContext getVelocityContextWith(final Object... objectsToAdd) {
		final VelocityContext context = new VelocityContext();
		for (final Object object : objectsToAdd) {
			if (object instanceof Model) {
				context.put(TEMPLATE_REFERENCE_MODEL, object);
			} else {
				context.put(TEMPLATE_REFERENCE_CLASS_DESCRIPTOR, object);
			}
		}
        context.put("TemplateStringUtility", new TemplateStringUtility());
        context.put("TemplateJavaUtility", new TemplateJavaUtility());
		return context;
	}

	String mergeTemplateWith(final VelocityContext context) throws MOGLiPluginException {
        final VelocityEngine engine = getVelocityEngine();
        final StringWriter writer = new StringWriter();
        final Template template = createVelocityTemplate(engine);
        try {
        	template.merge(context, writer); // create new content from template and class from model
        } catch (Exception e) {
        	throw new MOGLiPluginException("Velocity Error: " + e.getMessage());
        }
        writer.flush();
        return writer.toString();
	}

	private Template createVelocityTemplate(final VelocityEngine engine) throws MOGLiPluginException {
		final Template template;
		try {
        	 template = engine.getTemplate(velocityEngineData.getMainTemplateSimpleFileName(), "UTF-8");
		} catch (ResourceNotFoundException e) {
			final String templateDirAsString = velocityEngineData.getTemplateDir().getAbsolutePath();
			throw new MOGLiPluginException("Error finding template file:\n"
					+ velocityEngineData.getMainTemplateSimpleFileName()
					+ "\nRootDir: " + templateDirAsString, e);
		}
		return template;
	}


	private VelocityEngine getVelocityEngine() {
		final String templateDirAsString = velocityEngineData.getTemplateDir().getAbsolutePath();
		final String pluginInputDirAsString = velocityEngineData.getTemplateDir().getParentFile().getAbsolutePath();
		final String mogliccInputDirAsString = velocityEngineData.getTemplateDir().getParentFile().getParentFile().getAbsolutePath();
		final Properties velocityEngineProperties = new Properties();
        velocityEngineProperties.setProperty("output.encoding", "UTF-8");
        velocityEngineProperties.setProperty("input.encoding", "UTF-8");
        velocityEngineProperties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        velocityEngineProperties.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
        									 templateDirAsString + ", " + 
                                             pluginInputDirAsString + ", " + 
                                             mogliccInputDirAsString);

        final VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init(velocityEngineProperties);

		return velocityEngine;
	}

	@Override
	public boolean unpackDefaultInputData() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("Nothing to do in initDefaultInputData!");
		return false;
	}

	@Override
	public void setEngineData(final Object engineData) throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("-----");
		if (engineData == null) {
			throw new MOGLiPluginException("Parameter 'engineData' must not be null!");
		}
		final VelocityEngineData velocityEngineData;

		if (engineData instanceof VelocityEngineData) {
			velocityEngineData = (VelocityEngineData) engineData;
		} else {
			throw new MOGLiPluginException("VelocityEngineData expected! Wrong engine data set: " + engineData.getClass().getName());
		}

		if (velocityEngineData.getModel() == null) {
			throw new MOGLiPluginException("Model not set!");
		}

		if (velocityEngineData.getGeneratorPluginId() == null) {
			throw new MOGLiPluginException("GeneratorPluginId not set!");
		}

		if (infrastructure.getGenerator(velocityEngineData.getGeneratorPluginId()) == null) {
			throw new MOGLiPluginException("Unknown GeneratorPlugin!");
		}

		if (StringUtils.isEmpty(velocityEngineData.getArtefactType())) {
			throw new MOGLiPluginException("ArtefactType not set!");
		}

		if (velocityEngineData.getMainTemplateSimpleFileName() == null) {
			throw new MOGLiPluginException("MainTemplateName not set!");
		}

		if (velocityEngineData.getTemplateDir() == null) {
			throw new MOGLiPluginException("TemplateDir not set!");
		}

		final File templateDir = velocityEngineData.getTemplateDir();
		if (! templateDir.exists()) {
			throw new MOGLiPluginException("TemplateDir does not exist:\n"
					                            + templateDir.getAbsolutePath());
		}

		final File mainTemplateFile = new File(templateDir,
				                               velocityEngineData.getMainTemplateSimpleFileName());
		if (! mainTemplateFile.exists()) {
			throw new MOGLiPluginException("Main Template File does not exist:\n"
					   + mainTemplateFile.getAbsolutePath());
		}

		this.velocityEngineData = velocityEngineData;
		infrastructure.getPluginLogger().logInfo("Setting velocity engine data:\n '"
				+ velocityEngineData + "'...");

	}

	@Override
	public InfrastructureService getInfrastructure() {
		return infrastructure;
	}

	@Override
	public boolean unpackPluginHelpFiles() throws MOGLiPluginException {
		infrastructure.getPluginLogger().logInfo("unpackPluginHelpFiles");
		final PluginPackedData helpData = new PluginPackedData(this.getClass(), HELP_DATA_DIR, PLUGIN_ID);
		helpData.addFile("TemplateUtilities.htm");
		helpData.addFile("stylesheet.css", "apidocs");
		helpData.addFlatFolder("apidocs/resources", API_IMAGES);
		helpData.addFlatFolder("apidocs/com/iksgmbh/moglicc/provider/engine/velocity", JAVA_DOCS);
		PluginDataUnpacker.doYourJob(helpData, infrastructure.getPluginHelpDir(), infrastructure.getPluginLogger());
		return true;
	}

	/**
	 * FOR TEST PURPOSE ONLY
	 */
	VelocityEngineData getVelocityEngineData() {
		return velocityEngineData;
	}

	@Override
	public String getProviderReport()
	{
		if (getNumberOfCalls() == 0)
		{
			return "not used for code generation";
		}
		return callCountsModelBasedFullGeneration + " time(s) called in full generation model based mode: "
			   + FileUtil.getSystemLineSeparator()
			   + modelBasedReportEntries.toString().trim()
			   + FileUtil.getSystemLineSeparator()
			   + FileUtil.getSystemLineSeparator()
	           + callCountsClassBasedFullGeneration + " time(s) called in full generation class based mode: "
			   + FileUtil.getSystemLineSeparator()
		       + classBasedReportEntries.toString().trim();
	}

	@Override
	public int getNumberOfCalls()
	{
		return callCountsModelBasedFullGeneration +  callCountsClassBasedFullGeneration;
	}

	@Override
	public String getShortReport()
	{
		return "Velocity engine started in full generation mode for " + getNumberOfCalls() + " call(s).";
	}

	@Override
	public int getSuggestedPositionInExecutionOrder()
	{
		return 200;
	}
}