<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="org-common"
	tagdir="/WEB-INF/tags/addons/commerceorgaddon/responsive/common"%>
<%@ taglib prefix="company"
	tagdir="/WEB-INF/tags/addons/commerceorgaddon/responsive/company"%>

<spring:htmlEscape defaultHtmlEscape="true" />

<spring:url value="/my-company/organization-management/manage-units/"
	var="cancelUrl" htmlEscape="false" />

<spring:url
	value="/my-company/organization-management/manage-units/edit"
	var="editUnitUrl" htmlEscape="false">
	<spring:param name="unit" value="${unit.uid}" />
</spring:url>
<spring:url
	value="/my-company/organization-management/manage-units/create"
	var="createUnitUrl" htmlEscape="false">
	<spring:param name="unit" value="${unit.uid}" />
</spring:url>
<spring:url
	value="/my-company/organization-management/manage-units/disable"
	var="disableUnitUrl" htmlEscape="false">
	<spring:param name="unit" value="${unit.uid}" />
</spring:url>
<spring:url
	value="/my-company/organization-management/manage-units/enable"
	var="enableUnitUrl" htmlEscape="false">
	<spring:param name="unit" value="${unit.uid}" />
</spring:url>
<spring:url
	value="/my-company/organization-management/manage-units/add-address"
	var="addUnitAddressUrl" htmlEscape="false">
	<spring:param name="unit" value="${unit.uid}" />
</spring:url>
<spring:url
	value="/my-company/organization-management/manage-units/addcostcenter"
	var="addCostcenterUrl" htmlEscape="false">
	<spring:param name="unit" value="${unit.uid}" />
</spring:url>


