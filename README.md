#TargetMine
TargetMine is an integrated data warehouse system for target discovery based on the InterMine framework. See below for more details.

Web site:
http://targetmine.mizuguchilab.org/

Reference:
Chen Y-A, Tripathi LP, Mizuguchi K (2016) An integrative data analysis platform for gene set analysis and knowledge discovery in a data warehouse framework. Database (Oxford). 2016 Mar 17;2016. pii: baw009. <br/>
doi: 10.1093/database/baw009

Chen Y-A, Tripathi LP, Mizuguchi K (2011) TargetMine, an Integrated Data Warehouse for Candidate Gene Prioritisation and Target Discovery. PLoS ONE 6(3): e17844. <br/>
doi:10.1371/journal.pone.0017844


#Installation
See the [INSTALL](INSTALL) file for installation instructions.

#TargetMine Source Distribution
Copyright (C) 2011-2016 by The Mizuguchi Laboratory.

TargetMine requires the InterMine library. 
A modified version of InterMine is included in this distribution. (See 'Customized InterMine Library' below for details. The three directories, bio, imbuild and intermine, contain the InterMine library.)

#InterMine

A powerful open source data warehouse system.

[InterMine Documentation](http://intermine.readthedocs.org/en/latest/)

In order to improve the chance of continued funding for the InterMine project it would be appreciated if groups that use InterMine or parts of InterMine would let us know (email [info[at]flymine.org](mailto:info flymine.org)).

Copyright (C) 2002-2016 FlyMine

This product includes software developed by the [Apache Software Foundation](http://www.apache.org/).

#Licenses
TargetMine is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.

See [LICENSE](LICENSE) for licensing information.

InterMine is an open source project distributed under the GNU Lesser General Public Licence.

See [LICENSE.intermine](LICENSE.intermine) and [LICENSE.intermine.LIBS](LICENSE.intermine.LIBS) for licensing information.

#Customized InterMine Library
The following files were modified.

* bio/webapp/resources/webapp/WEB-INF/classDescriptions.properties
* bio/postprocess/main/src/org/intermine/bio/postprocess/PostProcessOperationsTask.java
* bio/webapp/src/org/intermine/bio/web/logic/OrthologueConverter.java
* intermine/web/main/src/org/intermine/web/struts/BuildBagAction.java
* intermine/web/main/src/org/intermine/web/struts/BuildBagForm.java

The following files were added.

* bio/postprocess/main/src/org/intermine/bio/postprocess/AssociateGeneAndIPC.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/CalculateBioThemeBackground.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/CoExpressionInteraction.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/HierarchicalClustering.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/IntegratedPathwayClustering.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/NetworkAnalysisTool.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/PpiDruggability.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/RemoveCompoundCasRegistryNumber.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/TranscribeDrugBankTargets.java
* bio/postprocess/main/src/org/intermine/bio/postprocess/TranscribeNcbiGeneId.java
* bio/webapp/src/org/intermine/bio/web/logic/ProteinOrthologConverter.java
