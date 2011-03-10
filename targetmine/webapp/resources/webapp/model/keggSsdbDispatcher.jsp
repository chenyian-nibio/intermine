<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


  <c:if test="${!empty interMineObject.keggId}">
  <p>
  <form name="ortholog" method="post" action="http://ssdb.genome.jp/ssdb-bin/ssdb_best_best">
<blockquote>
  <input type="hidden" name="org_gene" size="20" value="${interMineObject.keggId}" />
  with
  <select name="res_disp">
    <option value="http://ssdb.genome.jp/ssdb-bin/ssdb_best_best" selected="selected">best-best</option>
    <option value="http://ssdb.genome.jp/ssdb-bin/ssdb_best">forward best</option>
    <option value="http://ssdb.genome.jp/ssdb-bin/ssdb_rev_best">reverse best</option>
  </select>

  and above
  <select name="threshold">
    <option value="100" selected="selected">100</option>
    <option value="150">150</option>
    <option value="200">200</option>
    <option value="400">400</option>
  </select>

  <input type="submit" value="Go" onclick="return submit_chk(this.form);" />
  <input type="reset" value="Clear" />
  <br />
  <input type="radio" name="target_taxonomy" value="all" checked="checked" />All organisms
  <input type="radio" name="target_taxonomy" value="selected" />Selected organism group
  <select name="taxonomy">
    <option value="eukaryotes">Eukaryotes</option>
    <option value="prokaryotes">Prokaryotes</option>
    <option value="bacteria">Bacteria</option>

    <option value="archaea">Archaea</option>
    <option value="cyanobacteria">Cyanobacteria</option>
  </select>
</blockquote>
</form>
  
  </p>
  </c:if>

