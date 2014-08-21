<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="drug" value="${reportObject.object}"/>

<c:choose>
	<c:when test="${empty drug.atcCodes}">
		<h3>0 ATC Code</h3>
    	<p style="margin: 10px;">No ATC annotations.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(drug.atcCodes)} ATC Code<c:if test="${fn:length(drug.atcCodes) > 1}">s</c:if>
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
	<c:when test="${empty drug.jtcCodes}">
		<h3>0 JTC Code</h3>
    	<p style="margin: 10px;">No JTC annotations.</p>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(drug.jtcCodes)} JTC Code<c:if test="${fn:length(drug.jtcCodes) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
		<tr>
			<th width="100px">Code</th>
			<th>Hierarchy</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="jtcCode" items="${drug.jtcCodes}">
		    <tr>
		      <td><a href="report.do?id=${jtcCode.id}">${jtcCode.jtcCode}</a></td>
		      <td>
		      <a href="report.do?id=${jtcCode.parent.parent.parent.id}">${jtcCode.parent.parent.parent.jtcCode}</a> 
		      ${jtcCode.parent.parent.parent.name} &raquo; 
		      <a href="report.do?id=${jtcCode.parent.parent.id}">${jtcCode.parent.parent.jtcCode}</a> 
		      ${jtcCode.parent.parent.name} &raquo; 
		      <a href="report.do?id=${jtcCode.parent.id}">${jtcCode.parent.jtcCode}</a> 
		      ${jtcCode.parent.name} &raquo;
		      <a href="report.do?id=${jtcCode.id}">${jtcCode.jtcCode}</a> 
		      ${jtcCode.name}
		      </td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>

</div>