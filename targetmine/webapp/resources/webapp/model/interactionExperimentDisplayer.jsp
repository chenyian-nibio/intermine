<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="detail" value="${reportObject.object}"/>

<h3>Experiment</h3>
<c:choose>
  <c:when test="${empty detail.experiment}">
    	<p style="margin: 10px;">No information.</p>
  </c:when>
  <c:otherwise>
		<table>
		<thead>
		<tr>
			<th>Publication.PubMedID</th>
			<th>Interaction Detection Methods</th>
		</tr>
		</thead>
		<tbody>
		<tr>
			<td><a href="report.do?id=${detail.experiment.publication.id}">${detail.experiment.publication.pubMedId}</a></td>
			<td>
				<c:forEach var="miTerm" items="${detail.experiment.interactionDetectionMethods}" varStatus="status">
				<a href="report.do?id=${miTerm.id}">${miTerm.name} (${miTerm.identifier})</a>
				<c:if test="${status.count < fn:length(reaction.types)}">,&nbsp;</c:if> 
				</c:forEach>
			</td>
		</tr>
		</tbody>
		</table>

  </c:otherwise>
</c:choose>
