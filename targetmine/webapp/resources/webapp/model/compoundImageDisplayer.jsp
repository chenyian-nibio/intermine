<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="compound" value="${reportObject.object}"/>

<h3>Structure</h3>

<c:choose>
	<c:when test="${empty compound.inchiKey}">
    	<p style="margin: 10px;">No InChIKey annotation is available.</p>
	</c:when>
	<c:otherwise>

	<p style="margin-top: 8px;">
		InChIKey: ${compound.inchiKey} <br/>
		<img src="https://cactus.nci.nih.gov/chemical/structure/InChIKey=${compound.inchiKey}/image" />
		<br/>
		<span style="font-size: 8px;">Provided by <a href="https://cactus.nci.nih.gov/" target="_blank">The CACTUS web server</a></span>
	</p>
	</c:otherwise>
</c:choose>

</div>
