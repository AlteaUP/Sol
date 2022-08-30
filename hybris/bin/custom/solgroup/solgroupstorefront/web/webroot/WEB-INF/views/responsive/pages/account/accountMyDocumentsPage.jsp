<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/responsive/nav" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="wam" tagdir="/WEB-INF/tags/responsive/wamintegration" %>

<spring:htmlEscape defaultHtmlEscape="true"/>
<c:set var="searchUrl" value="/my-account/my-documents?documentType=${selectedDocumentType}&searchFromDate=${selectedSearchFromDateString}&searchToDate=${selectedSearchToDateString}&sort=${ycommerce:encodeUrl(searchPageData.pagination.sort)}"/>

<c:choose>
    <c:when test="${isOrganizationAgent == true}">

        <wam:myDocumentsHeader selectedDocumentType="${selectedDocumentType}"/>

        <div class="account-section-content col-md-6 col-md-push-3 content-empty">
            <spring:theme code="text.account.documents.notallowed" text="You are not allowed to access to the content of this page." />
        </div>
    </c:when>
    <c:otherwise>
        <!-- dropdown for different documentType -->
        <div class="row">
            <div class="col-xs-8 col-sm-4 col-md-2">
                <select name="documentType" class="form-control" onchange="ACC.mydocuments.reloadPage()">
                    <option value="invoices"
                        <c:if test="${selectedDocumentType eq 'invoices'}">selected="selected"</c:if>>
                        <spring:theme code="text.account.documents.page.type.invoices"/>
                    </option>
                    <option value="businessLetters"
                        <c:if test="${selectedDocumentType eq 'businessLetters'}">selected="selected"</c:if>>
                        <spring:theme code="text.account.documents.page.type.businessLetter"/>
                    </option>
                    <option value="dunningLetters"
                        <c:if test="${selectedDocumentType eq 'dunningLetters'}">selected="selected"</c:if>>
                        <spring:theme code="text.account.documents.page.type.dunningletter"/>
                    </option>
                    <option value="dn"
                        <c:if test="${selectedDocumentType eq 'dn'}">selected="selected"</c:if>>
                        <spring:theme code="text.account.documents.page.type.dn"/>
                    </option>
                    <option value="contracts"
                        <c:if test="${selectedDocumentType eq 'contracts'}">selected="selected"</c:if>>
                        <spring:theme code="text.account.documents.page.type.contracts"/>
                    </option>
                </select>
            </div>
        </div>

        <input id="defaultDaysRange" name="defaultDaysRange" type="hidden" value="${defaultDaysRange}"/>

        <!-- start date search date picker -->
        <div class="row" style="margin-top:20px;">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="control-label" for="orderDate">
                        <spring:theme code="text.account.documents.filter.startdate"/>
                    </label>
                    <fmt:formatDate value="${selectedSearchFromDate}" type="date" pattern="dd/MM/yyyy" var="formattedOrderDateStart" />
                    <input id="wamStartDate" name="wamStartDate"  class="form-control datepicker js-update-entry-orderDate-input" type="text" value="${formattedOrderDateStart}"/>
                </div>
            </div>
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="control-label" for="orderDate">
                        <spring:theme code="text.account.documents.filter.enddate"/>
                    </label>
                    <fmt:formatDate value="${selectedSearchToDate}" type="date" pattern="dd/MM/yyyy" var="formattedOrderDateEnd" />
                    <input id="wamEndDate" name="wamEndDate"  class="form-control datepicker js-update-entry-orderDate-input" type="text" value="${formattedOrderDateEnd}"/>
                </div>
            </div>
        </div>
        <div class="row" style="margin-top:20px;">

            <div class="col-sm-2">
                <small>
                    <strong>
                        <spring:theme code="text.account.documents.button.infomessage.prefix"/>
                        &nbsp;${defaultDaysRange}&nbsp;
                        <spring:theme code="text.account.documents.button.infomessage.suffix"/>
                    </strong>
                </small>
                <button id="searchWamDocumentButtonId" type="button" class="btn btn-primary btn-block" onClick="ACC.mydocuments.searchWamDocuments()">
                    <spring:theme code="text.account.documents.button.search"/>
                </button>
            </div>
        </div>

        <wam:myDocumentsHeader selectedDocumentType="${selectedDocumentType}"/>

        <!-- no document found for current selected type -->
        <c:if test="${empty searchPageData.results}">
            <div class="row">
                <div class="col-md-6 col-md-push-3">
                    <div class="account-section-content	content-empty">
                        <c:choose>
                            <c:when test="${selectedDocumentType eq 'invoices'}">
                                <ycommerce:testId code="invoices_noInvoices_label">
                                    <spring:theme code="text.account.documents.noinvoices"/>
                                </ycommerce:testId>
                            </c:when>
                            <c:when test="${selectedDocumentType eq 'businessLetters'}">
                                <ycommerce:testId code="invoices_noInvoices_label">
                                    <spring:theme code="text.account.documents.nobusinessletter"/>
                                </ycommerce:testId>
                            </c:when>
                            <c:when test="${selectedDocumentType eq 'dunningLetters'}">
                              <ycommerce:testId code="invoices_noInvoices_label">
                                  <spring:theme code="text.account.documents.nodunningletter"/>
                              </ycommerce:testId>
                            </c:when>
                            <c:when test="${selectedDocumentType eq 'dn'}">
                                <ycommerce:testId code="invoices_noInvoices_label">
                                    <spring:theme code="text.account.documents.nodn"/>
                                </ycommerce:testId>
                            </c:when>
                            <c:when test="${selectedDocumentType eq 'contracts'}">
                                <ycommerce:testId code="invoices_noInvoices_label">
                                    <spring:theme code="text.account.documents.nocontracts"/>
                                </ycommerce:testId>
                            </c:when>
                            <c:otherwise>
                                <ycommerce:testId code="invoices_noInvoices_label">
                                    <spring:theme code="text.account.documents.noinvoices"/>
                                </ycommerce:testId>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- document list found for current selected type -->
        <c:if test="${not empty searchPageData.results}">
            <div class="account-section-content	">
                <div class="account-orderhistory">

                    <div class="account-orderhistory-pagination">
                        <nav:pagination top="true" msgKey="text.account.documents.page" showCurrentPageInfo="true"
                                        hideRefineButton="true" supportShowPaged="${isShowPageAllowed}"
                                        supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}"
                                        searchUrl="${searchUrl}" numberPagesShown="${numberPagesShown}"/>
                    </div>

                    <div class="account-overview-table">
                        <table class="orderhistory-list-table responsive-table">
                            <thead>
                                <tr class="account-orderhistory-table-head responsive-table-head hidden-xs">
                                    <th><spring:theme code='text.account.documents.code'/></th>
                                    <th><spring:theme code='text.account.documents.date'/></th>
                                    <th><spring:theme code='text.account.documents.updateddate'/></th>
                                    <th><spring:theme code='text.account.documents.download'/></th>
                                </tr>
                            </thead>
                            <tbody>
                                <input name="documentTypeHidden" type="hidden" value="${selectedDocumentType}"/>
                                <c:forEach items="${searchPageData.results}" var="document">
                                    <tr class="responsive-table-item">
                                        <ycommerce:testId code="orderHistoryItem_orderDetails_link">
                                            <td class="hidden-sm hidden-md hidden-lg">
                                                <spring:theme code="text.account.documents.code"/>
                                            </td>
                                            <td class="responsive-table-cell">
                                                <input name="documentId" type="hidden" value="${document.id}"/>
                                                <input name="documentKey" type="hidden" value="${document.document}"/>
                                                ${document.documentNumber}
                                            </td>
                                            <td class="hidden-sm hidden-md hidden-lg">
                                                <spring:theme code="text.account.documents.date"/>
                                            </td>
                                            <td class="responsive-table-cell">
                                                <fmt:formatDate value="${document.date}" dateStyle="medium" timeStyle="short" type="date"/>
                                            </td>
                                            <td class="hidden-sm hidden-md hidden-lg">
                                                <spring:theme code="text.account.documents.updateddate"/>
                                            </td>
                                            <td class="responsive-table-cell">
                                                <c:if test="${not empty document.updatedDate}">
                                                    <fmt:formatDate value="${document.updatedDate}" dateStyle="medium" timeStyle="short" type="both"/>
                                                </c:if>
                                                <c:if test="${empty document.updatedDate}">
                                                    -
                                                </c:if>
                                            </td>
                                            <td class="responsive-table-cell remove-item-column">
                                                <c:if test="${not empty document.document}">
                                                    <a href="javascript:ACC.mydocuments.downloadDocument('${document.id}', '${document.document}', '${selectedDocumentType}');">
                                                        <span class="glyphicon glyphicon-download-alt"></span>
                                                    </a>
                                                </c:if>
                                                <c:if test="${empty document.document}">
                                                    <span class="glyphicon glyphicon-download-alt"></span>
                                                </c:if>
                                            </td>
                                        </ycommerce:testId>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="account-orderhistory-pagination">
                    <nav:pagination top="false" msgKey="text.account.documents.page" showCurrentPageInfo="true"
                                    hideRefineButton="true" supportShowPaged="${isShowPageAllowed}"
                                    supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}"
                                    searchUrl="${searchUrl}" numberPagesShown="${numberPagesShown}"/>
                </div>

            </div>
        </c:if>
    </c:otherwise>
</c:choose>
