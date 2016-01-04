<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:choose>
	<c:when test="${empty expressionMap}">
		<h3>No Expressions</h3>
	</c:when>
	<c:otherwise>
		<h3>Expressions</h3>
		<table>
		<c:forEach var="platform" items="${platformSet}">
		<thead>
			<tr>
				<th><br/><b>Platform: <a href="report.do?id=${platform.id}">${platform.title} (${platform.identifier})</a></b> - ${fn:length(expressionMap[platform])} tissues & cells</th>
			</tr>
		</thead>
		<tbody>
	    	<tr>
	    		<td style="padding-left: 16px; padding-bottom: 12px;">
	    			<c:forEach var="exp" items="${expressionMap[platform]}" varStatus="status">
	    				<a href="report.do?id=${exp.id}" title="${exp.value}">${exp.tissue.name}</a>
	    				<c:if test="${status.count < fn:length(expressionMap[platform])}">, </c:if>
	    			</c:forEach>
	    		</td>
	    	</tr>
		</tbody>
		</c:forEach>
		</table>
		
	</c:otherwise>
</c:choose>
</div>