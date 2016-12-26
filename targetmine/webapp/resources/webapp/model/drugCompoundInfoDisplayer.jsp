<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="drug" value="${reportObject.object}"/>

<c:choose>
	<c:when test="${empty drug.atcCodes}">
		<h3>0 ATC Classification</h3>
    	<p style="margin: 10px;">No ATC classification.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(drug.atcCodes)} ATC Classification<c:if test="${fn:length(drug.atcCodes) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
		<tr>
			<th width="100px">Code</th>
			<th>Hierarchy</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="atcCode" items="${drug.atcCodes}">
		    <tr>
		      <td><a href="report.do?id=${atcCode.id}">${atcCode.atcCode}</a></td>
		      <td>
		      <a href="report.do?id=${atcCode.parent.parent.parent.parent.id}">${atcCode.parent.parent.parent.parent.atcCode}</a> 
		      ${atcCode.parent.parent.parent.parent.name} &raquo; 
		      <a href="report.do?id=${atcCode.parent.parent.parent.id}">${atcCode.parent.parent.parent.atcCode}</a> 
		      ${atcCode.parent.parent.parent.name} &raquo; 
		      <a href="report.do?id=${atcCode.parent.parent.id}">${atcCode.parent.parent.atcCode}</a> 
		      ${atcCode.parent.parent.name} &raquo; 
		      <a href="report.do?id=${atcCode.parent.id}">${atcCode.parent.atcCode}</a> 
		      ${atcCode.parent.name}
		      </td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>
<br/>
<c:choose>
	<c:when test="${empty drug.jsccCodes}">
		<h3>0 Japan Standard Commodity Classification(JSCC)</h3>
    	<p style="margin: 10px;">No JSCC classification.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(drug.jsccCodes)} Japan Standard Commodity Classification<c:if test="${fn:length(drug.jsccCodes) > 1}">s</c:if>(JSCC<c:if test="${fn:length(drug.jsccCodes) > 1}">s</c:if>)
		</h3>
		<table>
		<thead>
		<tr>
			<th width="100px">Code</th>
			<th>Hierarchy</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="jsccCode" items="${drug.jsccCodes}">
		    <tr>
		      <td><a href="report.do?id=${jsccCode.id}">${jsccCode.jsccCode}</a></td>
		      <td>
		      <a href="report.do?id=${jsccCode.parent.parent.parent.id}">${jsccCode.parent.parent.parent.jsccCode}</a> 
		      ${jsccCode.parent.parent.parent.name} &raquo; 
		      <a href="report.do?id=${jsccCode.parent.parent.id}">${jsccCode.parent.parent.jsccCode}</a> 
		      ${jsccCode.parent.parent.name} &raquo; 
		      <a href="report.do?id=${jsccCode.parent.id}">${jsccCode.parent.jsccCode}</a> 
		      ${jsccCode.parent.name} &raquo;
		      <a href="report.do?id=${jsccCode.id}">${jsccCode.jsccCode}</a> 
		      ${jsccCode.name}
		      </td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>
<br/>
<c:choose>
	<c:when test="${empty drug.uspClassifications}">
		<h3>0 USP drug classification</h3>
    	<p style="margin: 10px;">No USP drug classification.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(drug.uspClassifications)} USP drug classification<c:if test="${fn:length(drug.uspClassifications) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
		<tr>
			<th>Classification</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="uspc" items="${drug.uspClassifications}">
		    <tr>
		      <td>
		      <c:if test="${!empty uspc.parent.parent}">
			      <a href="report.do?id=${uspc.parent.parent.id}">${uspc.parent.parent.name}</a> 
			      &raquo; 
		      </c:if>
		      <a href="report.do?id=${uspc.parent.id}">${uspc.parent.name}</a> 
		      &raquo;
		      <a href="report.do?id=${uspc.id}">${uspc.name}</a> 
		      </td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>

<br/>
<c:choose>
	<c:when test="${empty drug.synonyms}">
		<h3>Synonym</h3>
    	<p style="margin: 10px;">No synonym.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(drug.synonyms)} Synonym<c:if test="${fn:length(drug.synonyms) > 1}">s</c:if>
		</h3>
		<table>
		<tbody>
	    	<tr>
	    		<td style="padding-left: 16px; padding-bottom: 12px;">
	    			<c:forEach var="synonym" items="${drug.synonyms}" varStatus="status">
	    				<a href="report.do?id=${synonym.id}" title="${synonym.value}">${synonym.value}</a>
	    				<c:if test="${!empty synonym.type}"> (${synonym.type})</c:if>
	    				<c:if test="${status.count < fn:length(drug.synonyms)}">, </c:if>
	    			</c:forEach>
	    		</td>
	    	</tr>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>


</div>