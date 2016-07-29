<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


<c:set var="domain" value="${reportObject.object}"/>

<h2>Domain relationships</h2>

<c:choose>
	<c:when test="${empty domain.foundIn}">
		<!-- show nothing? -->
	</c:when>
	<c:otherwise>
	<div class="collection-table">
		<h3>
			Found in ${fn:length(domain.foundIn)} InterPro Entries
		</h3>
		<table>
		<thead>
			<tr>
				<th>DB identifier</th>
				<th>Name</th>
				<th>Short Name</th>
				<th>Type</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${domain.foundIn}">
		    <tr>
		    	<td><a href="report.do?id=${entry.id}">${entry.primaryIdentifier}</a></td>
		    	<td>${entry.name}</td>
		    	<td>${entry.shortName}</td>
		    	<td>${entry.type}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	<br/>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty domain.parentFeatures}">
		<!-- show nothing? -->
	</c:when>
	<c:otherwise>
	<div class="collection-table">
		<h3>
			${fn:length(domain.parentFeatures)} Parent Entries
		</h3>
		<table>
		<thead>
			<tr>
				<th>DB identifier</th>
				<th>Name</th>
				<th>Short Name</th>
				<th>Type</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${domain.parentFeatures}">
		    <tr>
		    	<td><a href="report.do?id=${entry.id}">${entry.primaryIdentifier}</a></td>
		    	<td>${entry.name}</td>
		    	<td>${entry.shortName}</td>
		    	<td>${entry.type}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	<br/>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty domain.childFeatures}">
		<!-- show nothing? -->
	</c:when>
	<c:otherwise>
	<div class="collection-table">
		<h3>
			${fn:length(domain.childFeatures)} Child Entries
		</h3>
		<table>
		<thead>
			<tr>
				<th>DB identifier</th>
				<th>Name</th>
				<th>Short Name</th>
				<th>Type</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${domain.childFeatures}">
		    <tr>
		    	<td><a href="report.do?id=${entry.id}">${entry.primaryIdentifier}</a></td>
		    	<td>${entry.name}</td>
		    	<td>${entry.shortName}</td>
		    	<td>${entry.type}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	<br/>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty domain.contains}">
		<!-- show nothing? -->
	</c:when>
	<c:otherwise>
	<div class="collection-table">
		<h3>
			Contains ${fn:length(domain.contains)} InterPro Entries
		</h3>
		<table>
		<thead>
			<tr>
				<th>DB identifier</th>
				<th>Name</th>
				<th>Short Name</th>
				<th>Type</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${domain.contains}">
		    <tr>
		    	<td><a href="report.do?id=${entry.id}">${entry.primaryIdentifier}</a></td>
		    	<td>${entry.name}</td>
		    	<td>${entry.shortName}</td>
		    	<td>${entry.type}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	</c:otherwise>
</c:choose>

<c:if test="${(empty domain.parentFeatures) && (empty domain.childFeatures) && (empty domain.contains) && (empty domain.foundIn)}">
    	<p style="margin: 10px;">No relationship has been defined for this entry.</p>
</c:if>

<p><hr></p>
