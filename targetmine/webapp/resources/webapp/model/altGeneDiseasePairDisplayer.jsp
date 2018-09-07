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
			${numOfAssociations} Genetic disease association<c:if test="${numOfAssociations > 1}">s</c:if>
		</h3>
		<table>
			<thead>
				<tr>
					<th style="min-width: 390px;"><b>GWAS catalog</b></th>
					<th style="min-width: 150px;">
						<c:if test="${!empty geneticDiseaseList['gwas']}">p-value</c:if> &nbsp;
					</th>
					<th style="min-width: 82px;">Number of publications</th>
					<th style="min-width: 82px;">Number of SNPs</th>
				</tr>
			</thead>
			<tbody>
				<c:choose>
					<c:when test="${empty geneticDiseaseList['gwas']}">
						<tr>
							<td class="smallnote" colspan="4"><i>No items in this category.</i></td>
						</tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="entryInfo" items="${geneticDiseaseList['gwas']}">
					    	<tr>
					    		<td>${entryInfo['diseaseColumn']}</td>
					    		<td>${entryInfo['gwasColumn']}</td>
					    		<td>${entryInfo['pubCountColumn']}</td>
					    		<td>${entryInfo['snpCountColumn']}</td>
					    	</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</tbody>
			<thead>
				<tr>
					<th><b>ClinVar</b></th>
					<th colspan="3">
						<c:if test="${!empty geneticDiseaseList['clinvar']}">Clinical significant</c:if> &nbsp;
					</th>
				</tr>
			</thead>
			<tbody>
				<c:choose>
					<c:when test="${empty geneticDiseaseList['clinvar']}">
						<tr>
							<td class="smallnote" colspan="4"><i>No items in this category.</i></td>
						</tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="entryInfo" items="${geneticDiseaseList['clinvar']}">
					    	<tr>
					    		<td>${entryInfo['diseaseColumn']}</td>
					    		<td>${entryInfo['clinvarColumn']}</td>
					    		<td>${entryInfo['pubCountColumn']}</td>
					    		<td>${entryInfo['snpCountColumn']}</td>
					    	</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</tbody>
			<thead>
				<tr><th colspan="4"><b>dbSNP-PubMed-MeSH</b></th></tr>
			</thead>
			<tbody>
				<c:choose>
					<c:when test="${empty geneticDiseaseList['dbsnpMesh']}">
						<tr>
							<td class="smallnote" colspan="4"><i>No items in this category.</i></td>
						</tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="entryInfo" items="${geneticDiseaseList['dbsnpMesh']}">
					    	<tr>
					    		<td>${entryInfo['diseaseColumn']}</td>
					    		<td>&nbsp;</td>
					    		<td>${entryInfo['pubCountColumn']}</td>
					    		<td>${entryInfo['snpCountColumn']}</td>
					    	</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
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
