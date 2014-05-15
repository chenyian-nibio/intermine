<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="group" value="${reportObject.object}"/>

<c:choose>
	<c:when test="${empty group.compounds}">
		<h3>0 Compound</h3>
    	<p style="margin: 10px;">No associated compound.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(group.compounds)} Compound<c:if test="${fn:length(group.compounds) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
		<tr>
			<th>DB identifier</th>
			<th>Name</th>
			<th>InChIKey</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="compound" items="${group.compounds}">
		    <tr>
		    	<td><a href="report.do?id=${compound.id}">${compound.primaryIdentifier}</a></td>
		    	<td>${compound.name}</td>
		    	<td>${compound.inchiKey}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
		<div style="height: 30px;"></div>
		
		<h3>
			Interacting Proteins
		</h3>
		<table>
		<thead>
		<tr>
			<th>DB identifier</th>
			<th>Uniprot Accession</th>
			<th>Name</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="compound" items="${group.compounds}">
			<c:forEach var="interaction" items="${compound.targetProteins}">
		    <tr>
		    	<td><a href="report.do?id=${interaction.protein.id}">${interaction.protein.primaryIdentifier}</a></td>
		    	<td><a href="report.do?id=${interaction.protein.id}">${interaction.protein.primaryAccession}</a></td>
		    	<td>${interaction.protein.name}</td>
		    </tr>
			</c:forEach>
		</c:forEach>
		</tbody>
		</table>
	</c:otherwise>
</c:choose>
