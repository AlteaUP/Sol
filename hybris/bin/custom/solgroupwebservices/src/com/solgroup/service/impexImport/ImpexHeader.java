package com.solgroup.service.impexImport;

import java.util.HashSet;
import java.util.Set;

public class ImpexHeader extends HashSet<ImpexAttribute> implements Set<ImpexAttribute> {

	private static final String BLANK_CHARACTER = " ";

	public enum Action {
		INSERT, INSERT_UPDATE, UPDATE, DELETE
	};

	private static final String DELIMETER = ";";
	private String item;
	private Action action;

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public ImpexHeader(String item, Action action) {
		super();
		this.item = item;
		this.action = action;
	}

	public String toHeader() {

		String attributes = new String();
		for (ImpexAttribute i : this)
			attributes = attributes.concat(i.toHeader().concat(DELIMETER));
		return action.name() + BLANK_CHARACTER + item + DELIMETER + attributes;
	}

}
