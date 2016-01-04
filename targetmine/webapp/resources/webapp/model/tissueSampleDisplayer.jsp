<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:choose>
	<c:when test="${empty platformMap}">
		<h3>No Samples</h3>
	</c:when>
	<c:otherwise>
		<h3>
			${total} Sample<c:if test="${total > 1}">s</c:if>
		</h3>
		<table>
		<c:forEach var="platform" items="${platformSet}">
		<thead>
			<tr>
				<th><br/><b>Platform: <a href="report.do?id=${platform.id}">${platform.title} (${platform.identifier})</a></b> - ${fn:length(platformMap[platform])} samples</th>
			</tr>
		</thead>
		<tbody>
	    	<tr>
	    		<td style="padding-left: 16px; padding-bottom: 12px;">
	    			<c:forEach var="sample" items="${platformMap[platform]}" varStatus="status">
	    				<a href="report.do?id=${sample.id}" title="${sample.identifier}">${sample.identifier}</a>
	    				<c:if test="${status.count < fn:length(platformMap[platform])}">, </c:if> 
	    			</c:forEach>
	    		</td>
	    	</tr>
		</tbody>
		</c:forEach>
		</table>
		
	</c:otherwise>
</c:choose>
</div>