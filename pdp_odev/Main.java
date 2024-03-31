package pdp_odev;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    /**
     * Main method to run the program.
     * 
     * @param args Command line arguments (not used in this program)
     */
    public static void main(String[] args) {
        // Prompt the user for the GitHub repository URL
        System.out.println("Please enter the GitHub repository URL:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String repoUrl = null;
        try {
            repoUrl = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Clone the repository
        GitRepository repository = new GitRepository(repoUrl);
        if (!repository.cloneRepository()) {
            System.out.println("Failed to clone the repository. Exiting...");
            return;
        }

        // Analyze Java files in the repository
        repository.analyzeJavaFiles();
    }
}
