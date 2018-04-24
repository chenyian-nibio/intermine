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
			var annot_class = { "Amadeus": ["-","-","-","-","-","-","-","-","V","-","-","-"],
								"Barcode 3.0": ["V","-","-","-","-","-","-","-","-","-","-","-"],
								"BioAssay": ["-","-","-","V","-","-","-","-","-","-","-","V"],
								"BioGRID": ["-","-","-","-","-","-","-","V","-","-","-","-"],
								"CATH": ["-","-","V","-","-","-","-","-","-","-","-","-"],
								"ChEBI": ["-","-","-","V","-","-","-","-","-","-","-","-"],
								"ChEMBL": ["-","-","-","V","-","-","-","-","-","-","-","V"],
								"ClinVar": ["-","-","-","-","-","-","-","-","-","V","-","-"],
								"DrugBank": ["-","-","-","V","-","-","-","-","-","-","-","V"],
								"DrugEBIlity": ["-","V","-","-","-","-","-","-","-","-","-","-"],
								"ENCODE ChIP-seq data": ["-","-","-","-","-","-","-","-","V","-","-","-"],
								"Entrez Gene": ["V","-","-","-","-","-","-","-","-","-","-","-"],
								"ENZYME": ["-","V","-","-","-","-","-","-","-","-","-","-"],
								"Gene3D": ["-","-","V","-","-","-","-","-","-","-","-","-"],
								"HTRI Database": ["-","-","-","-","-","-","-","-","V","-","-","-"],
								"InterPro": ["-","-","-","-","V","-","-","-","-","-","-","-"],
								"iRefIndex": ["-","-","-","-","-","-","-","V","-","-","-","-"],
								"KEGG Orthology": ["V","-","-","-","-","-","-","-","-","-","-","-"],
								"KEGG Pathway": ["-","-","-","-","-","-","V","-","-","-","-","-"],
								"Ligand Expo": ["-","-","-","V","-","-","-","-","-","-","-","V"],
								"miRBase": ["V","-","-","-","-","-","-","-","-","-","-","-"],
								"miRTarBase": ["-","-","-","-","-","-","-","-","-","-","V","-"],
								"NCI Pathway Interaction Database": ["-","-","-","-","-","-","V","-","-","-","-","-"],
								"NetAffx Annotation Files": ["V","-","-","-","-","-","-","-","-","-","-","-"],
								"GWAS Catalog": ["-","-","-","-","-","-","-","-","-","V","-","-"],
								"OMIM": ["-","-","-","-","-","-","-","-","-","V","-","-"],
								"ORegAnno": ["-","-","-","-","-","-","-","-","V","-","-","-"],
								"Reactome": ["-","-","-","-","-","-","V","-","-","-","-","-"],
								"SCOP": ["-","-","V","-","-","-","-","-","-","-","-","-"],
								"SIFTS": ["-","-","V","-","-","-","-","-","-","-","-","-"],
								"Swiss-Prot": ["-","V","-","-","-","-","-","-","-","-","-","-"],
								"TrEMBL": ["-","V","-","-","-","-","-","-","-","-","-","-"],
								"UniProt-GOA": ["-","-","-","-","-","V","-","-","-","-","-","-"],
								"wwPDB": ["-","-","V","-","-","-","-","-","-","-","-","-"],
								"KEGG Drug": ["-","-","-","V","-","-","-","-","-","-","-","-"]
							}

			var service_url = "";
			$.ajax({
				url: service_url + "service/query/results?query=%3Cquery+name%3D%22%22+model%3D%22genomic%22+view%3D%22DataSet.code+DataSet.name+DataSet.version+DataSet.dateType+DataSet.date+DataSet.id+DataSet.description%22+longDescription%3D%22%22+sortOrder%3D%22DataSet.name+asc%22%3E%3C%2Fquery%3E&format=json",
				dataType: "json",
				success: function(result) {
					var content = "";
					for (var i=0; i<result.results.length; i++) {
						var code = result.results[i][0];
						var name = result.results[i][1];
						name = name.replace(/ data set/,"");
						var dataset_id = result.results[i][5];
						var version = result.results[i][2];
						if (version != null) {
							if (code=='GOAN') {
								version = version.replace(/,/g,"<br/>");
							} 
						} else {
							version = "-";
						}
						var date = result.results[i][4];
						if (date != null) {
							// SCOP contains illegal date
							if (code != 'SCOP') {
								var d = new Date(date);
								date = d.getFullYear() + "/" + (d.getMonth() + 1) + "/" + d.getDate();
							}
						} else {
							date = "-";
						}

						content += ('<tr><td title="' + result.results[i][6] + '">' + 
							'<a href="' + service_url + 'report.do?id=' + dataset_id + '" target="_blank">' + name + '</a>' 
							+ "</td><td>" + version + '</td><td title="' + result.results[i][3] + '">' + date + "</td>");
							
						
						var ticks = "";
						if (annot_class[name] == null) {
							for (var k=0; k<12; k++) {
								ticks += '<td class="none">-</td>';
							}
						} else {
							for (var k=0; k<12; k++) {
								if (annot_class[name][k] == "V") {
									ticks += '<td class="tick"><img src="model/images/accept.png" width="16" height="16" alt="V">'
								} else {
									ticks += ('<td class="none">-</td>');
								}
							}
						}
						content += ticks;
						
						content += '</tr>';
					}
					$('#tablecontent').html(content);
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
			<dd><p>This page lists all data sets loaded along with the date the data was released or downloaded. Check <a href="http://targetmine.mizuguchilab.org/documentation/list-of-data-sources" target="_blank">here</a> for other details.</p></dd>
		</dl>
	</div>

	<table cellpadding="0" cellspacing="1px" border="0" id="dataset" class="display">
		<thead>
			<tr>
				<th rowspan="2" style="width: 240px;">Data Set</th>
				<th rowspan="2" style="width: 120px;">Version</th>
				<th rowspan="2" style="width: 120px;">Date</th>
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
		<tbody id="tablecontent">
		</tbody>
	</table>


</div>
<!-- /dataCategories -->
