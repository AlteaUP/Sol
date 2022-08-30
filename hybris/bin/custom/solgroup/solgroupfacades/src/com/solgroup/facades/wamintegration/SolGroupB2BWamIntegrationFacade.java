package com.solgroup.facades.wamintegration;

import com.solgroup.core.service.wamintegration.json.response.WamDownloadDocumentResponse;
import de.hybris.platform.commercefacades.wamdocument.data.WamDocumentData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;

public interface SolGroupB2BWamIntegrationFacade {

    public SearchPageData<WamDocumentData> getPagedDocumentsList(PageableData pageableData, HttpSession session, String documentsType, LocalDate fromDate, LocalDate startDate);

    public WamDownloadDocumentResponse downloadPdfDocument(HttpSession session, String documentId, String documentKey, String documentsType);

}
