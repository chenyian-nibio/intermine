<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:choose>
	<c:when test="${empty snpList}">
		<h3>No genetic disease association.</h3>
	</c:when>
	<c:otherwise>
		<h3>
			Genetic disease association (${fn:length(snpList)} SNP<c:if test="${fn:length(snpList) > 1}">s</c:if>)
		</h3>
		<table>
		<thead>
			<tr>
				<th>Disease</th>
				<th>SNP</th>
				<th>Functional <br/>consequence</th>
				<th>Frequency</th>
				<th>Clinical significant (ClinVar) <br/>
					or p-value (GWAS catalog)</th>
				<th>Number of <br/>publications</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="snp" items="${snpList}">
			<c:set var="diseaseInfos" value="${diseaseInfoMap[snp]}"/>
			<c:forEach var="di" items="${diseaseInfos}" varStatus="status">
	    	<tr>
	    		<td>${di[0]}</td>
	    		<c:if test="${status.count == 1}">
		    		<td rowspan="${fn:length(diseaseInfos)}">${snpInfoMap[snp][0]}</td>
		    		<td rowspan="${fn:length(diseaseInfos)}">${snpInfoMap[snp][1]}</td>
		    		<td rowspan="${fn:length(diseaseInfos)}">${snpInfoMap[snp][2]}</td>
	    		</c:if>
	    		<td>${di[1]}</td>
	    		<td>${di[2]}</td>
	    	</tr>
			</c:forEach>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>
</div>

<c:choose>
	<c:when test="${empty disgenet}">
		<!-- show nothing?-->
	</c:when>
	<c:otherwise>
	<div class="collection-table">
		<h3>
			${fn:length(disgenet)} Disease association<c:if test="${fn:length(disgenet) > 1}">s</c:if> from DisGeNet
		</h3>
		<table>
		<thead>
		<tr>
			<th>Disease</th>
			<th>Number of publications</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="disease" items="${disgenet}">
		    <tr>
		    	<td><a href="report.do?id=${disease.id}">${disease.diseaseTerm.title}</a></td>
		    	<td><a href="report.do?id=${disease.id}">${fn:length(disease.publications)}</a></td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty others}">
		<!-- show nothing?-->
	</c:when>
	<c:otherwise>
	<div class="collection-table">
		<h3>
			${fn:length(others)} Other disease association<c:if test="${fn:length(others) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
		<tr>
			<th>Disease</th>
			<th>Source</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="disease" items="${others}">
		    <tr>
		    	<td><a href="report.do?id=${disease.id}">${disease.diseaseTerm.title}</a></td>
		    	<td>${disease.dataSet.name}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	</c:otherwise>
</c:choose>
