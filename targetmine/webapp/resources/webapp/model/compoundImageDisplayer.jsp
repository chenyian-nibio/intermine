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
    	<p style="margin: 10px;">Not available.</p>
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="${fn:startsWith(compound.identifier, 'ChEMBL') || fn:startsWith(compound.name, 'CHEMBL')}">
			
	<div id="structureimage">
		<img src="https://www.ebi.ac.uk/chembl/api/data/image/${compound.inchiKey}?dimensions=300" onerror="document.getElementById('structureimage').innerHTML = 'Not available.'"/>
		<br/>
		<span style="font-size: 8px;">Provided by <a href="https://www.ebi.ac.uk/chembl/ws" target="_blank">ChEMBL web service</a></span>
	</div>
			</c:when>
			<c:otherwise>

	<div id="structureimage">
		<img src="https://cactus.nci.nih.gov/chemical/structure/InChIKey=${compound.inchiKey}/image" onerror="document.getElementById('structureimage').innerHTML = 'Not available.'"/>
		<br/>
		<span style="font-size: 8px;">Provided by <a href="https://cactus.nci.nih.gov/" target="_blank">The CACTUS web server</a></span>
	</div>
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

</div>
