<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="chemblCompound" value="${reportObject.object}"/>

<c:choose>
	<c:when test="${empty chemblCompound.alternateForms}">
	<!-- show nothing-->
	</c:when>
	<c:otherwise>
	<h3>Parent Compound in ChEMBL</h3>
		<c:choose>
			<c:when test="${empty chemblCompound.parent}">
				<div id="parentimage">
					<img src="https://www.ebi.ac.uk/chembl/api/data/image/${chemblCompound.inchiKey}?dimensions=200" onerror="document.getElementById('parentimage').innerHTML = 'Not available.'"/>
				</div>
				<div>
		    		<a href="report.do?id=${chemblCompound.id}">${chemblCompound.originalId}</a><br/>
		    		${chemblCompound.name}
	    		</div>
			</c:when>
			<c:otherwise>
				<div id="parentimage">
					<img src="https://www.ebi.ac.uk/chembl/api/data/image/${chemblCompound.parent.inchiKey}?dimensions=200" onerror="document.getElementById('parentimage').innerHTML = 'Not available.'"/>
				</div>
				<div>
		    		<a href="report.do?id=${chemblCompound.parent.id}">${chemblCompound.parent.originalId}</a><br/>
		    		${chemblCompound.parent.name}
	    		</div>
			</c:otherwise>
		</c:choose>
	<br/>
	<h3>Alternate Forms of Compound in ChEMBL</h3>
		<table>
	    <tbody>
    	<tr>
			<c:forEach var="altCompound" items="${chemblCompound.alternateForms}" varStatus="status">
	    		<td>
					<div id="image${status.count}">
	    				<img src="https://www.ebi.ac.uk/chembl/api/data/image/${altCompound.inchiKey}?dimensions=200" onerror="document.getElementById('image${status.count}').innerHTML = 'Not available.'"/>
					</div>
					<div>
		    			<a href="report.do?id=${altCompound.id}">${altCompound.originalId}</a><br/>
		    			${altCompound.name}
		    		</div>
	    		</td>
			</c:forEach>
	    </tr>
	    </tbody>
	    </table>
	</c:otherwise>
</c:choose>

</div>