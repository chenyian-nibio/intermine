<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="interaction" value="${reportObject.object}"/>

<h3>Transcription factor regulation</h3>


<table>
  <thead>
    <tr>
		<th colspan="3">Transcription factor</th>
		<th colspan="3">Targeted Gene</th>
    </tr>
  </thead>
  <tbody>
    <tr>
	<c:choose>
		<c:when test="${interaction.role == 'source'}">
			<td><a href="report.do?id=${interaction.gene.id}">${interaction.gene.primaryIdentifier}</a></td>
			<td><a href="report.do?id=${interaction.gene.id}">${interaction.gene.symbol}</a></td>
			<td>${interaction.gene.name}</td>
			<td><a href="report.do?id=${interaction.interactWith.id}">${interaction.interactWith.primaryIdentifier}</a></td>
			<td><a href="report.do?id=${interaction.interactWith.id}">${interaction.interactWith.symbol}</a></td>
			<td>${interaction.interactWith.name}</td>
		</c:when>
		<c:otherwise>
			<td><a href="report.do?id=${interaction.interactWith.id}">${interaction.interactWith.primaryIdentifier}</a></td>
			<td><a href="report.do?id=${interaction.interactWith.id}">${interaction.interactWith.symbol}</a></td>
			<td>${interaction.interactWith.name}</td>
			<td><a href="report.do?id=${interaction.gene.id}">${interaction.gene.primaryIdentifier}</a></td>
			<td><a href="report.do?id=${interaction.gene.id}">${interaction.gene.symbol}</a></td>
			<td>${interaction.gene.name}</td>
		</c:otherwise>
	</c:choose>
	</tr>
  </tbody>
</table>

</div>
