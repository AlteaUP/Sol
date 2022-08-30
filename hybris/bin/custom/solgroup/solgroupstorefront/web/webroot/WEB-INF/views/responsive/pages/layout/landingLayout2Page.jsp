<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>

<template:page pageTitle="${pageTitle}">
    <cms:pageSlot position="Section1" var="feature">
        <cms:component component="${feature}" />
    </cms:pageSlot>
    
    
    <div class="row application_category_spacer">
        <div class="col-xs-12 col-md-4">
            <cms:pageSlot position="Section2A" var="feature" element="div" class="row">
                <cms:component component="${feature}" element="div" class="col-xs-4 col-sm-4 yComponentWrapper"/>
            </cms:pageSlot>
        </div>
        <div class="col-xs-12 col-md-4">
            <cms:pageSlot position="Section2B" var="feature" element="div" class="row">
                <cms:component component="${feature}" element="div" class="col-xs-4 col-sm-4 yComponentWrapper"/>
            </cms:pageSlot>
        </div>
        <div class="col-xs-12 col-md-4">
            <cms:pageSlot position="Section2C" var="feature" element="div" class="row">
                <cms:component component="${feature}" element="div" class="col-xs-4 col-sm-4 yComponentWrapper"/>
            </cms:pageSlot>
        </div>
    </div>
    
	
    <cms:pageSlot position="Section3" var="feature" element="div">
        <cms:component component="${feature}" element="div" class="no-space yComponentWrapper"/>
    </cms:pageSlot>

    <cms:pageSlot position="Section5" var="feature" element="div">
        <cms:component component="${feature}" element="div" class="yComponentWrapper"/>
    </cms:pageSlot>

</template:page>
