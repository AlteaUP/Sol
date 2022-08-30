package com.solgroup.service.impexImport;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.util.Strings;

public class Impex extends HashSet<ImpexRow> {

	private static final String EOL = "\n";
	private ImpexHeader header;

	public String toImpex() {

		String impex = header.toHeader().concat(EOL);

		Iterator<ImpexRow> iterator = this.iterator();
		do {
			impex = impex.concat(iterator.next().toRow(header).concat(EOL));

		} while (iterator.hasNext());
		return impex;

	}
	
	public ImpexHeader getHeader() {
		return header;
	}

	public void setHeader(ImpexHeader header) {
		this.header = header;
	}
}
