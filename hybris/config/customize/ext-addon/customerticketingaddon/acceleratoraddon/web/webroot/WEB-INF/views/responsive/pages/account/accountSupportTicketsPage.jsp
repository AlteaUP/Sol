<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/responsive/nav" %>
<spring:htmlEscape defaultHtmlEscape="true" />
<c:set var="action">${param.action}</c:set>
<c:choose>
	<c:when test="${empty action}">
		<div class="account-section-header ${empty supportTickets ? '':'no-border'}">
			<spring:theme code="text.account.supporttickets" text="Support Tickets" />
		    <div class="account-section-header-add pull-right">
		        <a href="add-support-ticket" class="add-address" id="add-support-ticket-btn">
		            <spring:theme code="text.account.supporttickets.requestSupport" text="Request Support" />
		        </a>
		    </div>
		</div>
	</c:when>
	<c:when test="${'quote' eq action}">
	    <div class="account-section-header ${empty supportTickets ? '':'no-border'}">
			<spring:theme code="text.account.productquote.createTicket.title" text="Product Quote" />
		</div>
	</c:when>
</c:choose>

<c:set var="searchUrl" value="/my-account/support-tickets?sort=${searchPageData.pagination.sort}&action=${action}"/>
<c:choose>
	<c:when test="${empty action}">
		<c:if test="${empty searchPageData.results}">
			<div class="account-section-content col-md-6 col-md-push-3 content-empty">
		    	<spring:theme code="text.account.supporttickets.noSupporttickets" text="You have no support tickets" />
			</div>
		</c:if>
	</c:when>
	<c:when test="${'quote' eq action}">
	    <c:choose>
            <c:when test="${isOrganizationAgent == true}">
                <div class="account-section-content col-md-6 col-md-push-3 content-empty">
                    <spring:theme code="text.account.productquote.page.notallowed" text="You are not allowed to access to the content of this page." />
                </div>
            </c:when>
            <c:otherwise>
                <c:if test="${empty searchPageData.results}">
                    <div class="account-section-content col-md-6 col-md-push-3 content-empty">
                        <spring:theme code="text.account.productquote.noQuotation" text="You have no quotation" />
                    </div>
                </c:if>
		    </c:otherwise>
        </c:choose>
	</c:when>
</c:choose>
<div class="clearfix visible-md-block visible-lg-block"></div>

<c:if test="${isOrganizationAgent != true}">
    <div class="customer-ticketing account-overview-table">
        <c:if test="${not empty searchPageData.results}">
            <c:choose>
                <c:when test="${empty action}">
                    <nav:pagination top="true" msgKey="text.account.supportTickets.page" showCurrentPageInfo="true" hideRefineButton="true" supportShowPaged="${isShowPageAllowed}" supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}" searchUrl="${searchUrl}"  numberPagesShown="${numberPagesShown}"/>
                </c:when>
                <c:when test="${'quote' eq action}">
                    <nav:pagination top="true" msgKey="text.account.productquote.page" showCurrentPageInfo="true" hideRefineButton="true" supportShowPaged="${isShowPageAllowed}" supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}" searchUrl="${searchUrl}"  numberPagesShown="${numberPagesShown}"/>
                </c:when>
            </c:choose>
            <table class="responsive-table">
                <thead>
                    <tr class="responsive-table-head hidden-xs">
                        <c:choose>
                            <c:when test="${'quote' eq action}">
                                <th><spring:theme code="text.account.productquote.id" text="Quotation ID" /></th>
                                <th><spring:theme code="text.account.productquote.productName.productCode" text="Product Name | Product Code" /></th>
                            </c:when>
                            <c:when test="${empty action}">
                                <th><spring:theme code="text.account.supporttickets.ticketId" text="Ticket ID" /></th>
                                <th><spring:theme code="text.account.supporttickets.subject" text="Subject" /></th>
                            </c:when>
                        </c:choose>
                        <th><spring:theme code="text.account.supporttickets.dateCreated" text="Date Created" /></th>
                        <th><spring:theme code="text.account.supporttickets.dateUpdated" text="Date Updated" /></th>
                        <th class="supportTicketsTableState"><spring:theme code="text.account.supporttickets.status" text="Status" /></th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach items="${searchPageData.results}" var="request">
                        <c:choose>
                            <c:when test="${empty action}">
                                <c:url value="/my-account/support-ticket/${request.id}" var="url" />
                            </c:when>
                            <c:when test="${'quote' eq action}">
                                <c:url value="/my-account/support-ticket/${request.id}?action=quote" var="url" />
                            </c:when>
                        </c:choose>
                        <tr class="responsive-table-item">
                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.supporttickets.ticketId" text="Ticket ID" /></td>
                            <td><a href="${url}" class="responsive-table-link"><c:out value="${request.id}" /></a></td>

                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.supporttickets.subject" text="Subject" /></td>
                            <td class="break-word"><a href="${url}" class="responsive-table-link"><c:out value="${request.subject}" /></a></td>

                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.supporttickets.dateCreated" text="Date Created" /></td>
                            <td><fmt:formatDate value="${request.creationDate}" pattern="dd-MM-yy hh:mm a" /></td>

                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.supporttickets.dateUpdated" text="Date Updated" /></td>
                            <td><fmt:formatDate value="${request.lastModificationDate}" pattern="dd-MM-yy hh:mm a" /></td>

                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.supporttickets.status" text="Status" /></td>
                            <td><spring:message code="ticketstatus.${fn:toUpperCase(request.status.id)}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <c:choose>
                <c:when test="${empty action}">
                    <nav:pagination top="false" msgKey="text.account.supportTickets.page" showCurrentPageInfo="true" hideRefineButton="true" supportShowPaged="${isShowPageAllowed}" supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}" searchUrl="${searchUrl}"  numberPagesShown="${numberPagesShown}"/>
                </c:when>
                <c:when test="${'quote' eq action}">
                    <nav:pagination top="false" msgKey="text.account.productquote.page" showCurrentPageInfo="true" hideRefineButton="true" supportShowPaged="${isShowPageAllowed}" supportShowAll="${isShowAllAllowed}" searchPageData="${searchPageData}" searchUrl="${searchUrl}"  numberPagesShown="${numberPagesShown}"/>
                </c:when>
            </c:choose>
        </c:if>
    </div>
</c:if>
