<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="user" tagdir="/WEB-INF/tags/responsive/user" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:theme code="identityConfirmation.title" var="pageTitle"/>

<template:page pageTitle="${pageTitle}">
	<user:identityConfirmation/>
</template:page>
