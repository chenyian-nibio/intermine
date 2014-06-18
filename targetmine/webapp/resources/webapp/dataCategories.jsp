<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>

<!-- dataCategories -->
<html:xhtml/>

<div class="body">

		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
		<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
		<!-- DataTables -->
		<script src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
		
        <script type="text/javascript">
		
		$(document).ready(function(){
			$('#container').css("visibility", "hidden");
			var service_url = "";
			$.ajax({
				url: "service/query/results?query=%3Cquery+name%3D%22%22+model%3D%22genomic%22+view%3D%22DataSet.code+DataSet.name+DataSet.version+DataSet.dateType+DataSet.date+DataSet.id+DataSet.description%22+longDescription%3D%22%22+sortOrder%3D%22DataSet.name+asc%22%3E%3C%2Fquery%3E&format=json",
				dataType: "json",
				success: function(result) {
					for (var i=0; i<result.results.length; i++) {
						var code = result.results[i][0];
						var name = result.results[i][1];
						var dataset_id = result.results[i][5];
						
						name = name.replace(/ data set/,"");
						$("#name_"+code).html('<a href="report.do?id=' + dataset_id + '" target="_blank">' + name + '</a>');
						$("#name_"+code).attr("title",result.results[i][6]);
						
						var version = result.results[i][2];
						if (version != null) {
							if (code=='GOAN') {
								version = version.replace(/,/g,"<br/>");
							} 
							$("#ver_"+code).html(version);
						} else {
							$("#ver_"+code).html("-");
						}
						
						var date = result.results[i][4];
						if (date != null) {
						// SCOP contains illegal date
							if (code=='SCOP') {
								$("#date_"+code).html("2009/6/-");
							} else {
								var d = new Date(date);
								var date_string = d.getFullYear() + "/" + (d.getMonth() + 1) + "/" + d.getDate();
								$("#date_"+code).html(date_string);
							}
							$("#date_"+code).attr("title",result.results[i][3])
						} else {
							$("#date_"+code).html("-");
						}
					}
					$('#dataset').dataTable({
						"iDisplayLength": 25,
						"bAutoWidth": false,
						"sScrollX": "100%",
						"sScrollXInner": "2200px",
						"bScrollCollapse": true
					} );
					$('#container').css("visibility", "visible");
				},
				error: function() {
					$("#dataset").html("Error retrieving data from TargetMine");
				}
			})
		});
		</script>
        <style>
			body    { font-family: 'Lucida Grande', Verdana, Geneva, Lucida, Helvetica, Arial, sans-serif; font-size: 12px;}
			td.tick { font-weight: bold; color: red; text-align: center;}
			td.none { font-weight: none; color: green; text-align: center;}
			table.dataTable thead th { text-align: center; color: black; background-color: #EBEBEB; }
			table.dataTable tr.odd  { background-color: #DADADA; }
			table.dataTable tr.even { background-color: #F9F9F9; }
			table.dataTable tr.odd td.sorting_1 { background-color: #C1C1C1;}
			table.dataTable tr.even td.sorting_1 { background-color: #E1E1E1;}
			table.dataTable a { color: #049; }
		</style>

	 <div class="plainbox" style="" >
		<dl>
			<dt>
	        	<h1 id="">Integrated data in TargetMine</h1>
	        </dt>
			<dd><p>This page lists all data sources loaded along with the date the data was released or downloaded.</p></dd>
		</dl>
	</div>

	<table cellpadding="0" cellspacing="1px" border="0" id="dataset" class="display">
		<thead>
			<tr>
				<th rowspan="2" style="width: 220px;">Data Source</th>
				<th rowspan="2" style="width: 100px;">Version</th>
				<th rowspan="2" style="width: 100px;">Date</th>
				<th colspan="12" style="width: 1680px;">Biological annotations</th>
			</tr>
			<tr>
				<th>Gene</th>
				<th>Protein</th>
				<th>Protein <br />structure</th>
				<th>Chemical <br />compound</th>
				<th>Protein <br />domains</th>
				<th>Gene <br />function</th>
				<th>Pathways</th>
				<th>Protein-<br />protein <br />interactions</th>
				<th>TF-target <br />interactions</th>
				<th>Disease-<br />gene <br />associations</th>
				<th>miRNA-<br />target <br />associations</th>
				<th>Compound-<br />target <br />associations</th>
			</tr>
		</thead>
		<tbody>
			<tr><td id="name_AMAD">Amadeus</td><td id="ver_AMAD">-</td><td id="date_AMAD">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_BGRD">BioGRID interaction data set</td><td id="ver_BGRD">-</td><td id="date_BGRD">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_CATH">CATH</td><td id="ver_CATH">-</td><td id="date_CATH">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_CHBI">ChEBI</td><td id="ver_CHBI">-</td><td id="date_CHBI">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_CMBL">ChEMBL</td><td id="ver_CMBL">-</td><td id="date_CMBL">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td></tr>
			<tr><td id="name_DOAN">DO Annotation</td><td id="ver_DOAN">-</td><td id="date_DOAN">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_DGBK">DrugBank</td><td id="ver_DGBK">-</td><td id="date_DGBK">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td></tr>
			<tr><td id="name_DRGB">DrugEBIlity</td><td id="ver_DRGB">-</td><td id="date_DRGB">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_ENZY">ENZYME</td><td id="ver_GENE">-</td><td id="date_GENE">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GENE">Entrez Gene</td><td id="ver_ENZY">-</td><td id="date_ENZY">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GN3D">Gene3D</td><td id="ver_GN3D">-</td><td id="date_GN3D">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GWAS">Genome-Wide Association Studies</td><td id="ver_GWAS">-</td><td id="date_GWAS">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_INTP">InterPro data set</td><td id="ver_INTP">-</td><td id="date_INTP">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_KORT">KEGG orthologues data set</td><td id="ver_KORT">-</td><td id="date_KORT">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_KEGG">KEGG pathways data set</td><td id="ver_KEGG">-</td><td id="date_KEGG">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_LGEX">Ligand Expo</td><td id="ver_LGEX">-</td><td id="date_LGEX">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td></tr>
			<tr><td id="name_NCIP">NCI Pathway Interaction Database</td><td id="ver_NCIP">-</td><td id="date_NCIP">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_AFFY">NetAffx Annotation Files</td><td id="ver_AFFY">-</td><td id="date_AFFY">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_OREG">ORegAnno</td><td id="ver_OREG">-</td><td id="date_OREG">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_PPIV">PPI view</td><td id="ver_PPIV">-</td><td id="date_PPIV">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_PBCM">PubChem</td><td id="ver_PBCM">-</td><td id="date_PBCM">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_REAC">Reactome data set</td><td id="ver_REAC">-</td><td id="date_REAC">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_SCOP">SCOP</td><td id="ver_SCOP">-</td><td id="date_SCOP">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_SIFT">SIFTS</td><td id="ver_SIFT">-</td><td id="date_SIFT">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_STCH">STITCH</td><td id="ver_STCH">-</td><td id="date_STCH">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td></tr>
			<tr><td id="name_SWPR">Swiss-Prot data set</td><td id="ver_SWPR">-</td><td id="date_SWPR">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_TRMB">TrEMBL data set</td><td id="ver_TRMB">-</td><td id="date_TRMB">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_GOAN">UniProt-GOA</td><td id="ver_GOAN">-</td><td id="date_GOAN">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_IREF">iRefIndex interaction data set</td><td id="ver_IREF">-</td><td id="date_IREF">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_MIRB">miRBase</td><td id="ver_MIRB">-</td><td id="date_MIRB">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_MITB">miRTarBase</td><td id="ver_MITB">-</td><td id="date_MITB">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td></tr>
			<tr><td id="name_WPDB">wwPDB</td><td id="ver_WPDB">-</td><td id="date_WPDB">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_PIAS">DR. PIAS</td><td id="ver_PIAS">-</td><td id="date_PIAS">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<tr><td id="name_PCBA">BioAssay</td><td id="ver_PCBA">-</td><td id="date_PCBA">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td></tr>
			<tr><td id="name_HTRI">HTRI DB</td><td id="ver_HTRI">-</td><td id="date_HTRI">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td><td class="none">-</td></tr>
			<!-- to be removed -->
			<tr><td id="name_OMIM">OMIM</td><td id="ver_OMIM">-</td><td id="date_OMIM">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="none">-</td><td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V"></td><td class="none">-</td><td class="none">-</td></tr>
		</tbody>
	</table>


</div>
<!-- /dataCategories -->
