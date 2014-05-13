<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<div class="collection-table">

<c:set var="assay" value="${reportObject.object}"/>

<h3>Source database identifier</h3>


    <table>
    <thead>
    <tr>
    	<th>Identifier</th>
    	<th>Examine the original source page:</th>
    </tr>
    </thead>
     <tbody>
    <tr>
    	<td>${assay.identifier}</td>
  		<td>
    	<c:choose>
  			<c:when test="${assay.source == 'ChEMBL'}">
  					ChEMBL Assay report: <a href="https://www.ebi.ac.uk/chembl/assay/inspect/${assay.identifier}" target="_blank">${assay.identifier}</a> 
			</c:when>
			<c:otherwise>
  					PubChen BioAssay: <a href="http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid=${assay.identifier}" target="_blank">AID ${assay.identifier}</a> 
  			</c:otherwise>		
			</c:choose>
  			<img src="/targetmine/model/images/extlink.gif" width="11" height="9">
  		</td>
    </tr>
    </tbody>
    </table>
