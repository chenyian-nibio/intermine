<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="detail" value="${reportObject.object}"/>

<h3>Interaction and PPI Druggability</h3>


<c:choose>
  <c:when test="${empty detail.interaction.ppiDruggability}">
    <table>
    <thead>
    <tr>
    	<th>Confidence</th>
    	<th>Gene 1 . Symbol</th>
    	<th>Gene 2 . Symbol</th>
    	<th>&nbsp;</th>
    </tr>
    </thead>
    <tbody>
    <tr>
    	<td>${detail.interaction.confidence}</td>
    	<td>${detail.interaction.gene1.symbol}</td>
    	<td>${detail.interaction.gene2.symbol}</td>
    	<td>No PPI Druggability available.</td>
    </tr>
    </tbody>
    </table>
  </c:when>
  <c:otherwise>
    <table>
    <thead>
    <tr>
    	<th>Confidence</th>
    	<th>Gene 1 . Symbol</th>
    	<th>Gene 2 . Symbol</th>
    	<th>Structural Score</th>
    	<th>DrugChemical Score</th>
    	<th>Functional Score</th>
    	<th>All Score</th>
    	<th>&nbsp;</th>
    </tr>
    </thead>
    <tbody>
    <tr>
    	<td>${detail.interaction.confidence}</td>
    	<td>${detail.interaction.gene1.symbol}</td>
    	<td>${detail.interaction.gene2.symbol}</td>
    	<td>${detail.interaction.ppiDruggability.structuralScore}	</td>
    	<td>${detail.interaction.ppiDruggability.drugChemicalScore}	</td>
    	<td>${detail.interaction.ppiDruggability.functionalScore}	</td>
    	<td>${detail.interaction.ppiDruggability.allScore}	</td>
    	<td><a href="http://www.drpias.net/view_entry.php?entrezgene_id=${detail.interaction.gene1.primaryIdentifier}&entrezgene_id_2=${detail.interaction.gene2.primaryIdentifier}" target="_blank">External link (Dr.PIAS)</a></td>
    </tr>
    </tbody>
    </table>
  </c:otherwise>
</c:choose>
