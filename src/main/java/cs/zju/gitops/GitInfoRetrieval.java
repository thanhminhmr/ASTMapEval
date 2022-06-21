package cs.zju.gitops;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class GitInfoRetrieval {
    private static String project = "";
    private static String commitId = "";
    private static Map<String, String> oldModifiedFileMap;
    private static List<String> addedFiles;
    private static List<String> deletedFiles;
    private static Map<String, ByteArrayOutputStream> fileDiffMap;
    private static boolean hasDeletedFile = false;

    private static void initInfo(String project, String commitId){
        if (checkInCache(project, commitId))
            return;
        GitInfoRetrieval.project = project;
        GitInfoRetrieval.commitId = commitId;

        try {
            addedFiles = new ArrayList<>();
            oldModifiedFileMap = new HashMap<>();
            fileDiffMap = new HashMap<>();
            GitUtils.getModificationInfo(project, commitId, addedFiles, oldModifiedFileMap, fileDiffMap);
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Git Error: " + commitId);
        }
        processTestAndNonJavaFiles();
    }

    private static void processTestAndNonJavaFiles(){
        List<String> tempList = new ArrayList<>();
        Map<String, String> tempFileMap = new HashMap<>();
        Map<String, ByteArrayOutputStream> tempDiffMap = new HashMap<>();
        deletedFiles = new ArrayList<>();
        for (String filePath: addedFiles){
            if (GitUtils.checkNonJavaAndTestFiles(filePath))
                tempList.add(filePath);
        }
        for (String filePath: oldModifiedFileMap.keySet()){
            String newPath = oldModifiedFileMap.get(filePath);
            if (newPath == null){
                if (GitUtils.checkNonJavaAndTestFiles(filePath)) {
                    deletedFiles.add(filePath);
                    tempFileMap.put(filePath, null);
                    tempDiffMap.put(filePath, fileDiffMap.get(filePath));
                }
            } else {
                if (GitUtils.checkNonJavaAndTestFiles(filePath) && GitUtils.checkNonJavaAndTestFiles(newPath)) {
                    tempFileMap.put(filePath, newPath);
                    tempDiffMap.put(filePath, fileDiffMap.get(filePath));
                }
            }
        }
        addedFiles = tempList;
        oldModifiedFileMap = tempFileMap;
        fileDiffMap = tempDiffMap;
    }

    private static boolean checkInCache(String project, String commitId){
        return GitInfoRetrieval.project.equals(project) && GitInfoRetrieval.commitId.equals(commitId);
    }

    public static List<String> getDeletedFiles(String project, String commitId) {
        initInfo(project, commitId);
        return deletedFiles;
    }

    public static List<String> getAddedFiles(String project, String commitId) {
        initInfo(project, commitId);
        return addedFiles;
    }

    public static Map<String, String> getOldModifiedFileMap(String project, String commitId) {
        initInfo(project, commitId);
        return oldModifiedFileMap;
    }

    public static Map<String, ByteArrayOutputStream> getFileDiffMap(String project, String commitId) {
        initInfo(project, commitId);
        return fileDiffMap;
    }

    public static ByteArrayOutputStream getFileDiff(String project, String commitId, String path) {
        initInfo(project, commitId);
        return fileDiffMap.get(path);
    }

    public static String getAfterModificationPath(String project, String commitId, String oldPath) {
        initInfo(project, commitId);
        return oldModifiedFileMap.get(oldPath);
    }

    public static boolean hasDeletedFile(){
        initInfo(project, commitId);
        return hasDeletedFile;
    }
}
