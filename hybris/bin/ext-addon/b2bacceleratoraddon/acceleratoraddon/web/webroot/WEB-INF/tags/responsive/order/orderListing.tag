<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="searchUrl" required="true" type="String" %>
<%@ attribute name="messageKey" required="true" type="String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/responsive/nav" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement"%>

<spring:htmlEscape defaultHtmlEscape="true"/>
<spring:url value="/my-account/order/" var="orderDetailsUrl" htmlEscape="false"/>
<spring:url value="/my-account/orders" var="bindUrl" htmlEscape="false"/>

<div class="account-section-content">    
    <div class="pagination-bar top">
        <form:form id="orderHistorySearchForm" action="${bindUrl}" method="get" modelAttribute="solGroupOrderHistoryFiltersForm" >
            <div class="row" >
                <div class="col-sm-3 binding-btn">
                    <div class="form-group">
                    <label class="control-label" for="orderCode">
                        <spring:theme code="text.account.orderHistory.filter.orderNumber" text="Order Code"/>
                    </label>
                    <input id="orderCode" name="orderCode" class="text form-control" type="text" value="${fn:escapeXml(solGroupOrderHistoryFiltersForm.orderCode)}" />
                    </div>
                </div>  										
				<div class="col-sm-3 binding-btn">
                	<div class="form-group">
                	 	<label class="control-label" for="orderDate">
                       		<spring:theme code="text.account.orderHistory.filter.datePlaced" text="Date Placed"/>
                   		</label>
                   		<fmt:formatDate value="${solGroupOrderHistoryFiltersForm.orderDate}" type="date" pattern="dd/MM/yyyy" var="formattedOrderDate" />
                		<input id="orderDate" name="orderDate" class="form-control datepicker js-update-entry-orderDate-input" type="text" value="${formattedOrderDate}" />
                	</div>
                </div>	
                <div class="col-sm-3 binding-btn">
                    <div class="control-group">
                    <label class="control-label" for="orderStatus">
                        <spring:theme code="text.account.orderHistory.filter.orderStatus" text="Order Status"/>
                    </label>
                    <select id="orderStatus" name="orderStatus" class="text form-control">
                    	<option value="" >
                            <spring:theme code="text.account.orderHistory.filter.pleaseSelect" text=""></spring:theme>
                        </option>
                        <c:forEach items="${orderStatusList}" var="associatedItem">
        					<option value="${associatedItem}" ${associatedItem == solGroupOrderHistoryFiltersForm.orderStatus ? 'selected' : ''}>
        						<spring:theme code="text.account.order.status.display.${associatedItem}"/>
        					</option>
    					</c:forEach>
                    </select>
                    </div>
                </div>
        	</div> 
        	<div class="row">
	        	<div class="col-sm-6 binding-btn"></div>
        		<div class="col-sm-3 binding-btn">
	                <div class="accountActions">
	                    <button id="reset" class="btn btn-default btn-block">
	                    	<spring:theme code="text.account.orderHistory.filter.resetButton" text="Reset" />
	                    </button>
	                 </div>
	            </div>
	            <div class="col-sm-3 binding-btn">
	                <div class="accountActions">
	                    <button id="submit" type="submit" class="btn btn-primary btn-block">
	                        <spring:theme code="text.account.orderHistory.filter.submitButton" text="Submit" />
	                    </button>
	                </div>
	            </div>
        	</div>          
        </form:form>
    </div>

	<c:if test="${empty searchPageData.results}">
	    <div class="row">
	        <div class="col-md-6 col-md-push-3">
	            <div class="account-section-content content-empty">
	                <ycommerce:testId code="orderHistory_noOrders_label">
	                    <spring:theme code="text.account.orderHistory.noOrders"/>
	                </ycommerce:testId>
	            </div>
	        </div>
	    </div>
	</c:if>
	
	<c:if test="${not empty searchPageData.results}">
        <div class="account-orderhistory">
            <div class="account-orderhistory-pagination">
                <nav:pagination top="true" msgKey="${messageKey}" showCurrentPageInfo="true" hideRefineButton="true"
                                supportShowPaged="${isShowPageAllowed}" supportShowAll="${isShowAllAllowed}"
                                searchPageData="${searchPageData}" searchUrl="${searchUrl}"
                                numberPagesShown="${numberPagesShown}"/>
            </div>
            <div class="responsive-table">
                <table class="responsive-table">
                    <thead>
                    <tr class="responsive-table-head hidden-xs">
                        <th id="header1"><spring:theme code="text.account.orderHistory.orderNumber"/></th>
                        <%-- <th id="header2"><spring:theme code="text.account.orderHistory.purchaseOrderNumber"/></th> --%>
                        <th id="header3"><spring:theme code="text.account.orderHistory.orderStatus"/></th>
                        <th id="header4"><spring:theme code="text.account.orderHistory.datePlaced"/></th>
                        <th id="header5"><spring:theme code="text.account.orderHistory.total"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${searchPageData.results}" var="order">
                        <tr class="responsive-table-item">
                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.orderHistory.orderNumber"/></td>
                            <td headers="header1" class="responsive-table-cell">
                                <ycommerce:testId code="orderHistoryItem_orderDetails_link">
                                    <a href="${orderDetailsUrl}${ycommerce:encodeUrl(order.code)}" class="responsive-table-link">
                                        ${fn:escapeXml(order.code)}
                                    </a>
                                </ycommerce:testId>
                            </td>
                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.orderHistory.purchaseOrderNumber"/></td>
                            <%-- <td headers="header2" class="responsive-table-cell">
                                    ${fn:escapeXml(order.purchaseOrderNumber)}
                            </td> --%>
                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.orderHistory.orderStatus"/></td>
                            <td headers="header3" class="responsive-table-cell">
                                <spring:theme code="text.account.order.status.display.${order.status}"/>
                            </td>
                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme
                                    code="text.account.orderHistory.datePlaced"/></td>
                            <td headers="header4" class="responsive-table-cell">
                                <fmt:formatDate value="${order.placed}" dateStyle="medium" timeStyle="short" type="both" pattern="dd/MM/yyyy"/>
                            </td>
                            <td class="hidden-sm hidden-md hidden-lg"><spring:theme code="text.account.orderHistory.total"/></td>
                            <td headers="header5" class="responsive-table-cell responsive-table-cell-bold">
                                <div>
                                    <c:if test="${!order.refill}">
                                        ${fn:escapeXml(order.total.formattedValue)}
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="account-orderhistory-pagination">
            <nav:pagination top="false" msgKey="${messageKey}" showCurrentPageInfo="true" hideRefineButton="true"
                            supportShowPaged="${isShowPageAllowed}" supportShowAll="${isShowAllAllowed}"
                            searchPageData="${searchPageData}" searchUrl="${searchUrl}"
                            numberPagesShown="${numberPagesShown}"/>
        </div>
    </c:if>
</div>