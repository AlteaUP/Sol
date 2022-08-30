package com.solgroup.service.impexImport;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;

public class ImpexAttribute extends ArrayList<ImpexAttributeModifier> implements List<ImpexAttributeModifier> {

	private String attributeName;

	public ImpexAttribute(String attributeName) {
		super();
		this.attributeName = attributeName;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String toHeader()
	{
		
		String modifiers = "";
		for(ImpexAttributeModifier i : this)
			modifiers = modifiers.concat(i.toHeader());
		return attributeName + modifiers;
	}


	
}
