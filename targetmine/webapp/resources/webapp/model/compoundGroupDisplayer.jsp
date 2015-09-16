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
		    	<td><a href="report.do?id=${compound.id}">${compound.identifier}</a></td>
		    	<td>${compound.name}</td>
		    	<td>${compound.inchiKey}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>

<div style="height: 30px;"></div>

<c:choose>
	<c:when test="${empty proteins}">
		<h3>0 Interacting Proteins</h3>
    	<p style="margin: 10px;">No interacting proteins.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(proteins)} Interacting Protein<c:if test="${fn:length(proteins) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
		<tr>
			<th>DB identifier</th>
			<th>Primary Accession</th>
			<th>Name</th>
			<th>Organism . Name</th>
			<th>Data Sets</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="protein" items="${proteins}">
		    <tr>
		    	<td><a href="report.do?id=${protein.id}">${protein.primaryIdentifier}</a></td>
		    	<td><a href="report.do?id=${protein.id}">${protein.primaryAccession}</a></td>
		    	<td>${protein.name}</td>
		    	<td>${protein.organism.name}</td>
		    	<td>
		    		<c:forEach var="ds" items="${dataSets[protein.primaryIdentifier]}">
		    			<span style="padding: 0 2px; color: white; background-color: ${colorMap[ds]}; font-weight: bold;" title="${nameMap[ds]}">
		    				${ds}
		    			</span>
		    		</c:forEach>
		    	</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</c:otherwise>
</c:choose>

</div>