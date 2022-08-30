package com.solgroup.service.impexImport;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.util.Strings;

public class ImpexRow extends HashMap<ImpexAttribute, String> {

	private static final String IGNORE_TOKEN = "<ignore>";
	private static final String DELIMETER = ";";

	public String toRow(ImpexHeader header) {
		String row = DELIMETER;
		Iterator<ImpexAttribute> iterator = header.iterator();
		do {
			String i = this.get(iterator.next());

			if (Strings.isNotBlank(i)) {
				row = row.concat(i).concat(DELIMETER);
			} else {
				row = row.concat(IGNORE_TOKEN + DELIMETER);
			}

		} while (iterator.hasNext());
		return row;
	}

}
