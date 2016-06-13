<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<h3>Modifications</h3>

  	<c:choose>
	  	<c:when test="${!empty modificationMap}">
			<table>
			  <thead>
			    <tr>
					<th>Modification Type</th>
					<th>Positions</th>
			    </tr>
			  </thead>
			  <tbody>
				<c:forEach items="${typeList}" var="type">
			    <tr>
			    	<td>${type}</td>
			    	<td>
		    			<c:forEach var="feature" items="${modificationMap[type]}" varStatus="status">
		    				<a href="report.do?id=${feature.id}" title="${feature.description}">${feature.begin}</a>
		    				<c:if test="${status.count < fn:length(modificationMap[type])}">, </c:if>
		    			</c:forEach>
			    	</td>
				</tr>
		    	</c:forEach>
			  </tbody>
			</table>
		</c:when>
		<c:otherwise>
			<p style="font-style:italic;">No modification.</p>
		</c:otherwise>
	</c:choose>

</div>
