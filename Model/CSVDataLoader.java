package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream; 
import java.io.InputStreamReader;

public class CSVDataLoader {

    public static void loadSeedData(ElectionModel model, String folderPath) {
        loadCandidates(model, folderPath + "/candidates.csv");
        loadVoters(model, folderPath + "/voters.csv");
        loadBallots(model, folderPath + "/ballots.csv");
    }

    private static void loadCandidates(ElectionModel model, String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 2) model.addCandidate(new Candidate(p[0].trim(), p[1].trim()));
            }
        } catch (IOException e) {
            System.err.println("Error reading candidates.csv: " + e.getMessage());
        }
    }

    private static void loadVoters(ElectionModel model, String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3) {
                    model.addVoter(new Voter(p[0].trim(), p[1].trim(), Boolean.parseBoolean(p[2].trim())));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading voters.csv: " + e.getMessage());
        }
    }

    private static void loadBallots(ElectionModel model, String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5) {
                    model.addInitialBallot(new Ballot(p[0].trim(), p[1].trim(), List.of(p[2].trim(), p[3].trim(), p[4].trim())));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading ballots.csv: " + e.getMessage());
        }
    }
}
