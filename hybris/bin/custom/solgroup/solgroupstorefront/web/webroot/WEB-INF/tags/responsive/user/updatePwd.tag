<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>

<spring:htmlEscape defaultHtmlEscape="true"/>
        <div class="account-section">
            <div class="account-section-header no-border"><spring:theme code="text.account.profile.resetPassword"/></div>
            <div class="account-section-content row">
                <form:form method="post" commandName="updatePwdForm">
                    <div class="col-md-6">
                        <div class="form-group">
                            <formElement:formPasswordBox idKey="pwd" labelKey="updatePwd.pwd" path="pwd"
                                                         inputCSS="form-control password-strength" mandatory="true"/>
                        </div>
                        <div class="form-group">
                            <formElement:formPasswordBox idKey="checkPwd" labelKey="updatePwd.checkPwd"
                                                         path="checkPwd" inputCSS="form-control" mandatory="true"/>
                        </div>
                        <div class="row login-form-action">
                            <div class="col-sm-6">
                                <button type="submit" class="btn btn-primary btn-block">
                                    <spring:theme code="updatePwd.submit"/>
                                </button>
                            </div>
                            <div class="col-sm-6">
                                <button type="button" class="btn btn-default btn-block backToHome">
                                    <spring:theme code="text.button.cancel"/>
                                </button>
                            </div>
                        </div>
                    </div>
                </form:form>
            </div>
        </div>

