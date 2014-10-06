<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">
<h3>ATC Classification Hierarchy</h3>

  	<c:choose>
	  	<c:when test="${!empty allParents}">
		    <table>
		       <thead>
		       		<tr>
		        		<th>Code</th>
		        		<th>Name</th>
		        	</tr>
		        </thead>
		        <tbody>
				<c:forEach items="${allParents}" var="entry">
			    	<tr>
			    		<td><a href="report.do?id=${entry.id}">${entry.atcCode}</a></td>
			    		<td><a href="report.do?id=${entry.id}">${entry.name}</a></td>
			    	</tr>
				</c:forEach>
		    </table>
		</c:when>
		<c:otherwise>
			<p style="font-style:italic;">No information</p>
		</c:otherwise>
	</c:choose>
</div>
