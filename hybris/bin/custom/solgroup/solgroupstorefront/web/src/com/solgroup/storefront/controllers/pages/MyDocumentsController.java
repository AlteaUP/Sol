package com.solgroup.storefront.controllers.pages;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.wamintegration.json.response.WamDownloadDocumentResponse;
import com.solgroup.facades.wamintegration.SolGroupB2BWamIntegrationFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractSearchPageController;
import de.hybris.platform.assistedserviceservices.utils.AssistedServiceSession;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.wamdocument.data.WamDocumentData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/my-account/my-documents")
public class MyDocumentsController  extends AbstractSearchPageController {

    private static final String MY_DOCUMENTS_CMS_PAGE = "my-documents";
    private static final Logger LOG = Logger.getLogger(MyDocumentsController.class);

    @Resource(name = "accountBreadcrumbBuilder")
    private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

    @Resource(name = "solGroupB2BWamIntegrationFacade")
    private SolGroupB2BWamIntegrationFacade solGroupB2BWamIntegrationFacade;

    @RequestMapping(method = RequestMethod.GET)
    @RequireHardLogIn
    public String loadMyDocumentsPage(
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "show", defaultValue = "Page") final ShowMode showMode,
            @RequestParam(value = "sort", defaultValue = "byDate") final String sortCode,
            @RequestParam(value = "documentType", defaultValue = "invoices") final String documentType,
            @RequestParam(value = "searchFromDate", defaultValue = "") String searchFromDate,
            @RequestParam(value = "searchToDate", defaultValue = "") String searchToDate,
            final Model model, final HttpServletRequest request, final HttpServletResponse response)
            throws CMSItemNotFoundException {

        // If in ASM session with organization-type agent you are not allowed to view documents
        Object asmAttribute = getSessionService().getCurrentSession().getAttribute("ASM");
        boolean isOrganizationAgent = false;

        final boolean asmViewEnabled = Boolean.parseBoolean(Config.getParameter("wam.integration.api.getdocumentslist.asm.viewenabled"));

        if(asmViewEnabled && asmAttribute != null){
            AssistedServiceSession asmSession = (AssistedServiceSession) asmAttribute;
            UserModel agent = asmSession.getAgent();
            if(agent != null){
                for(PrincipalGroupModel userGroupModel : agent.getGroups()){
                    if(userGroupModel.getUid().equals("solOrganizationGroup")){
                        isOrganizationAgent = true;
                        break;
                    }
                }
            }
        }
        model.addAttribute("isOrganizationAgent", isOrganizationAgent);

        if(!isOrganizationAgent) {

            final String pageSize = Config.getParameter("wam.integration.api.getdocumentslist.pageSize");
            String defaultDaysRange = Config.getParameter("wam.integration.api.getdocumentslist.defaultdaysrange");

            LocalDate startDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            if (StringUtils.isEmpty(searchFromDate)) {
                //Initialize startDate to X days from now
                startDate = LocalDate.now();
                startDate = startDate.minusDays(Long.parseLong(defaultDaysRange));
                searchFromDate = startDate.format(formatter);
            } else {
                startDate = LocalDate.parse(searchFromDate, formatter);
            }

            LocalDate endDate = null;
            if (StringUtils.isEmpty(searchToDate)) {
                //Initialize endDate to now
                endDate = LocalDate.now();

                searchToDate = endDate.format(formatter);
            } else {
                endDate = LocalDate.parse(searchToDate, formatter);
            }

            final PageableData pageableData = createPageableData(page, Integer.parseInt(pageSize), sortCode, showMode);
            SearchPageData<WamDocumentData> pagedData = solGroupB2BWamIntegrationFacade.getPagedDocumentsList(
                    pageableData, request.getSession(), SolgroupCoreConstants.WamConstants.WamDocumentsTypeEnum.getWamCodeFromFeCode(documentType), startDate, endDate);
            populateModel(model, pagedData, showMode);

            ZoneId defaultZoneId = ZoneId.systemDefault();
            //This attribute is used to value date picker correctly
            model.addAttribute("selectedSearchFromDate", Date.from(startDate.atStartOfDay(defaultZoneId).toInstant()));
            model.addAttribute("selectedSearchToDate", Date.from(endDate.atStartOfDay(defaultZoneId).toInstant()));
            //This attribute is used in query string for subsequent request
            model.addAttribute("selectedSearchFromDateString", searchFromDate);
            model.addAttribute("selectedSearchToDateString", searchToDate);
            //Used as a parameter for time range selection on FE
            model.addAttribute("defaultDaysRange", defaultDaysRange);

        }

        model.addAttribute("selectedDocumentType", documentType);

        final List<Breadcrumb> breadcrumbs = accountBreadcrumbBuilder.getBreadcrumbs(null);
        breadcrumbs.add(new Breadcrumb("/my-account/my-documents", getMessageSource().getMessage(
                SolgroupCoreConstants.WamConstants.WamDocumentsTypeEnum.getBreadcrumbCodeFromFeCode(documentType),
                null, getI18nService().getCurrentLocale()), null));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY, breadcrumbs);
        storeCmsPageInModel(model, getContentPageForLabelOrId(MY_DOCUMENTS_CMS_PAGE));
        setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MY_DOCUMENTS_CMS_PAGE));

        return getViewForPage(model);
    }

    @RequestMapping(value="/downloadWamDocument", method = RequestMethod.GET)
    public void downloadPDFResource(
            @RequestParam("documentId") String documentId,
            @RequestParam("documentKey") String documentKey,
            @RequestParam("documentType") String documentType,
            final Model model, final HttpServletRequest request, final HttpServletResponse response){

        model.addAttribute("selectedDocumentType", documentType);

        WamDownloadDocumentResponse wamResponse = solGroupB2BWamIntegrationFacade.downloadPdfDocument(
                request.getSession(), documentId, documentKey,  SolgroupCoreConstants.WamConstants.WamDocumentsTypeEnum.getWamCodeFromFeCode(documentType));

        if(wamResponse != null && !StringUtils.isEmpty(wamResponse.getFileBase64())) {
            response.setContentType("application/pdf");
            String fileName = documentId + ".pdf";
            if(!StringUtils.isEmpty(wamResponse.getFileName())){
                fileName = wamResponse.getFileName();
            }
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

            //Decode base64 from response
            byte[] decodedPdf = null;
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                decodedPdf = decoder.decodeBuffer(wamResponse.getFileBase64());
                response.getOutputStream().write(decodedPdf);
                response.getOutputStream().flush();
            } catch (IOException e) {
                LOG.error("Error decoding pdf from WAM due to: " + e);
            }
        }else{
            LOG.error("No document found on wam for documentId: " + documentId + " and documentKey: " + documentKey);
        }
    }

}
