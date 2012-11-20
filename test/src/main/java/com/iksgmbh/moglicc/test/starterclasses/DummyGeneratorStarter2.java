package com.iksgmbh.moglicc.test.starterclasses;

import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.moglicc.core.InfrastructureService;
import com.iksgmbh.moglicc.exceptions.MogliPluginException;
import com.iksgmbh.moglicc.plugin.MogliPlugin;
import com.iksgmbh.moglicc.plugin.type.basic.Generator;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.MetaInfoValidator;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.MetaInfoValidatorVendor;

public class DummyGeneratorStarter2 implements Generator, MetaInfoValidatorVendor {

	public static final String PLUGIN_ID = "DummyGenerator2";
	private InfrastructureService infrastructure;
	private List<MetaInfoValidator> metaInfoValidatorList;

	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public PluginType getPluginType() {
		return MogliPlugin.PluginType.GENERATOR;
	}

	@Override
	public void doYourJob() {
		infrastructure.getPluginLogger().logInfo(PLUGIN_ID);
	}

	@Override
	public void setMogliInfrastructure(InfrastructureService infrastructure) {
		this.infrastructure = infrastructure;
	}

	@Override
	public List<String> getDependencies() {
		List<String> toReturn = new ArrayList<String>();
		toReturn.add("DummyDataProvider");
		toReturn.add("StandardModelProvider");		
		toReturn.add("VelocityEngineProvider");
		return toReturn;
	}

	@Override
	public boolean unpackDefaultInputData() throws MogliPluginException {
		return false;
	}

	@Override
	public List<MetaInfoValidator> getMetaInfoValidatorList() throws MogliPluginException {
		return metaInfoValidatorList;
	}
	
	public void setMetaInfoValidatorList(final List<MetaInfoValidator> metaInfoValidatorList)  {
		this.metaInfoValidatorList = metaInfoValidatorList;
	}

	@Override
	public InfrastructureService getMogliInfrastructure() {
		return infrastructure;
	}	
	
	@Override
	public boolean unpackPluginHelpFiles() throws MogliPluginException {
		return false;
	}

}