<template:page pageTitle="${pageTitle}">

	<div class="account-section">
		<div>
			<org-common:headline url="${cancelUrl}"
				labelKey="text.company.manage.units.unitDetails" />
		</div>

		<div class="account-section-content">
			<div class="well well-lg well-tertiary">
				<div class="row">
					<div class="col-sm-10 col-no-padding">
						<div class="row">
							<div class="col-sm-4">
								<div class="item-group">
									<span class="item-label"> <spring:theme
											code="text.company.unit.erpCode" />
									</span> <span class="item-value"> ${fn:escapeXml(unit.erpCode)}
									</span>
								</div>
								<div class="item-group">
									<span class="item-label"> <spring:theme
											code="text.company.unit.name" />
									</span> <span class="item-value"> ${fn:escapeXml(unit.name)} </span>
								</div>
							</div>
							<c:if test="${not empty unit.unit.uid}">
								<div class="col-sm-4">
									<div class="item-group">
										<span class="item-label"> <spring:theme
												code="text.company.unit.parent" />
										</span> <span class="item-value">
											${fn:escapeXml(unit.unit.erpCode)} -
											${fn:escapeXml(unit.unit.name)}</span>
									</div>
								</div>
							</c:if>
							<c:if test="${not empty unit.vatID}">
								<div class="col-sm-4">
									<div class="item-group">
										<span class="item-label"> <spring:theme
												code="text.company.unit.vatID" />
										</span> <span class="item-value"> ${fn:escapeXml(unit.vatID)}</span>
									</div>
								</div>
							</c:if>
							<c:if test="${not empty unit.taxPayerCode}">
								<div class="col-sm-4">
									<div class="item-group">
										<span class="item-label"> <spring:theme
												code="text.company.unit.taxPayerCode" />
										</span> <span class="item-value">
											${fn:escapeXml(unit.taxPayerCode)}</span>
									</div>
								</div>
							</c:if>
							<%-- 							<c:if test="${not empty unit.approvalProcessName}"> --%>
							<!-- 								<div class="col-sm-4"> -->
							<!-- 									<div class="item-group"> -->
							<%-- 										<span class="item-label"> <spring:theme --%>
							<%-- 												code="text.company.unit.approvalProcess" /> --%>
							<!-- 										</span> <span class="item-value"> -->
							<%-- 											${fn:escapeXml(unit.approvalProcessName)} </span> --%>
							<!-- 									</div> -->
							<!-- 								</div> -->
							<%-- 							</c:if> --%>
						</div>
					</div>
					<div class="col-sm-2">
						<div class="item-action">
							<%--                             <a href="${editUnitUrl}" class="edit btn btn-block btn-primary"> --%>
							<%--                                 <spring:theme code="text.company.manage.units.button.editUnit"/> --%>
							<%--                             </a> --%>
						</div>
					</div>
				</div>
			</div>
			<!-- 			<div class="accountActions-link"> -->
			<%-- 				<c:choose> --%>
			<%-- 					<c:when test="${unit.active}"> --%>
			<%-- 						<c:if test="${unit.uid != user.unit.uid}"> --%>
			<%-- 							<span class="js-action-confirmation-modal disable-link"> <ycommerce:testId --%>
			<%-- 									code="Unit_DisableUnit_button"> --%>
			<!-- 									<a href="#" -->
			<%-- 										data-action-confirmation-modal-title="<spring:theme code="text.company.manage.units.button.disableUnit"/>" --%>
			<%-- 										data-action-confirmation-modal-id="disable"> <spring:theme --%>
			<%-- 											code="text.company.manage.units.button.disableUnit" /> --%>
			<!-- 									</a> -->
			<%-- 								</ycommerce:testId> --%>
			<!-- 							</span> -->
			<%-- 							<company:actionConfirmationModal id="disable" --%>
			<%-- 								targetUrl="${disableUnitUrl}" --%>
			<%-- 								messageKey="text.company.manage.units.disableUnit.confirmation" /> --%>
			<%-- 						</c:if> --%>
			<%-- 					</c:when> --%>
			<%-- 					<c:otherwise> --%>
			<%-- 						<c:choose> --%>
			<%-- 							<c:when test="${unit.unit.active}"> --%>
			<%-- 								<ycommerce:testId code="Unit_EnableUnit_button"> --%>
			<%-- 									<a href="${enableUnitUrl}" class="button enable-link"> <spring:theme --%>
			<%-- 											code="text.company.manage.units.button.enableUnit" /> --%>
			<!-- 									</a> -->
			<%-- 								</ycommerce:testId> --%>
			<%-- 							</c:when> --%>
			<%-- 							<c:otherwise> --%>
			<!-- 								<div class="pull-right"> -->
			<%-- 									<spring:theme --%>
			<%-- 										code="text.company.manage.units.parentUnit.disabled" /> --%>
			<!-- 								</div> -->
			<%-- 							</c:otherwise> --%>
			<%-- 						</c:choose> --%>
			<%-- 					</c:otherwise> --%>
			<%-- 				</c:choose> --%>
			<!-- 			</div> -->

			<div class="account-list">
				<c:set var="count" value="${fn:length(unit.addresses)}" />
				<c:forEach items="${unit.addresses}" var="address">
					<c:if test="${address.shippingAddress != true}">
						<c:set var="count">${count-1}</c:set>
					</c:if>
				</c:forEach>
				<c:if test="${count != 0}">
					<org-common:selectEntityHeadline url="${addUnitAddressUrl}"
						labelKey="text.company.manage.units.addresses" count="${count}"
						addNew="true" />
				</c:if>
				<div class="account-cards">
					<div class="row">
						<c:set var="shippingAddress" value="${unit.shippingAddress}" />
						<c:forEach items="${unit.addresses}" var="address">
							<c:if test="${address.shippingAddress == true}">

								<div class="col-xs-12 col-sm-6 col-md-4 card">
									<c:if test="${shippingAddress.id != address.id}">
										<spring:url
											value="/my-company/organization-management/manage-units/edit-address/"
											var="editUnitAddressUrl" htmlEscape="false">
											<spring:param name="unit" value="${unit.uid}" />
											<spring:param name="addressId" value="${address.id}" />
										</spring:url>
										<spring:url
											value="/my-company/organization-management/manage-units/remove-address/"
											var="removeUnitAddressUrl" htmlEscape="false">
											<spring:param name="unit" value="${unit.uid}" />
											<spring:param name="addressId" value="${address.id}" />
										</spring:url>
									</c:if>
									<ul class="pull-left">
										<li><c:if test="${shippingAddress.id == address.id}">
												<spring:theme
													code='text.company.manage.units.addresses.default' />
											</c:if></li>
									</ul>
									<ul class="pull-left">
										<c:if test="${not empty address.line1}">
											<li><spring:theme
													code='text.company.manage.units.addresses.line1' />${fn:escapeXml(address.line1)}</li>
										</c:if>
										<c:if test="${not empty address.line2}">
											<li><spring:theme
													code='text.company.manage.units.addresses.line2' />${fn:escapeXml(address.line2)}</li>
										</c:if>
										<c:if test="${not empty address.town}">
											<li><spring:theme
													code='text.company.manage.units.addresses.town' />${fn:escapeXml(address.town)}</li>
										</c:if>
										<c:if test="${not empty address.postalCode}">
											<li><spring:theme
													code='text.company.manage.units.addresses.postalCode' />${fn:escapeXml(address.postalCode)}</li>
										</c:if>
										<c:if test="${not empty address.country.name}">
											<li><spring:theme
													code='text.company.manage.units.addresses.country.name' />${fn:escapeXml(address.country.name)}</li>
										</c:if>
										<c:if test="${not empty address.email}">
											<li><spring:theme
													code='text.company.manage.units.addresses.email' />${fn:escapeXml(address.email)}</li>
										</c:if>
										<c:if test="${not empty address.phone}">
											<li><spring:theme
													code='text.company.manage.units.addresses.phone' />${fn:escapeXml(address.phone)}</li>
										</c:if>
										<c:if test="${not empty address.fax}">
											<li><spring:theme
													code='text.company.manage.units.addresses.fax' />${fn:escapeXml(address.fax)}</li>
										</c:if>
									</ul>
									<c:if test="${shippingAddress.id != address.id}">
										<div class="account-cards-actions pull-left">
											<a href="${editUnitAddressUrl}" class="edit-item"
												title="<spring:theme code='text.company.manage.units.edit'/>">
												<span class="glyphicon glyphicon-pencil"></span>
											</a> <span class="js-action-confirmation-modal"> <a
												href="${removeUnitAddressUrl}" class="remove-item"
												title="<spring:theme code='text.company.manage.units.remove'/>"
												data-action-confirmation-modal-title="<spring:theme code='text.company.manage.units.delete.address'/>"
												data-action-confirmation-modal-id="delete-address"> <span
													class="glyphicon glyphicon-remove"></span>
											</a>
											</span>
										</div>
									</c:if>
								</div>
							</c:if>
						</c:forEach>
					</div>
					<company:actionConfirmationModal id="delete-address" targetUrl=""
						actionButtonAsLink="true" useSourceElementUrl="true"
						messageKey="text.company.manage.units.delete.address.message"
						actionButtonLabelKey="text.company.delete.button" />
				</div>


				<div class="account-list-header">
					<spring:theme code="text.company.manage.units.billingAddress" />
				</div>
				<div class="account-cards">
					<div class="row">


						<c:set var="address" value="${unit.billingAddress}" />
						<%-- 						<c:forEach items="${unit.billingAddress}" var="address"> --%>
						<%-- 							<c:if test="${address.billingAddress == true}"> --%>

						<div class="col-xs-12 col-sm-6 col-md-4 card">
							<c:if test="${address.inheritedFromParentUnit==true}">
								<ul class="pull-left">
									<li><spring:theme
											code="text.company.manage.units.billingAddress.inheritedFromParentUnit" /></li>
								</ul>
							</c:if>
							<%-- 							<spring:url --%>
							<%-- 								value="/my-company/organization-management/manage-units/edit-address/" --%>
							<%-- 								var="editUnitAddressUrl" htmlEscape="false"> --%>
							<%-- 								<spring:param name="unit" value="${unit.uid}" /> --%>
							<%-- 								<spring:param name="addressId" value="${address.id}" /> --%>
							<%-- 							</spring:url> --%>
							<%-- 							<spring:url --%>
							<%-- 								value="/my-company/organization-management/manage-units/remove-address/" --%>
							<%-- 								var="removeUnitAddressUrl" htmlEscape="false"> --%>
							<%-- 								<spring:param name="unit" value="${unit.uid}" /> --%>
							<%-- 								<spring:param name="addressId" value="${address.id}" /> --%>
							<%-- 							</spring:url> --%>
							<ul class="pull-left">
								<c:if test="${not empty address.line1}">
									<li><spring:theme code='text.company.manage.units.addresses.line1' />${fn:escapeXml(address.line1)}</li>
								</c:if>
								<c:if test="${not empty address.line2}">
									<li><spring:theme code='text.company.manage.units.addresses.line2' />${fn:escapeXml(address.line2)}</li>
								</c:if>
								<c:if test="${not empty address.town}">
									<li><spring:theme code='text.company.manage.units.addresses.town' />${fn:escapeXml(address.town)}</li>
								</c:if>
								<c:if test="${not empty address.postalCode}">
									<li><spring:theme code='text.company.manage.units.addresses.postalCode' />${fn:escapeXml(address.postalCode)}</li>
								</c:if>
								<c:if test="${not empty address.country.name}">
									<li><spring:theme code='text.company.manage.units.addresses.country.name' />${fn:escapeXml(address.country.name)}</li>
								</c:if>
								<c:if test="${not empty address.email}">
									<li><spring:theme code='text.company.manage.units.addresses.email' />${fn:escapeXml(address.email)}</li>
								</c:if>
								<c:if test="${not empty address.phone}">
									<li><spring:theme code='text.company.manage.units.addresses.phone' />${fn:escapeXml(address.phone)}</li>
								</c:if>
								<c:if test="${not empty address.fax}">
									<li><spring:theme code='text.company.manage.units.addresses.fax' />${fn:escapeXml(address.fax)}</li>
								</c:if>
							</ul>
							<!-- 									<div class="account-cards-actions pull-left"> -->
							<%-- 										<a href="${editUnitAddressUrl}" class="edit-item" --%>
							<%-- 											title="<spring:theme code='text.company.manage.units.edit'/>"> --%>
							<!-- 											<span class="glyphicon glyphicon-pencil"></span> -->
							<!-- 										</a> <span class="js-action-confirmation-modal"> <a -->
							<%-- 											href="${removeUnitAddressUrl}" class="remove-item" --%>
							<%-- 											title="<spring:theme code='text.company.manage.units.remove'/>" --%>
							<%-- 											data-action-confirmation-modal-title="<spring:theme code='text.company.manage.units.delete.address'/>" --%>
							<!-- 											data-action-confirmation-modal-id="delete-address"> <span -->
							<!-- 												class="glyphicon glyphicon-remove"></span> -->
							<!-- 										</a> -->
							<!-- 										</span> -->
							<!-- 									</div> -->
						</div>
						<%-- 							</c:if> --%>
						<%-- 						</c:forEach> --%>
					</div>
					<company:actionConfirmationModal id="delete-address" targetUrl=""
						actionButtonAsLink="true" useSourceElementUrl="true"
						messageKey="text.company.manage.units.delete.address.message"
						actionButtonLabelKey="text.company.delete.button" />
				</div>

				<!--                 <div class="account-list-header"> -->
				<%--                     <spring:theme code="text.company.manage.units.accountManagers"/> (${fn:length(unit.accountManagers)}) --%>
				<!--                 </div> -->
				<!--                 <div class="account-cards"> -->
				<!--                     <div class="row"> -->
				<%--                         <c:forEach items="${unit.accountManagers}" var="accountManager"> --%>
				<!--                             <div class="col-xs-12 col-sm-6 col-md-4 card"> -->
				<!--                                 <ul class="pull-left"> -->
				<%--                                     <li>${fn:escapeXml(accountManager.name)}</li> --%>
				<!--                                 </ul> -->
				<!--                             </div> -->
				<%--                         </c:forEach> --%>
				<!--                     </div> -->
				<!--                 </div> -->

				<%--                 <org-common:selectEntityHeadline url="${addCostcenterUrl}" labelKey="text.company.manage.units.costCenters" --%>
				<%--                                                  count="${fn:length(unit.costCenters)}" addNew="true"/> --%>
				<!--                 <div class="account-cards"> -->
				<!--                     <div class="row"> -->
				<%--                         <c:forEach items="${unit.costCenters}" var="b2bCostCenter"> --%>
				<!--                             <div class="col-xs-12 col-sm-6 col-md-4 card"> -->
				<%--                                 <spring:url value="/my-company/organization-management/manage-units/costcenter/" var="viewCostcenterUrl" htmlEscape="false"> --%>
				<%--                                     <spring:param name="unit" value="${unit.uid}"/> --%>
				<%--                                     <spring:param name="costCenterCode" value="${b2bCostCenter.code}"/> --%>
				<%--                                 </spring:url> --%>
				<!--                                 <ul class="pull-left"> -->
				<!--                                     <li> -->
				<%--                                         <a href="${viewCostcenterUrl}">${fn:escapeXml(b2bCostCenter.code)}</a> --%>
				<!--                                     </li> -->
				<%--                                     <li>${fn:escapeXml(b2bCostCenter.name)}</li> --%>
				<%--                                     <li>${fn:escapeXml(b2bCostCenter.currency.name)}</li> --%>
				<!--                                 </ul> -->
				<!--                             </div> -->
				<%--                         </c:forEach> --%>
				<!--                     </div> -->
				<!--                 </div> -->

				<%--                 <org-common:selectEntityHeadline url="${createUnitUrl}" labelKey="text.company.manage.units.childUnits" --%>
				<%--                                                  count="${fn:length(unit.children)}" addNew="false"/> --%>
				<div class="account-list-header">
					<spring:theme code="text.company.manage.units.childUnits" />
					(${fn:length(unit.children)})
				</div>
				<div class="account-cards">
					<div class="row">
						<c:forEach items="${unit.children}" var="unit">
							<div class="col-xs-12 col-sm-6 col-md-4 card">
								<spring:url
									value="/my-company/organization-management/manage-units/details"
									var="unitUrl" htmlEscape="false">
									<spring:param name="unit" value="${unit.uid}" />
								</spring:url>
								<ul class="pull-left">
									<li><a href="${unitUrl}">${fn:escapeXml(unit.erpCode)}</a></li>
									<li>${fn:escapeXml(unit.name)}</li>
								</ul>
							</div>
						</c:forEach>
					</div>
				</div>

				<spring:url
					value="/my-company/organization-management/manage-units/createuser"
					var="createUserUrl" htmlEscape="false">
					<spring:param name="unit" value="${unit.uid}" />
				</spring:url>
				<%-- 				<spring:url --%>
				<%-- 					value="/my-company/organization-management/manage-units/administrators" --%>
				<%-- 					var="editAdministratorUrl" htmlEscape="false"> --%>
				<%-- 					<spring:param name="unit" value="${unit.uid}" /> --%>
				<%-- 					<spring:param name="role" value="b2badmingroup" /> --%>
				<%-- 				</spring:url> --%>
				<%-- 				<spring:url --%>
				<%-- 					value="/my-company/organization-management/manage-units/managers" --%>
				<%-- 					var="editManagersUrl" htmlEscape="false"> --%>
				<%-- 					<spring:param name="unit" value="${unit.uid}" /> --%>
				<%-- 					<spring:param name="role" value="b2bmanagergroup" /> --%>
				<%-- 				</spring:url> --%>
				<%-- 				<spring:url --%>
				<%-- 					value="/my-company/organization-management/manage-units/approvers" --%>
				<%-- 					var="editApproversUrl" htmlEscape="false"> --%>
				<%-- 					<spring:param name="unit" value="${unit.uid}" /> --%>
				<%-- 				</spring:url> --%>
				<spring:url
					value="/my-company/organization-management/manage-units/customers"
					var="editCustomersUrl" htmlEscape="false">
					<spring:param name="unit" value="${unit.uid}" />
					<spring:param name="role" value="b2bcustomergroup" />
				</spring:url>

				<%-- 				<company:userList users="${unit.approvers}" action="approvers" --%>
				<%-- 					role="b2bapprovergroup" editUrl="${editApproversUrl}" --%>
				<%-- 					createUrl="${createUserUrl}" /> --%>

				<%-- 				<company:userList users="${unit.administrators}" --%>
				<%-- 					action="administrators" role="b2badmingroup" --%>
				<%-- 					editUrl="${editAdministratorUrl}" createUrl="${createUserUrl}" /> --%>

				<%-- 				<company:userList users="${unit.managers}" action="managers" --%>
				<%-- 					role="b2bmanagergroup" editUrl="${editManagersUrl}" --%>
				<%-- 					createUrl="${createUserUrl}" /> --%>

				<company:userList users="${unit.customers}" action="customers"
					role="b2bcustomergroup" editUrl="${editCustomersUrl}"
					createUrl="${createUserUrl}" />
			</div>
		</div>
	</div>
</template:page>