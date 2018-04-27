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
	<c:when test="${empty gene.keggOrthology}">
    	<p style="margin: 10px;">No KO annotation.</p>
	</c:when>
	<c:otherwise>

		<table>
		<c:forEach var="koEntry" items="${koSet}">
		<thead>
			<tr>
				<th><a href="report.do?id=${koEntry.id}">${fn:substringAfter(koEntry.identifier, ":")}</a></th>
			</tr>
		</thead>
		<tbody>
	    	<tr>
	    		<td style="padding-left: 16px; padding-bottom: 12px;">
	    			<c:forTokens var="type" items="paralogue orthologue" delims=" ">
	    				<c:if test="${!empty koMap[koEntry][type]}"> 
	    				<div><b>${type}</b><br/>
			    			<c:forEach var="sample" items="${koMap[koEntry][type]}" varStatus="status">
			    				<a href="report.do?id=${sample.id}" title="${sample.organism.shortName}">${sample.primaryIdentifier} (${sample.symbol})</a><c:if test="${status.count < fn:length(koMap[koEntry][type])}">, </c:if> 
			    			</c:forEach>
		    			</div>
		    			</c:if>
	    			</c:forTokens>
	    		</td>
	    	</tr>
		</tbody>
		</c:forEach>
		</table>
		
	</c:otherwise>
</c:choose>

</div>