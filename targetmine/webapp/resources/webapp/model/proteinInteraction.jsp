<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im" %>

<TABLE width="100%">
  <TR>
    <TD valign="top">
      <div class="heading2">
        Current data
      </div>
      <div class="body">
        <P><B>Protein-protein interactions from PPI view</B></P>
        <h4>
          <a href="javascript:toggleDiv('hiddenDiv1');">
            <img id='hiddenDiv1Toggle' src="images/disclosed.gif"/>
          PPI view collected PPI data from the following PPI databases...
          </a>
        </h4>
        <div id="hiddenDiv1" class="dataSetDescription">
          <TABLE border="0">
            <tr><td rowspan=7>&nbsp; </td></tr>

            <tr>
              <td><b>BIND</b></td>
              <td><a href="http://www.bind.ca/Action">http://www.bind.ca/Action</a></td>
            </tr>
            <tr>
              <td><b>DIP</b></td>
              <td><a href="http://dip.doe-mbi.ucla.edu">http://dip.doe-mbi.ucla.edu</a></td>

            </tr>
            <tr>
	      <td><b>MINT</b></td>
              <td><a href="http://mint.bio.uniroma2.it/mint/Welcome.do">http://mint.bio.uniroma2.it/mint/Welcome.do</a></td>
            </tr>
            <tr>
	      <td><b>HPRD</b></td>

              <td><a href="http://www.hprd.org">http://www.hprd.org</a></td>
            </tr>
            <tr>
	      <td><b>IntAct</b></td>
              <td><a href="http://www.ebi.ac.uk/intact/index.jsp">http://www.ebi.ac.uk/intact/index.jsp</a></td>
            </tr>
            <tr>

	      <td><b>GNP_Y2H</b></td>
              <td><a href="http://genomenetwork.nig.ac.jp/public/sys/gnppub/Top.do">http://genomenetwork.nig.ac.jp/public/sys/gnppub/Top.do</a></td>
            </tr>
          </table>
        </div>
        
        <P><B>Genetic interactions from the BioGRID</B></P>
        <P><B>Transcription factor data from AMADEUS and ORegAnno</B></P>
        <P><B>Protein-protein interactions from the iRefIndex and PPIView</B></P>

      </div>
    </TD>
  </TR>
</TABLE>
