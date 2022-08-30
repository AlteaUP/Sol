<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>

<template:page pageTitle="${pageTitle}">

	<cms:pageSlot position="Section1" var="feature">
		<cms:component component="${feature}" element="div" class=""/>
	</cms:pageSlot>
	
	<div class="container__full">
		<div class="row">
			<%-- 
			<cms:pageSlot position="Section2" var="feature" element="div" class="col-md-9">
			--%>
			<cms:pageSlot position="Section2" var="feature" element="div" class="col-md-12">
				<cms:component component="${feature}"/>
			</cms:pageSlot>
		</div>
	</div>
	
	<cms:pageSlot position="Section3" var="feature" element="div" >
		<cms:component component="${feature}"/>
	</cms:pageSlot>

</template:page>
