package com.solgroup.acceleratorfacades.csv.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;

import de.hybris.platform.acceleratorfacades.csv.CsvFacade;
import de.hybris.platform.acceleratorfacades.csv.impl.DefaultCsvFacade;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;

public class DefaultSolgroupCsvFacade extends DefaultCsvFacade implements CsvFacade {
	
	@Override
	protected void writeOrderEntry(Writer writer, OrderEntryData entry) throws IOException {
		final StringBuilder csvContent = new StringBuilder();
		csvContent.append(StringEscapeUtils.escapeCsv(entry.getProduct().getErpCode())).append(DELIMITER)
				.append(StringEscapeUtils.escapeCsv(entry.getQuantity().toString())).append(DELIMITER)
				.append(StringEscapeUtils.escapeCsv(entry.getProduct().getName())).append(DELIMITER)
				.append(StringEscapeUtils.escapeCsv(entry.getBasePrice().getFormattedValue())).append(LINE_SEPERATOR);

		writer.write(csvContent.toString());
	}

    public void generateImportSavedCartTemplate(final List<String> headers, final Writer writer) throws IOException {
        if (CollectionUtils.isNotEmpty(headers))
        {
            final StringBuilder csvHeader = new StringBuilder();
            int i = 0;
            for (; i < headers.size() - 1; i++)
            {
                csvHeader.append(StringEscapeUtils.escapeCsv(headers.get(i))).append(DELIMITER);
            }
            csvHeader.append(StringEscapeUtils.escapeCsv(headers.get(i))).append(LINE_SEPERATOR);
            writer.write(csvHeader.toString());
        }

    }


}
