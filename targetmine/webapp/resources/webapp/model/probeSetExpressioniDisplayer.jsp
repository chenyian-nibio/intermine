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
		<thead>
			<tr>
				<th width="500px">Platforms</th>
				<th>Tissues</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="platform" items="${platformSet}">
	    	<tr>
	    		<td>${platform.title} (${platform.identifier})</td>
	    		<td>
	    			<c:forEach var="exp" items="${expressionMap[platform]}">
	    				<a href="report.do?id=${exp.id}" title="${exp.value}">${exp.tissue.name}</a>, 
	    			</c:forEach>
	    		</td>
	    	</tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>
</div>