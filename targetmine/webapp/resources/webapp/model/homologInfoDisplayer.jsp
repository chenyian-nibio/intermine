<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="gene" value="${reportObject.object}"/>

<h3>Orthologous gene (Bi-directional Best Hit)</h3>
<c:choose>
	<c:when test="${empty orthologs}">
		<p style="margin: 10px;">No orthologous gene.</p>
	</c:when>
	<c:otherwise>
		<table>
		<thead>
		<tr>
			<th>Organism</th>
			<th>DB Identifier</th>
			<th>Symbol</th>
		</tr>
		</thead>
		<tbody>
				<c:forEach var="entry" items="${orthologs}">
					<tr>
						<td><a href="report.do?id=${entry.organism.id}">${entry.organism.name}</a></td>
						<td><a href="report.do?id=${entry.id}">${entry.primaryIdentifier}</a></td>
						<td><a href="report.do?id=${entry.id}">${entry.symbol}</a></td>
					</tr>
				</c:forEach>
		</tbody>
		</table>
	</c:otherwise>
</c:choose>
<br/>
<h3>KEGG Orthology (KO)</h3>
<c:choose>
	<c:when test="${empty gene.homologues}">
    	<p style="margin: 10px;">No KO annotation.</p>
	</c:when>
	<c:otherwise>
		<table>
		<thead>
		<tr>
			<th>Type</th>
			<th>Organism</th>
			<th>DB Identifier</th>
			<th>Symbol</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${gene.homologues}">
			<tr>
				<td><a href="report.do?id=${entry.id}">${entry.type}</a></td>
				<td><a href="report.do?id=${entry.homologue.organism.id}">${entry.homologue.organism.name}</a></td>
				<td><a href="report.do?id=${entry.homologue.id}">${entry.homologue.primaryIdentifier}</a></td>
				<td><a href="report.do?id=${entry.homologue.id}">${entry.homologue.symbol}</a></td>
			</tr>
		</c:forEach>
		</tbody>
		</table>
	</c:otherwise>
</c:choose>

</div>