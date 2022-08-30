<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="footer" tagdir="/WEB-INF/tags/responsive/common/footer"  %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<c:if test="${component.visible}">
	<div class="container-fluid">
	    <div class="footer__top">


	        <div class="row">
	           
	           
	            <div class="footer__left col-xs-12 col-sm-12 col-md-3">
	                <div class="row">

						<c:if test="${footerImage ne null}">
							<div>
							
							<c:choose>
								<c:when test="${empty footerImageUrl}">
									<img title="${fn:escapeXml(footerImage.altText)}" alt="${fn:escapeXml(footerImage.altText)}" src="${footerImage.url}">
								</c:when>
								<c:otherwise>
									<a href="${footerImageUrl}" <c:if test="${footerImageUrlExternal}"> target="_blank"</c:if>>
										<img title="${fn:escapeXml(footerImage.altText)}" alt="${fn:escapeXml(footerImage.altText)}" src="${footerImage.url}">
									</a>
								</c:otherwise>
							</c:choose>
							
							</div>
						</c:if>

	               </div>
	           </div>
	           
	           
	           
	           
	           <div class="footer__right col-xs-12 col-md-9">

	                       <div class="col-xs-12 col-sm-1 footer__dropdown" style="float:right;padding-left:0px; padding-right:0px;">
	                           <footer:languageSelector languages="${languages}" currentLanguage="${currentLanguage}" />
	                       </div>


	                	<c:forEach items="${component.navigationNode.children}" var="childLevel1">
	                		<div class="footer__nav--container col-xs-12 col-sm-1" style="float:right;">
	                			<div class="title">
	                                <c:forEach items="${childLevel1.entries}" var="childlink" >
                                    	<cms:component component="${childlink.item}" evaluateRestriction="true" element="div" class="footer__nav--links footer__link"/>
	                                </c:forEach>
								</div>
	                		</div>
	                	</c:forEach>



	               <c:if test="${showLanguageCurrency}">
	                   <div class="row">
	                       <div class="col-xs-6 col-md-6 footer__dropdown">
	                           <footer:languageSelector languages="${languages}" currentLanguage="${currentLanguage}" />
	                       </div>
	                       <div class="col-xs-6 col-md-6 footer__dropdown">
	                           <footer:currencySelector currencies="${currencies}" currentCurrency="${currentCurrency}" />
	                       </div>
	                   </div>
	               </c:if>

	            </div>
	        </div>
	    </div>
	</div>
	
	<c:if test="${notice ne null}">
		<div class="footer__bottom">
		    <div class="footer__copyright">
		        <div class="container">${notice}</div>
		    </div>
		</div>
	</c:if>

</c:if>