package org.modmine.web;

/*
 * Copyright (C) 2002-2010 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.intermine.bio.constants.ModMineCacheKeys;
import org.intermine.bio.util.BioConverterUtil;
import org.intermine.model.bio.DatabaseRecord;
import org.intermine.model.bio.Experiment;
import org.intermine.model.bio.Project;
import org.intermine.model.bio.ResultFile;
import org.intermine.model.bio.Submission;
import org.intermine.modelproduction.MetadataManager;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.sql.Database;
import org.intermine.util.PropertiesUtil;
import org.modmine.web.GBrowseParser.GBrowseTrack;


/**
 * Read modENCODE metadata into objects that simplify display code, cache results.
 *
 * @author Richard Smith
 */
public final class MetadataCache
{
    private static final Logger LOG = Logger.getLogger(MetadataCache.class);
    
    private static Map<String, DisplayExperiment> experimentCache = null;
    private static Map<Integer, Map<String, Long>> submissionFeatureCounts = null;
    private static Map<String, Map<String, Long>> experimentFeatureCounts = null;
    private static Map<String, Map<String, Long>> experimentUniqueFeatureCounts = null;
    private static Map<Integer, Map<String, Long>> submissionFeatureExpressionLevelCounts = null;
    private static Map<String, Map<String, Long>> experimentFeatureExpressionLevelCounts = null;
    private static Map<Integer, Integer> submissionExpressionLevelCounts = null;
    private static Map<Integer, Integer> submissionIdCache = null;
    private static Map<Integer, List<GBrowseTrack>> submissionTracksCache = null;
    
    private static Map<Integer, Set<ResultFile>> submissionFilesCache = null;
    private static Map<Integer, Integer> filesPerSubmissionCache = null;
    private static Map<Integer, List<String>> submissionLocatedFeatureTypes = null;
    private static Map<Integer, List<String>> submissionUnlocatedFeatureTypes = null;
    private static Map<Integer, List<String[]>> submissionRepositedCache = null;
    private static Map<String, String> featDescriptionCache = null;
    private static Map<String, List<DisplayExperiment>> projectExperiments = null;
    private static Map<String, List<DisplayExperiment>> categoryExperiments = null;
    
    private static Properties metadataProperties = null;
    
    private static long lastTrackCacheRefresh = 0;
    private static final long TWO_HOUR = 7200000;
    
    private MetadataCache() {
    }
    
    // TODO check for duplication of queries
    
    /**
     * Fetch experiment details for display.
     * @param os the production objectStore
     * @return a list of experiments
     */
    public static synchronized List<DisplayExperiment> getExperiments(ObjectStore os) {
        if (experimentCache == null) {
            readExperiments(os);
        }
        return new ArrayList<DisplayExperiment>(experimentCache.values());
    }
    
    /**
     * Fetch experiment details for display.
     * @param os the production objectStore
     * @return a list of experiments
     */
    public static synchronized Map<String, List<DisplayExperiment>>
    getProjectExperiments(ObjectStore os) {
        if (projectExperiments == null) {
            readProjectExperiments(os);
        }
        return projectExperiments;
    }
    
    /**
     * Fetch experiment details for display.
     * @param os the production objectStore
     * @return a list of experiments
     */
    public static synchronized Map<String, List<DisplayExperiment>>
    getCategoryExperiments(ObjectStore os) {
        if (categoryExperiments == null) {
            readCategoryExperiments(os);
        }
        return categoryExperiments;
    }
    
    /**
     * Fetch the metadata properties from database.
     * @param os the production objectStore
     * @return the metadata properties
     * @throws SQLException exception
     * @throws IOException exception
     */
    public static synchronized Properties getProperties(ObjectStore os)
    throws SQLException, IOException {
        if (metadataProperties == null) {
            readProperties(os);
        }
        return metadataProperties;
    }
    
    
    /**
     * Fetch GBrowse tracks per submission for display. This updates automatically from the GBrowse
     * server and refreshes periodically (according to threshold).  When refreshing another process
     * is spawned which will update tracks when finished, if GBrowse can't be accessed the current
     * list of tracks of tracks are preserved.
     * @return map from submission id to list of GBrowse tracks
     */
    public static synchronized Map<Integer, List<GBrowseTrack>> getGBrowseTracks() {
        fetchGBrowseTracks();
        while (submissionTracksCache == null) {
            try {
                MetadataCache.class.wait();
            } catch (InterruptedException e) {
            }
        }
        return submissionTracksCache;
    }
    
    
    /**
     * Fetch unlocated feature types per submission.
     * @param os the production objectStore
     * @return map of unlocated feature types
     */
    public static synchronized Map<Integer, List<String>> getLocatedFeatureTypes(ObjectStore os) {
        if (submissionLocatedFeatureTypes == null) {
            readSubmissionLocatedFeature(os);
        }
        return submissionLocatedFeatureTypes;
    }
    
