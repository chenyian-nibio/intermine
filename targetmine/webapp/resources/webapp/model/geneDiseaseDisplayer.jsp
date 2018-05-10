<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:choose>
	<c:when test="${empty geneticDiseaseTable}">
		<h3>No genetic disease association.</h3>
	</c:when>
	<c:otherwise>
		<h3>
			Genetic disease association
		</h3>
		<table>
		<thead>
			<tr>
				<th>Disease</th>
				<th>SNP</th>
				<th>Functional consequence</th>
				<th>Frequency</th>
				<th>Clinical significant<br/>(ClinVar)</th>
				<th>GWAS p-value<br/>(GWAS catalog)</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="line" items="${geneticDiseaseTable}">
	    	<tr>
	    		<td>${line[0]}</td>
	    		<td>${line[1]}</td>
	    		<td>${line[2]}</td>
	    		<td>${line[3]}</td>
	    		<td>${line[4]}</td>
	    		<td>${line[5]}</td>
	    	</tr>
		</c:forEach>
		</tbody>
		</table>
		
	</c:otherwise>
</c:choose>
</div>