TargetMine
============

[![intermine](http://img.shields.io/badge/InterMine-1.8.5-green.svg?style=flat)](https://github.com/intermine/intermine/tree/intermine-1.8.5)

TargetMine is an integrated data warehouse system for target discovery which is based on the InterMine framework with a few customized features.

Web site: [https://targetmine.mizuguchilab.org/](https://targetmine.mizuguchilab.org/)

**TargetMine source code in this repository is a "Pre-InterMine 2.0" version.**

For the newer version, please refer to [https://github.com/chenyian-nibio/targetmine-gradle](https://github.com/chenyian-nibio/targetmine-gradle).

*Please notice that the future developement of TargetMine will be found in the new depository only.*

InterMine
------------------------

A powerful open source data warehouse system.

Copyright (C) 2002-2019 FlyMine

For other information, please refer to [https://github.com/intermine/intermine](https://github.com/intermine/intermine).

TargetMine Source Distribution
------------------------

Copyright (C) 2011-2019 by The Mizuguchi Laboratory.

TargetMine requires the InterMine library. A modified version of InterMine is included in this distribution.

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
 * bio/postprocess/main/src/org/intermine/bio/postprocess/RemoveCompoundCasRegistryNumber.java
 * bio/postprocess/main/src/org/intermine/bio/postprocess/TranscribeDrugBankTargets.java
 * bio/postprocess/main/src/org/intermine/bio/postprocess/TranscribeNcbiGeneId.java
 * bio/webapp/src/org/intermine/bio/web/logic/ProteinOrthologConverter.java

License
------------------------

TargetMine is licensed under the MIT License.

See [LICENSE](LICENSE.md) for licensing information.

InterMine is an open source project distributed under the GNU Lesser General Public Licence.

See [LICENSE.intermine](LICENSE.intermine) and [LICENSE.intermine.LIBS](LICENSE.intermine.LIBS) for licensing information.

This product includes software developed by the [Apache Software Foundation][apache].

Please cite
------------------------

**An integrative data analysis platform for gene set analysis and knowledge discovery in a data warehouse framework.**  
*Chen Y-A, Tripathi LP, Mizuguchi K.*  
[Database (Oxford). 2016 Mar 17;2016. pii: baw009](https://academic.oup.com/database/article/doi/10.1093/database/baw009/2630159)  
[![doi](http://img.shields.io/badge/DOI-10.1093%2Fdatabase%2Fbaw009-blue.svg?style=flat)](https://academic.oup.com/database/article/doi/10.1093/database/baw009/2630159) 
[![pubmed](http://img.shields.io/badge/PMID-26989145-blue.svg?style=flat)](https://www.ncbi.nlm.nih.gov/pubmed/26989145)

**TargetMine, an Integrated Data Warehouse for Candidate Gene Prioritisation and Target Discovery.**  
*Chen Y-A, Tripathi LP, Mizuguchi K.*  
[PLoS ONE. 2011 Mar 8;6(3): e17844](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0017844)  
[![doi](http://img.shields.io/badge/DOI-10.1371%2Fjournal%2Epone%2E0017844-blue.svg?style=flat)](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0017844) 
[![pubmed](http://img.shields.io/badge/PMID-21408081-blue.svg?style=flat)](https://www.ncbi.nlm.nih.gov/pubmed/21408081)


[apache]: http://www.apache.org
