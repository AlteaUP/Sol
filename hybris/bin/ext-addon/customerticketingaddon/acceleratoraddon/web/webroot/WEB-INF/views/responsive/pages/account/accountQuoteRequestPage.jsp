<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/responsive/common" %>
<spring:htmlEscape defaultHtmlEscape="true" />

<c:set var="subjectPlaceholder">
	<spring:theme code="text.account.productquote.createTicket.subject.placeholder" arguments="${param.n},${param.c}"/>
</c:set>
<c:set var="back">/${param.n}/p/${param.i}</c:set>

<div id="global-alerts" class="global-alerts"></div>
<div class="back-link border">
    <div class="row">
        <div class="container-lg col-md-6">
            <a href="${back}">
                <span class="glyphicon glyphicon-chevron-left"></span>
            </a>
            <span class="label"><spring:theme code="text.account.productquote.createTicket.title" text="Product Quote" /></span> 
        </div>
    </div>
</div>

<div class="row">
    <div class="container-lg col-md-6">
        <div class="account-section-content">
            <div class="account-section-form">
                <div id="customer-ticketing-alerts"></div>
                <form:form method="post" commandName="supportTicketForm" enctype="multipart/form-data">
                    <formElement:formInputBox idKey="createTicket-subject" labelKey="text.account.supporttickets.createTicket.subject" path="subject" inputCSS="text" mandatory="true" placeholder="${subjectPlaceholder}" disabled="true"/>
                    <input id="subject" name="subject" value="${subjectPlaceholder}" hidden="true">
                    <div id="Size-supportTicketForm-subject" class="help-block" style="display: none;"></div>
                    <formElement:formTextArea idKey="createTicket-message" labelKey="text.account.supporttickets.createTicket.message" path="message" mandatory="true" areaCSS="form-control" labelCSS="control-label"/>
                    <div id="NotEmpty-supportTicketForm-message" class="help-block" style="display: none;"></div>
                    <div id="Size-supportTicketForm-message" class="help-block" style="display: none;"></div>                       
                    <div hidden="true">
                    	<input type="file" name="files" id="attachmentFiles" multiple size="60" class="file-upload__input js-file-upload__input" data-max-upload-size="${maxUploadSize}" hidden="true"/>
					</div>                 
                    
                    <!-- Default Category -->                        
                    <div class="form-group">
                    	<c:forEach var="category" items="${categories}">
                          	<c:if test="${category eq 'QUOTE'}">
                          		<c:set var="quote">${category}</c:set>
                          	</c:if>
                        </c:forEach>
                        <label class="control-label" for="text.account.productquote.createTicket.defaultCategory">
                        	<spring:theme code="text.account.productquote.createTicket.defaultCategory" arguments="${quote}"/>
                        </label>
                        <input id="ticketCategory" name="ticketCategory" value="${quote}" hidden="true">                            
                    </div>
                  
                    <div id="customer-ticketing-buttons" class="form-actions">
                        <div class="accountActions">
                            <div class="row">
                                <div class="col-sm-6 col-sm-push-6 accountButtons">
                                    <ycommerce:testId code="supportTicket_create_button">
                                        <button class="btn btn-primary btn-block" type="submit" id="addQuote">
                                        <spring:theme code="text.account.supporttickets.createTicket.submit" text="Submit"/>
                                        </button>
                                    </ycommerce:testId>
                                </div>

                                <div class="col-sm-6 col-sm-pull-6 accountButtons">
                                    <a href="${back}" class="btn btn-default btn-block">
                                        <spring:theme code="text.account.supporttickets.createTicket.back" text="Cancel" />
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </form:form>
            </div>
        </div>
    </div>
</div>
<common:globalMessagesTemplates/>