    /**
     * Fetch unlocated feature types per submission.
     * @param os the production objectStore
     * @return map of unlocated feature types
     */
    public static synchronized Map<Integer, List<String>> getUnlocatedFeatureTypes(ObjectStore os) {
        if (submissionUnlocatedFeatureTypes == null) {
            readUnlocatedFeatureTypes(os);
        }
        return submissionUnlocatedFeatureTypes;
    }
    
    /**
     * Fetch unlocated feature types per submission.
     * @param os the production objectStore
     * @param dccId ID from DCC
     * @return map of unlocated feature types
     */
    public static synchronized
    Set<String> getUnlocatedFeatureTypesBySubId(ObjectStore os, Integer dccId) {
        if (submissionUnlocatedFeatureTypes == null) {
            readUnlocatedFeatureTypes(os);
        }
        Set<String> uf = new HashSet<String>(submissionUnlocatedFeatureTypes.get(dccId));
        return uf;
    }
    
    /**
     * Fetch the collection of ResultFiles per submission.
     * @param os the production objectStore
     * @return map
     */
    public static synchronized Map<Integer, Set<ResultFile>> getSubmissionFiles(ObjectStore os) {
        if (submissionFilesCache == null) {
            readSubmissionFiles(os);
        }
        return submissionFilesCache;
    }
    
    /**
     * Fetch the collection of Expression Level Counts per submission.
     * @param os the production objectStore
     * @return map
     */
    public static synchronized Map<Integer, Integer>
    getSubmissionExpressionLevelCounts(ObjectStore os) {
        if (submissionExpressionLevelCounts == null) {
            readSubmissionExpressionLevelCounts(os);
        }
        return submissionExpressionLevelCounts;
    }
    
    /**
     * Fetch the collection of Expression Level Counts per submission.
     * @param os the production objectStore
     * @return map
     */
    public static synchronized Map<Integer, Map<String, Long>>
    getSubmissionFeatureExpressionLevelCounts(ObjectStore os) {
        if (submissionFeatureExpressionLevelCounts == null) {
            readSubmissionFeatureExpressionLevelCounts(os);
        }
        return submissionFeatureExpressionLevelCounts;
    }
    
    /**
     * Fetch the collection of Expression Level Counts per submission.
     * @param os the production objectStore
     * @return map
     */
    public static synchronized Map<String, Map<String, Long>>
    getExperimentFeatureExpressionLevelCounts(ObjectStore os) {
        if (experimentFeatureExpressionLevelCounts == null) {
            readExperimentFeatureExpressionLevelCounts(os);
        }
        return experimentFeatureExpressionLevelCounts;
    }
    
    /**
     * Fetch number of input/output file per submission.
     * @param os the production objectStore
     * @return map
     */
    public static synchronized Map<Integer, Integer> getFilesPerSubmission(ObjectStore os) {
        if (submissionFilesCache == null) {
            readSubmissionFiles(os);
        }
        filesPerSubmissionCache = new HashMap<Integer, Integer>();
        
        Iterator<Integer> dccId = submissionFilesCache.keySet().iterator();
        while (dccId.hasNext()) {
            Integer thisSub = dccId.next();
            Integer nrFiles = submissionFilesCache.get(thisSub).size();
            filesPerSubmissionCache.put(thisSub, nrFiles);
        }
        return filesPerSubmissionCache;
    }
    
    
    /**
     * Fetch a list of file names for a given submission.
     * @param os the objectStore
     * @param dccId the modENCODE submission id
     * @return a list of file names
     */
    public static synchronized List<ResultFile> getFilesByDccId(ObjectStore os,
            Integer dccId) {
        if (submissionFilesCache == null) {
            readSubmissionFiles(os);
        }
        return new ArrayList<ResultFile>(submissionFilesCache.get(dccId));
    }
    
    /**
     * Fetch a list of GBrowse tracks for a given submission.
     * @param dccId the modENCODE submission id
     * @return a list of file names
     */
    public static synchronized List<GBrowseTrack> getTracksByDccId(Integer dccId) {
        Map<Integer, List<GBrowseTrack>> tracks = getGBrowseTracks();
        if (tracks.get(dccId) != null) {
            return new ArrayList<GBrowseTrack>(tracks.get(dccId));
        } else {
            return new ArrayList<GBrowseTrack>();
        }
    }
    
    /**
     * Fetch a list of file names for a given submission.
     * @param servletContext servletContext
     * @return a list of file names
     */
    public static synchronized
    Map<String, String> getFeatTypeDescription(ServletContext servletContext) {
        if (featDescriptionCache == null) {
            readFeatTypeDescription(servletContext);
        }
        return featDescriptionCache;
    }
    
    /**
     * Fetch a map from feature type to count for a given submission.
     * @param os the objectStore
     * @param dccId the modENCODE submission id
     * @return a map from feature type to count
     */
    public static synchronized Map<String, Long> getSubmissionFeatureCounts(ObjectStore os,
            Integer dccId) {
        if (submissionFeatureCounts == null) {
            readSubmissionFeatureCounts(os);
        }
        return submissionFeatureCounts.get(dccId);
    }
    
