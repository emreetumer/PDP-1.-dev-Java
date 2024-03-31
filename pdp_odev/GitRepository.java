package pdp_odev;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitRepository {
    private String repoUrl;
    private String repoName;

    public GitRepository(String repoUrl) {
        this.repoUrl = repoUrl;
        this.repoName = getRepoNameFromUrl(repoUrl);
    }

    public boolean cloneRepository() {
        try {
            Process process = Runtime.getRuntime().exec("git clone " + repoUrl);
            process.waitFor();
            int exitCode = process.exitValue();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void analyzeJavaFiles() {
        File repoDir = new File(repoName);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            System.out.println("Repository directory not found. Exiting...");
            return;
        }

        File[] javaFiles = repoDir.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null || javaFiles.length == 0) {
            System.out.println("No Java files found in the repository. Exiting...");
            return;
        }

        for (File javaFile : javaFiles) {
            analyzeJavaFile(javaFile);
        }
    }

    private void analyzeJavaFile(File javaFile) {
        // Initialize variables for analysis
        int javadocLines = 0;
        int otherComments = 0;
        int codeLines = 0;
        int loc = 0;
        int functionCount = 0;

        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(javaFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean inCommentBlock = false;
        for (String line : lines) {
            line = line.trim();
            loc++;

            if (line.startsWith("//")) {
                otherComments++;
            } else if (line.startsWith("/*")) {
                inCommentBlock = true;
                otherComments++;
            } else if (line.endsWith("*/")) {
                inCommentBlock = false;
                otherComments++;
            } else if (inCommentBlock) {
                otherComments++;
            } else if (line.startsWith("/**")) {
                javadocLines++;
            } else if (line.startsWith("*")) {
                javadocLines++;
            } else if (!line.isEmpty()) {
                codeLines++;
            }

            // Count functions (assuming method declaration lines)
            if (line.contains("(") && line.contains(")") && line.contains("{") && line.contains("}")) {
                functionCount++;
            }
        }

        // Calculate analysis metrics
        double yg = ((javadocLines + otherComments) * 0.8) / functionCount;
        double yh = (double) codeLines / functionCount * 0.3;
        double commentDeviationPercentage = ((100 * yg) / yh) - 100;

        // Print analysis results
        System.out.println("Sınıf: " + javaFile.getName());
        System.out.println("Javadoc Satır Sayısı: " + javadocLines);
        System.out.println("Yorum Satır Sayısı: " + otherComments);
        System.out.println("Kod Satır Sayısı: " + codeLines);
        System.out.println("LOC: " + loc);
        System.out.println("Fonksiyon Sayısı: " + functionCount);
        System.out.println("Yorum Sapma Yüzdesi: %" + commentDeviationPercentage);
        System.out.println("-----------------------------------------");
    }

    private String getRepoNameFromUrl(String url) {
        // Pattern to match GitHub repository URL
        Pattern pattern = Pattern.compile("github\\.com/(\\S+)/(.+?)(\\.git)?/?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String username = matcher.group(1);
            String repository = matcher.group(2);
            return username + "-" + repository;
        }
        return null;
    }
}
