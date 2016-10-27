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
					<th style="width: 180px;">Modification Type</th>
					<th>Positions</th>
			    </tr>
			  </thead>
			  <tbody>
				<c:forEach items="${typeList}" var="type">
			    <tr>
			    	<td>${type}</td>
			    	<td>
		    			<c:forEach var="modification" items="${modificationMap[type]}" varStatus="status">
		    				<a href="report.do?id=${modification.id}">${modification.position}</a>
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

	<c:if test="${pageNote}">
		<div style="font-size: 8px; margin: 12px 12px;">
			Contains the data derived from <a href="http://www.phosphosite.org/uniprotAccAction?id=${reportObject.object.primaryAccession}" target="_blank">PhosphoSitePlus&reg; (PSP)</a>. The PSP data is not for commercial use.
		</div>
	</c:if>
</div>
