<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:choose>
	<c:when test="${empty geneticDiseaseList}">
		<h3>No genetic disease association.</h3>
	</c:when>
	<c:otherwise>
		<h3>
			${fn:length(geneticDiseaseList)} Genetic disease association<c:if test="${fn:length(geneticDiseaseList) > 1}">s</c:if>
		</h3>
		<table>
		<thead>
			<tr>
				<th style="min-width: 390px;">Disease</th>
				<th style="max-width: 290px;">Clinical significant <br/> (ClinVar)</th>
				<th style="max-width: 290px;">p-value <br/> (GWAS catalog)</th>
				<th style="max-width: 30px;">Disease <br/> MeSH</th>
				<th>Number of <br/>publications</th>
				<th>Number of <br/>SNPs</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="entryInfo" items="${geneticDiseaseList}">
	    	<tr>
	    		<td>${entryInfo['diseaseColumn']}</td>
	    		<td style="max-width: 290px;">${entryInfo['clinvarColumn']}</td>
	    		<td style="max-width: 290px;">${entryInfo['gwasColumn']}</td>
	    		<td style="max-width: 30px;">${entryInfo['diseaseMeshColumn']}</td>
	    		<td>${entryInfo['pubCountColumn']}</td>
	    		<td>${entryInfo['snpCountColumn']}</td>
	    	</tr>
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
		    	<td><a href="report.do?id=${disease.id}">${disease.diseaseTerm.name}</a></td>
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
		    	<td><a href="report.do?id=${disease.id}">${disease.diseaseTerm.name}</a></td>
		    	<td>${disease.dataSet.name}</td>
		    </tr>
		</c:forEach>
		</tbody>
		</table>
	</div>
	</c:otherwise>
</c:choose>
