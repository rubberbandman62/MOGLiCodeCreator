package com.iksgmbh.moglicc.provider.model.standard.impl;

import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.moglicc.provider.model.standard.metainfo.MetaInfo;
import com.iksgmbh.moglicc.provider.model.standard.metainfo.MetaInfoSupport;

public abstract class MetaModelObject implements MetaInfoSupport {
	
	protected List<MetaInfo> metaInfoList;
	
	public void addMetaInfo(final MetaInfo metaInfo) {
		this.metaInfoList.add(metaInfo);
	}	
	
	@Override
	public String getMetaInfoValueFor(final String metaInfoName) {
		for (final MetaInfo element : metaInfoList) {
			if (element.getName().equals(metaInfoName)) {
				return element.getValue();
			}
		}
		return META_INFO_NOT_FOUND.replaceFirst("#", metaInfoName);
	}
	
	@Override
	public List<String> getAllMetaInfoValuesFor(final String metaInfoName) {
		final List<String> toReturn = new ArrayList<String>();
		for (final MetaInfo element : metaInfoList) {
			if (element.getName().equals(metaInfoName)) {
				toReturn.add(element.getValue());
			}
		}
		return toReturn;
	}
	
	@Override
	public List<MetaInfo> getMetaInfoList() {
		return metaInfoList;
	}
	

	@Override
	public boolean doesHaveMetaInfo(final String metaInfoName, final String value) {
		for (final MetaInfo element : metaInfoList) {
			if (element.getName().equals(metaInfoName)
				&& element.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean doesHaveAnyMetaInfosWithName(final String metaInfoName) {
		for (final MetaInfo element : metaInfoList) {
			if (element.getName().equals(metaInfoName)) {
				return true;
			}
		}
		return false;
	}

	protected String getCommaSeparatedListOfMetaInfoNames() {
		final StringBuffer sb = new StringBuffer();
		for (final MetaInfo element : metaInfoList) {
			sb.append(element.getName());
			sb.append(", ");
		}
		final String s = sb.toString();
		if (s.length() < 3) {
			return "";
		}
		return s.substring(0, s.length()-2);
	}

}
