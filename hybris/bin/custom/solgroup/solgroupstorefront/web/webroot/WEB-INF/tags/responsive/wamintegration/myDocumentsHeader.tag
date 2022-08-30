<%@ attribute name="selectedDocumentType" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="documentTypeVar" value="${selectedDocumentType}"/>

<!-- labels table header for different documentType -->
<c:choose>
    <c:when test="${documentTypeVar eq 'invoices'}">
        <div class="account-section-header">
            <spring:theme code="text.account.documents.myinvoices"/>
        </div>
    </c:when>
    <c:when test="${documentTypeVar eq 'businessLetters'}">
        <div class="account-section-header">
            <spring:theme code="text.account.documents.mybusinessletter"/>
        </div>
    </c:when>
    <c:when test="${documentTypeVar eq 'dunningLetters'}">
        <div class="account-section-header">
            <spring:theme code="text.account.documents.mydunningletter"/>
        </div>
    </c:when>
    <c:when test="${documentTypeVar eq 'dn'}">
        <div class="account-section-header">
            <spring:theme code="text.account.documents.mydn"/>
        </div>
    </c:when>
    <c:when test="${documentTypeVar eq 'contracts'}">
        <div class="account-section-header">
            <spring:theme code="text.account.documents.mycontracts"/>
        </div>
    </c:when>
    <c:otherwise>
        <div class="account-section-header">
            <spring:theme code="text.account.documents.myinvoices"/>
        </div>
    </c:otherwise>
</c:choose>