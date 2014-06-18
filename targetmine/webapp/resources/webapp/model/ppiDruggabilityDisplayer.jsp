<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="interaction" value="${reportObject.object}"/>

<h3>PPI Druggability</h3>


<c:choose>
  <c:when test="${empty interaction.ppiDruggability}">
    <p style="margin: 10px;">No PPI Druggability available.</p>
  </c:when>
  <c:otherwise>
    <table>
    <thead>
    <tr>
    	<th>Structural Score</th>
    	<th>DrugChemical Score</th>
    	<th>Functional Score</th>
    	<th>All Score</th>
    	<th>&nbsp;</th>
    </tr>
    </thead>
    <tbody>
    <tr>
    	<td>${interaction.ppiDruggability.structuralScore}	</td>
    	<td>${interaction.ppiDruggability.drugChemicalScore}	</td>
    	<td>${interaction.ppiDruggability.functionalScore}	</td>
    	<td>${interaction.ppiDruggability.allScore}	</td>
    	<td><a href="http://www.drpias.net/view_entry.php?entrezgene_id=${interaction.gene1.primaryIdentifier}&entrezgene_id_2=${interaction.gene2.primaryIdentifier}" target="_blank">External link (Dr.PIAS)</a></td>
    </tr>
    </tbody>
    </table>
  </c:otherwise>
</c:choose>