    /**
     * Fetch a submission by the modENCODE submission ids
     * @param os the objectStore
     * @param dccId the modENCODE submission id
     * @return the requested submission
     * @throws ObjectStoreException if error reading database
     */
    public static synchronized Submission getSubmissionByDccId(ObjectStore os, Integer dccId)
    throws ObjectStoreException {
        if (submissionIdCache == null) {
            readSubmissionIds(os);
        }
        return (Submission) os.getObjectById(submissionIdCache.get(dccId));
    }
    
    /**
     * Get experiment information by name
     * @param os the objectStore
     * @param name of the experiment to fetch
     * @return details of the experiment
     * @throws ObjectStoreException if error reading database
     */
    public static synchronized DisplayExperiment getExperimentByName(ObjectStore os, String name)
    throws ObjectStoreException {
        if (experimentCache == null) {
            readExperiments(os);
        }
        return experimentCache.get(name);
    }
    
    /**
     *
     * @param os objectStore
     * @return map experiment expression levels
     */
    public static Map<String, Integer> getExperimentExpressionLevels(ObjectStore os) {
        Map<String, Integer> experimentELevel = new HashMap<String, Integer>();
        
        Map<Integer, Integer> subELevelMap = getSubmissionExpressionLevelCounts(os);
        
        for (DisplayExperiment exp : getExperiments(os)) {
            Integer expCount = 0;
            for (Submission sub : exp.getSubmissions()) {
                Integer subCount = subELevelMap.get(sub.getdCCid());
                if (subCount != null) {
                    expCount = expCount + subCount;
                }
            }
            experimentELevel.put(exp.getName(), expCount);
        }
        return experimentELevel;
    }
    
    
    /**
     *
     * @param os objectStore
     * @return map exp-repository entries
     */
    public static Map<String, Set<String[]>> getExperimentRepositoryEntries(ObjectStore os) {
        Map<String, Set<String[]>> reposited = new HashMap<String, Set<String[]>>();
        
        Map<Integer, List<String[]>> subRepositedMap = getRepositoryEntries(os);
        
        for (DisplayExperiment exp : getExperiments(os)) {
            Set<String[]> expReps = new HashSet<String[]>();
            for (Submission sub : exp.getSubmissions()) {
                List<String[]> subReps = subRepositedMap.get(sub.getdCCid());
                if (subReps != null) {
                    expReps.addAll(subReps);
                }
            }
            // for each experiment, we don't to count twice the same repository
            // entry produced by 2 different submissions.
            Set<String[]> expRepsCleaned = removeDuplications(expReps);
            reposited.put(exp.getName(), expRepsCleaned);
        }
        return reposited;
    }
    
    
    /**
     *
     * @param os objectStore
     * @return map exp-tracks
     */
    public static Map<String, List<GBrowseTrack>> getExperimentGBrowseTracks(ObjectStore os) {
        Map<String, List<GBrowseTrack>> tracks = new HashMap<String, List<GBrowseTrack>>();
        
        Map<Integer, List<GBrowseTrack>> subTracksMap = getGBrowseTracks();
        
        for (DisplayExperiment exp : getExperiments(os)) {
            List<GBrowseTrack> expTracks = new ArrayList<GBrowseTrack>();
            tracks.put(exp.getName(), expTracks);
            for (Submission sub : exp.getSubmissions()) {
                
                if (subTracksMap.get(sub.getdCCid()) != null) {
                    List<GBrowseTrack> subTracks = subTracksMap.get(sub.getdCCid());
                    if (subTracks != null) {
                        // check so it is unique
                        // expTracks.addAll(subTracks);
                        addToList(expTracks, subTracks);
                    } else {
                        continue;
                    }
                }
            }
        }
        return tracks;
    }
    
    
    /**
     * Set the map of GBrowse tracks.
     *
     * @param tracks map of dccId:GBrowse tracks
     */
    public static synchronized void setGBrowseTracks(Map<Integer, List<GBrowseTrack>> tracks) {
        MetadataCache.class.notifyAll();
        submissionTracksCache = tracks;
    }
    
    /*==========================
    PRIVATE METHODS
============================*/
    
    private static void fetchGBrowseTracks() {
        long timeSinceLastRefresh = System.currentTimeMillis() - lastTrackCacheRefresh;
        if (timeSinceLastRefresh > TWO_HOUR) {
            readGBrowseTracks();
            lastTrackCacheRefresh = System.currentTimeMillis();
        }
    }
    
