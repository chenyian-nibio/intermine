<!-- inparanoid.jsp -->
<%@ taglib tagdir="/WEB-INF/tags" prefix="im" %>

<TABLE width="100%">
  <tr>
    <td valign="top">
      <div class="heading2">
        Current data
      </div>
      <div class="body">

 <h4>
  <a href="javascript:toggleDiv('hiddenDiv1');">
    <img id='hiddenDiv1Toggle' src="images/disclosed.gif"/>
      Homology data from KEGG Orthology ...
  </a>
 </h4>

<div id="hiddenDiv1" class="dataSetDescription">


        <p>Homologue (including Orthologue and Paralogue) relationships come from <A href="http://www.genome.jp/kegg/ko.html" target="_new">KEGG Orthology</A> between the following organisms:</p>
        <ul>
          <li><I>H. sapiens</I></li>
          <li><I>R. norvegicus</I></li>
          <li><I>M. musculus</I></li>
          <li><I>D. melanogaster</I></li>
          <li><I>E. coli</I></li>
        </ul>

</div>

</td>



    <td width="40%" valign="top">
      <div class="heading2">
       Bulk download
      </div>
      <div class="body">
        <ul>
          <li>
			<div style="font-weight: bold; color: red;">Users of this product/service may not download large quantities of KEGG Data.</div>
          </li>
        </ul>
      </div>
    </td>
  </tr>
</TABLE>

<!-- /inparanoid.jsp -->
