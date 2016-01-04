<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


<c:set var="gene" value="${reportObject.object}"/>

<c:choose>
	<c:when test="${empty gene.reactions}">
<div class="collection-table gray">
		<h3>0&nbsp;Reactions</h3>
	</c:when>
	<c:otherwise>
<div class="collection-table">
		<h3>
			${fn:length(gene.reactions)} Reactions
		</h3>
		<table>
		<thead>
			<tr>
				<th >Entities</th>
				<th>Types</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="reaction" items="${gene.reactions}">
		    <tr>
		    	<td><a href="report.do?id=${reaction.id}"><span style="font-size: 10px; background: #666; color: #FFF; padding: 1px;">Reaction:</span></a>&nbsp;&nbsp; 
		    		<a href="report.do?id=${reaction.gene1.id}">${reaction.gene1.symbol} (${reaction.gene1.primaryIdentifier})</a>
		    		&nbsp;&rArr;&nbsp;
		    		<a href="report.do?id=${reaction.gene2.id}">${reaction.gene2.symbol} (${reaction.gene2.primaryIdentifier})</a>
		    	</td>
		    	<td>
		    		<c:forEach var="type" items="${reaction.types}" varStatus="status">
		    			<a href="report.do?id=${type.id}">${type.name}</a>
		    			<c:if test="${status.count < fn:length(reaction.types)}">,&nbsp;</c:if> 
		    		</c:forEach>
		    	</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>

</div>