    /**
     * Method to obtain the map of unlocated feature types by submission id
     *
     * @param os the objectStore
     * @return submissionUnlocatedFeatureTypes
     */
    private static Map<Integer, List<String>> readUnlocatedFeatureTypes(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        try {
            
            if (submissionUnlocatedFeatureTypes != null) {
                return submissionUnlocatedFeatureTypes;
            }
            
            submissionUnlocatedFeatureTypes = new HashMap<Integer, List<String>>();
            if (submissionLocatedFeatureTypes == null) {
                readSubmissionLocatedFeature(os);
            }
            
            if (submissionFeatureCounts == null) {
                readSubmissionFeatureCounts(os);
            }
            
            for (Integer subId : submissionFeatureCounts.keySet()) {
                
                Set<String> allFeatures = submissionFeatureCounts.get(subId).keySet();
                Set<String> difference = new HashSet<String>(allFeatures);
                if (submissionLocatedFeatureTypes.get(subId) != null) {
                    difference.removeAll(submissionLocatedFeatureTypes.get(subId));
                }
                
                if (!difference.isEmpty()) {
                    List<String> thisUnlocated = new ArrayList<String>();
                    
                    for (String fType : difference) {
                        thisUnlocated.add(fType);
                    }
                    submissionUnlocatedFeatureTypes.put(subId, thisUnlocated);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed unlocated feature cache, took: " + timeTaken + "ms size = "
                + submissionUnlocatedFeatureTypes.size());
        return submissionUnlocatedFeatureTypes;
    }
    
    
    
    /**
     * adds the elements of a list i to a list l only if they are not yet
     * there
     * @param l the receiving list
     * @param i the donating list
     */
    private static void addToList(List<GBrowseTrack> l, List<GBrowseTrack> i) {
        Iterator<GBrowseTrack> it  = i.iterator();
        while (it.hasNext()) {
            GBrowseTrack thisId = it.next();
            if (!l.contains(thisId)) {
                l.add(thisId);
            }
        }
    }
    
    
    private static Set<String[]> removeDuplications(Set<String[]> expReps) {
        // removing the same repository entry coming from different submissions
        // in the given experiment
        Set<String> db = new HashSet<String>();
        Set<String> acc = new HashSet<String>();
        Set<String[]> dup = new HashSet<String[]>();
        for (String[] s : expReps) {
            if (db.contains(s[0]) && acc.contains(s[1])) {
                // we don't remove place holders
                if (!s[1].startsWith("To be")) {
                    dup.add(s);
                }
            }
            db.add(s[0]);
            acc.add(s[1]);
        }
        // do the difference between sets and return it
        Set<String[]> uniques = new HashSet<String[]>(expReps);
        uniques.removeAll(dup);
        return uniques;
    }
    
    
    /**
     * Fetch a map from project name to experiment.
     * @param os the production ObjectStore
     * @return a map from project name to experiment
     */
    private static Map<String, List<DisplayExperiment>>
    readProjectExperiments(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        
        projectExperiments = new TreeMap<String, List<DisplayExperiment>>();
        for (DisplayExperiment exp : getExperiments(os)) {
            List<DisplayExperiment> exps = projectExperiments.get(exp.getProjectName());
            if (exps == null) {
                exps = new ArrayList<DisplayExperiment>();
                projectExperiments.put(exp.getProjectName(), exps);
            }
            exps.add(exp);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        LOG.info("Made project map: " + projectExperiments.size()
                + " took: " + totalTime + " ms.");
        return projectExperiments;
    }
    
    /**
     * Fetch a map from project name to experiment.
     * @param os the production ObjectStore
     * @return a map from project name to experiment
     */
    private static Map<String, List<DisplayExperiment>>
    readCategoryExperiments(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        projectExperiments = getProjectExperiments(os);
        categoryExperiments = new TreeMap<String, List<DisplayExperiment>>();
        
        for (List<DisplayExperiment> expList : projectExperiments.values()) {
            for (DisplayExperiment exp : expList) {
                String cat = adaptCategory(exp);
                BioConverterUtil.addToListMap(categoryExperiments, cat, exp);
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        LOG.info("Made category map: " + categoryExperiments.size()
                + " took: " + totalTime + " ms.");
        return categoryExperiments;
    }
    
    /**
     * to reduce the number of fields and to deal temporarily with
     * sub 2675
     * TODO remove first ||
     * @param exp the DisplayExperiment
     * @return the category for the front page
     */
    private static String adaptCategory(DisplayExperiment exp) {
        String cat = exp.getCategory();
        if (cat == null) {
            return "Gene Structure";
        }
        if (cat.startsWith("Gene Structure")) {
            return "Gene Structure";
        }
        if (cat.startsWith("Replication") || cat.endsWith("Replication")) {
            return "Replication";
        }
        return cat;
    }
    
    private static void readExperiments(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        experimentFeatureCounts = readExperimentFeatureCounts(os);
        experimentUniqueFeatureCounts = readUniqueExperimentFeatureCounts(os);
        
        try {
            Query q = new Query();
            QueryClass qcProject = new QueryClass(Project.class);
            QueryField qcName = new QueryField(qcProject, "name");
            
            q.addFrom(qcProject);
            q.addToSelect(qcProject);
            
            QueryClass qcExperiment = new QueryClass(Experiment.class);
            q.addFrom(qcExperiment);
            q.addToSelect(qcExperiment);
            
            QueryCollectionReference projExperiments = new QueryCollectionReference(qcProject,
            "experiments");
            ContainsConstraint cc = new ContainsConstraint(projExperiments, ConstraintOp.CONTAINS,
                    qcExperiment);
            
            q.setConstraint(cc);
            q.addToOrderBy(qcName);
            
            Results results = os.execute(q);
            
            experimentCache = new HashMap<String, DisplayExperiment>();
            
            @SuppressWarnings("unchecked") Iterator<ResultsRow> iter =
                (Iterator) results.iterator();
            
            while (iter.hasNext()) {
                ResultsRow<?> row = (ResultsRow<?>) iter.next();
                
                Project project = (Project) row.get(0);
                Experiment experiment = (Experiment) row.get(1);
                
                // expFeatureUniqueCounts is a subset of expFeatureCounts
                Map<String, Long> expFeatureCounts =
                    experimentFeatureCounts.get(experiment.getName());
                Map<String, Long> expFeatureUniqueCounts = experimentUniqueFeatureCounts
                .get(experiment.getName());
                
                Set<FeatureCountsRecord> featureCountsRecords =
                    new LinkedHashSet<FeatureCountsRecord>();
                
                if (expFeatureCounts != null) {
                    for (Map.Entry<String, Long> entry : expFeatureCounts.entrySet()) {
                        String ft = entry.getKey();
                        Long fc = entry.getValue();
                        Long ufc = null;
                        if (expFeatureUniqueCounts.get(ft) != null) {
                            ufc = expFeatureUniqueCounts.get(ft);
                        }
                        
                        FeatureCountsRecord fcr = new FeatureCountsRecord(ft, fc, ufc);
                        featureCountsRecords.add(fcr);
                    }
                } else { featureCountsRecords = null; }
                
                DisplayExperiment displayExp = new DisplayExperiment(experiment, project,
                        featureCountsRecords, os);
                experimentCache.put(displayExp.getName(), displayExp);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed experiment cache, took: " + timeTaken + "ms size = "
                + experimentCache.size());
    }
    
    
    /**
     * The counts are duplicated in the method, see getUniqueExperimentFeatureCounts
     */
    private static Map<String, Map<String, Long>> readExperimentFeatureCounts(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        
        experimentFeatureCounts = new LinkedHashMap<String, Map<String, Long>>();
        Properties props = extractProperties(os, ModMineCacheKeys.EXP_FEATURE_COUNT);
        
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            String[] token = keyString.split("\\.");
            
            String exp = getName(token);
            String feature = token[token.length - 1];
            Long count = Long.parseLong((String) props.get(key));
            
            Map<String, Long> featureCounts = experimentFeatureCounts.get(exp);
            if (featureCounts == null) {
                featureCounts = new HashMap<String, Long>();
                experimentFeatureCounts.put(exp, featureCounts);
            }
            featureCounts.put(feature, count);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Read experiment feature counts, took: " + timeTaken + "ms");
        
        return experimentFeatureCounts;
    }
    
    /**
     * to deal with experiment with (1) dot in the name
     * e.g. "Changes in expression of small RNAs during aging in C. elegans"
     * @param token
     * @return the experiment name
     */
    private static String getName(String[] token) {
        String exp = null;
        if (token.length > 2) {
            exp = token[0] + "." + token[1];
            return exp;
        }
        exp = token[0];
        return exp;
    }
    
    /**
     * Method equivalent to getExperimentFeatureCounts but return Unique counts
     *
     * @param os
     * @return Map<String: expName, Map<String: feature type, Long: count>>
     */
    private static Map<String, Map<String, Long>>
    readUniqueExperimentFeatureCounts(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        experimentUniqueFeatureCounts = new LinkedHashMap<String, Map<String, Long>>();
        
        Properties props = extractProperties(os, ModMineCacheKeys.UNIQUE_EXP_FEATURE_COUNT);
        
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            
            String[] token = keyString.split("\\.");
            String exp = stripDash(getName(token));
            String feature = token[token.length - 1];
            Long count = Long.parseLong((String) props.get(key));
            
            Map<String, Long> featureCounts = experimentUniqueFeatureCounts.get(exp);
            if (featureCounts == null) {
                featureCounts = new HashMap<String, Long>();
                experimentUniqueFeatureCounts.put(exp, featureCounts);
            }
            featureCounts.put(feature, count);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Read experiment unique feature counts, took: " + timeTaken + "ms");
        
        return experimentUniqueFeatureCounts;
    }
    
    /**
     * to extract the required properties from the db
     * @param os
     * @param the property key
     * @return the properties
     */
    private static Properties extractProperties(ObjectStore os, String propKey) {
        Properties props = new Properties();
        try {
            props =
                PropertiesUtil.stripStart(propKey, getProperties(os));
        } catch (SQLException e) {
            throw new RuntimeException("SQL ERROR while getting property " + propKey, e);
        } catch (IOException e) {
            throw new RuntimeException("IO error while getting property " + propKey, e);
        }
        return props;
    }
    
    /**
     * @param exp
     */
    private static String stripDash(String exp) {
        String fixed = exp.replace("\\", "");
        return fixed;
    }
    
    private static Properties readProperties(ObjectStore os) throws SQLException, IOException {
        Database db = ((ObjectStoreInterMineImpl) os).getDatabase();
        String objectSummaryString =
            MetadataManager.retrieve(db, MetadataManager.MODMINE_METADATA_CACHE);
        
        metadataProperties = new Properties();
        InputStream objectStoreSummaryPropertiesStream = new StringInputStream(objectSummaryString);
        
        metadataProperties.load(objectStoreSummaryPropertiesStream);
        return metadataProperties;
    }
    
    private static void readSubmissionFeatureCounts(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        
        submissionFeatureCounts = new LinkedHashMap<Integer, Map<String, Long>>();
        Properties props = extractProperties(os, ModMineCacheKeys.SUB_FEATURE_COUNT);
        
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            
            String[] token = keyString.split("\\.");
            Integer dccId = Integer.parseInt(token[0]);
            String feature = token[1];
            Long count = Long.parseLong((String) props.get(key));
            
            Map<String, Long> featureCounts = submissionFeatureCounts.get(dccId);
            if (featureCounts == null) {
                featureCounts = new HashMap<String, Long>();
                submissionFeatureCounts.put(dccId, featureCounts);
            }
            featureCounts.put(feature, count);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed submissionFeatureCounts cache, took: " + timeTaken + "ms size = "
                + submissionFeatureCounts.size());
    }
    
    private static void readSubmissionIds(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        
        submissionIdCache = new HashMap<Integer, Integer>();
        
        Query q = new Query();
        q.setDistinct(false);
        QueryClass qcSub = new QueryClass(Submission.class);
        q.addFrom(qcSub);
        q.addToSelect(qcSub);
        q.addToOrderBy(qcSub);
        
        Results results = os.execute(q);
        // for each classes set the values for jsp
        @SuppressWarnings("unchecked") Iterator<ResultsRow> iter =
            (Iterator) results.iterator();
        while (iter.hasNext()) {
            ResultsRow<?> row = iter.next();
            Submission sub = (Submission) row.get(0);
            submissionIdCache.put(sub.getdCCid(), sub.getId());
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed submissions cache, took: " + timeTaken
                + "ms size = " + submissionIdCache.size());
    }
    
    private static void readSubmissionFeatureExpressionLevelCounts(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        
        submissionFeatureExpressionLevelCounts = new LinkedHashMap<Integer, Map<String, Long>>();
        Properties props =
            extractProperties(os, ModMineCacheKeys.SUB_FEATURE_EXPRESSION_LEVEL_COUNT);
        
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            
            String[] token = keyString.split("\\.");
            Integer dccId = Integer.parseInt(token[0]);
            String feature = token[1];
            Long count = Long.parseLong((String) props.get(key));
            
            Map<String, Long> featureCounts = submissionFeatureExpressionLevelCounts.get(dccId);
            if (featureCounts == null) {
                featureCounts = new HashMap<String, Long>();
                submissionFeatureExpressionLevelCounts.put(dccId, featureCounts);
            }
            featureCounts.put(feature, count);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed submissionFeatureExpressionLevelCounts cache, took: "
                + timeTaken + "ms size = "
                + submissionFeatureExpressionLevelCounts.size());
        LOG.debug("submissionFeatureELCounts " + submissionFeatureExpressionLevelCounts);
    }
    
    private static void readExperimentFeatureExpressionLevelCounts(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        submissionFeatureExpressionLevelCounts = getSubmissionFeatureExpressionLevelCounts(os);
        experimentFeatureExpressionLevelCounts = new LinkedHashMap<String, Map<String, Long>>();
        
        for (DisplayExperiment exp : getExperiments(os)) {
            Map<String, Long> featCount = new HashMap<String, Long>();
            
            for (Integer sub : submissionFeatureExpressionLevelCounts.keySet()) {
                Map<String, Long> subFeatCount = submissionFeatureExpressionLevelCounts.get(sub);
                if (subFeatCount == null) {
                    continue;
                }
                for (String feat : subFeatCount.keySet()) {
                    Long count = null;
                    if (featCount.containsKey(feat)) {
                        count = featCount.get(feat) + subFeatCount.get(feat);
                    } else {
                        count = subFeatCount.get(feat);
                    }
                    featCount.put(feat, count);
                }
            }
            experimentFeatureExpressionLevelCounts.put(exp.getName(), featCount);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed experimentFeatureExpressionLevelCounts cache, took: " + timeTaken
                + "ms size = " + experimentFeatureExpressionLevelCounts.size());
        LOG.debug("experimentFeatureELCounts " + experimentFeatureExpressionLevelCounts);
    }
    
    
    private static void readSubmissionExpressionLevelCounts(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        submissionExpressionLevelCounts = new HashMap<Integer, Integer>();
        if (submissionIdCache == null) {
            readSubmissionIds(os);
        }
        
        if (submissionFeatureExpressionLevelCounts == null) {
            readSubmissionFeatureExpressionLevelCounts(os);
        }
        
        for (Integer dccId : submissionIdCache.keySet()) {
            Integer count = 0;
            Map<String, Long> featureCounts =
                submissionFeatureExpressionLevelCounts.get(dccId);
            if (featureCounts == null) {
                continue;
            }
            if (!featureCounts.isEmpty()) {
                for (String feat : featureCounts.keySet()) {
                    count = count + featureCounts.get(feat).intValue();
                }
            }
            submissionExpressionLevelCounts.put(dccId, count);
        }
        
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed submissionExpressionLevelCounts cache, took: " + timeTaken
                + "ms size = " + submissionExpressionLevelCounts.size());
        LOG.debug("submissionELCounts " + submissionExpressionLevelCounts);
    }
    
    
    private static void readSubmissionFiles(ObjectStore os) {
        //
        long startTime = System.currentTimeMillis();
        try {
            
            Query q = new Query();
            QueryClass qcSubmission = new QueryClass(Submission.class);
            QueryField qfDCCid = new QueryField(qcSubmission, "DCCid");
            q.addFrom(qcSubmission);
            q.addToSelect(qfDCCid);
            
            QueryClass qcFile = new QueryClass(ResultFile.class);
            q.addFrom(qcFile);
            q.addToSelect(qcFile);
            
            QueryCollectionReference subFiles = new QueryCollectionReference(qcSubmission,
            "resultFiles");
            ContainsConstraint cc = new ContainsConstraint(subFiles, ConstraintOp.CONTAINS,
                    qcFile);
            
            q.setConstraint(cc);
            q.addToOrderBy(qfDCCid);
            
            Results results = os.execute(q);
            
            submissionFilesCache = new HashMap<Integer, Set<ResultFile>>();
            
            @SuppressWarnings("unchecked") Iterator<ResultsRow> iter =
                (Iterator) results.iterator();
            
            while (iter.hasNext()) {
                ResultsRow<?> row = (ResultsRow<?>) iter.next();
                
                Integer dccId = (Integer) row.get(0);
                ResultFile file = (ResultFile) row.get(1);
                
                addToMap(submissionFilesCache, dccId, file);
            }
            
        } catch (Exception err) {
            err.printStackTrace();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed submission collections caches, took: " + timeTaken + "ms    size: files = "
                + submissionFilesCache.size());
    }
    
    
    private static void readSubmissionLocatedFeature(ObjectStore os) {
        long startTime = System.currentTimeMillis();
        submissionLocatedFeatureTypes = new LinkedHashMap<Integer, List<String>>();
        Properties props = extractProperties(os, ModMineCacheKeys.SUB_LOCATED_FEATURE_TYPE);
        
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            
            String[] token = keyString.split("\\.");
            Integer dccId = Integer.parseInt(token[0]);
            String feature = (String) props.get(key);
            
            addToMap(submissionLocatedFeatureTypes, dccId, feature);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed located features cache, took: " + timeTaken + "ms size = "
                + submissionLocatedFeatureTypes.size());
    }
    
    
    /**
     * Fetch reposited (GEO/SRA/AE..) entries per submission.
     * @param os the production objectStore
     * @return map
     */
    public static synchronized Map<Integer, List<String[]>> getRepositoryEntries(ObjectStore os) {
        if (submissionRepositedCache == null) {
            readSubmissionRepositoryEntries(os);
        }
        return submissionRepositedCache;
    }
    
    
    private static void readSubmissionRepositoryEntries(ObjectStore os) {
        //
        long startTime = System.currentTimeMillis();
        try {
            Query q = new Query();
            QueryClass qcSubmission = new QueryClass(Submission.class);
            QueryField qfDCCid = new QueryField(qcSubmission, "DCCid");
            q.addFrom(qcSubmission);
            q.addToSelect(qfDCCid);
            
            QueryClass qcRepositoryEntry = new QueryClass(DatabaseRecord.class);
            QueryField qfDatabase = new QueryField(qcRepositoryEntry, "database");
            QueryField qfAccession = new QueryField(qcRepositoryEntry, "accession");
            QueryField qfUrl = new QueryField(qcRepositoryEntry, "url");
            q.addFrom(qcRepositoryEntry);
            q.addToSelect(qfDatabase);
            q.addToSelect(qfAccession);
            q.addToSelect(qfUrl);
            
            // join the tables
            QueryCollectionReference ref1 =
                new QueryCollectionReference(qcSubmission, "databaseRecords");
            ContainsConstraint cc = new ContainsConstraint(ref1, ConstraintOp.CONTAINS,
                    qcRepositoryEntry);
            
            q.setConstraint(cc);
            q.addToOrderBy(qfDCCid);
            q.addToOrderBy(qfDatabase);
            
            Results results = os.execute(q);
            
            submissionRepositedCache = new HashMap<Integer, List<String[]>>();
            
            Integer counter = 0;
            
            Integer prevSub = new Integer(-1);
            List<String[]> subRep = new ArrayList<String[]>();
            Iterator<?> i = results.iterator();
            while (i.hasNext()) {
                ResultsRow<?> row = (ResultsRow<?>) i.next();
                
                counter++;
                Integer dccId = (Integer) row.get(0);
                String db = (String) row.get(1);
                String acc = (String) row.get(2);
                String url = (String) row.get(3);
                String[] thisRecord = {db, acc, url};
                
                if (!dccId.equals(prevSub) || counter.equals(results.size())) {
                    if (prevSub > 0) {
                        if (counter.equals(results.size())) {
                            prevSub = dccId;
                            subRep.add(thisRecord);
                        }
                        List<String[]> subRepIn = new ArrayList<String[]>();
                        subRepIn.addAll(subRep);
                        submissionRepositedCache.put(prevSub, subRepIn);
                        subRep.clear();
                    }
                    prevSub = dccId;
                }
                subRep.add(thisRecord);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed Repository entries cache, took: " + timeTaken + "ms size = "
                + submissionRepositedCache.size());
    }
    
    
    /**
     * adds an element to a list which is the value of a map
     * @param m       the map (<Integer, List<String>>)
     * @param key     the key for the map
     * @param value   the list
     */
    private static void addToMap(Map<Integer, List<String>> m, Integer key, String value) {
        
        List<String> ids = new ArrayList<String>();
        
        if (m.containsKey(key)) {
            ids = m.get(key);
        }
        if (!ids.contains(value)) {
            ids.add(value);
            m.put(key, ids);
        }
    }
    
    /**
     * adds an element to a set of ResultFile which is the value of a map
     * @param m       the map (<Integer, Set<ResultFile>>)
     * @param key     the key for the map
     * @param value   the list
     */
    private static void addToMap(Map<Integer, Set<ResultFile>> m, Integer key, ResultFile value) {
        
        Set<ResultFile> files = new HashSet<ResultFile>();
        
        if (m.containsKey(key)) {
            files = m.get(key);
        }
        if (!files.contains(value)) {
            // check if same name
            for (ResultFile file : files) {
                if (file.getName().equals(value.getName())) {
                    LOG.debug("DUPLICATED FILE " + value.getName() + " - ids: in " + value.getId()
                            + " dup " + file.getId());
                    return;
                }
            }
            files.add(value);
            m.put(key, files);
        }
    }
    
    /**
     * Method to fill the cached map of submissions (ddcId) to list of
     * GBrowse tracks
     *
     */
    private static void readGBrowseTracks() {
        Runnable r = new Runnable() {
            public void run() {
                threadedReadGBrowseTracks();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
    
    private static void threadedReadGBrowseTracks() {
        long startTime = System.currentTimeMillis();
        
        Map<Integer, List<GBrowseTrack>> tracks = new HashMap<Integer, List<GBrowseTrack>>();
        Map<Integer, List<GBrowseTrack>> flyTracks = null;
        Map<Integer, List<GBrowseTrack>> wormTracks = null;
        try {
            flyTracks = GBrowseParser.readTracks("fly");
            wormTracks = GBrowseParser.readTracks("worm");
        } catch (Exception e) {
            LOG.error(e);
        }
        
        if (flyTracks != null && wormTracks != null) {
            tracks.putAll(flyTracks);
            tracks.putAll(wormTracks);
            setGBrowseTracks(tracks);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed GBrowse tracks cache, took: " + timeTaken + "ms  size = "
                + tracks.size());
    }
    
    
    /**
     * This method get the feature descriptions from a property file.
     *
     * @return the map feature/description
     */
    private static Map<String, String> readFeatTypeDescription(ServletContext servletContext) {
        long startTime = System.currentTimeMillis();
        
        featDescriptionCache = new HashMap<String, String>();
        
        Properties props = new Properties();
        
        InputStream is
        = servletContext.getResourceAsStream("/WEB-INF/featureTypeDescr.properties");
        if (is == null) {
            LOG.info("Unable to find /WEB-INF/featureTypeDescr.properties, "
                    + "there will be no feature type descriptions");
        } else {
            try {
                props.load(is);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Enumeration<?> en = props.keys();
            while (en.hasMoreElements()) {
                String expFeat = (String) en.nextElement();
                String descr = props.getProperty(expFeat);
                featDescriptionCache.put(expFeat, descr);
            }
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        LOG.info("Primed feature description cache, took: " + timeTaken + "ms size = "
                + featDescriptionCache.size());
        return featDescriptionCache;
    }
}
