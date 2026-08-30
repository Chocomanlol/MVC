package View;

import Controller.ElectionController;
import Model.BallotStatus;
import Model.ElectionStatus;
import Model.PatternGroup;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class ConsoleView {
    private final ElectionController controller;
    private final Scanner scanner;

    public ConsoleView(ElectionController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printHeader();
            printMenu();
            System.out.print("Select option (0-5): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showCandidates();
                case "2" -> castVote();
                case "3" -> closeElection();
                case "4" -> decidePendingGroups();
                case "5" -> showDetailedStatus();
                case "0" -> {
                    System.out.println("\n👋 Application closed successfully.");
                    running = false;
                }
                default -> System.out.println("❌ Invalid option. Please try again.");
            }
        }
    }

    // --- R5: Status Summary according to Election Lifecycle ---
    private void printHeader() {
        System.out.println("\n==================================================");
        System.out.println("🗳️  Club President Election System");
        System.out.println("Current Status: [" + controller.getElectionStatus() + "]");
        System.out.println("--------------------------------------------------");

        ElectionStatus status = controller.getElectionStatus();
        if (status == ElectionStatus.OPEN) {
            System.out.println("📊 Total ballots received: " + controller.getBallots().size() + " ballots");
        } else if (status == ElectionStatus.CLOSED) {
            System.out.println("📊 Provisional Scores (from approved ballots):");
            printScores();
        } else if (status == ElectionStatus.FINALIZED) {
            System.out.println("🏆 Official Election Results (FINALIZED):");
            printScores();
            System.out.println("✅ Approved ballots: " + controller.getApprovedBallotCount() + " | ❌ Rejected ballots: " + controller.getRejectedBallotCount());
        }
        System.out.println("==================================================");
    }

    private void printMenu() {
        System.out.println("1. View all candidates");
        System.out.println("2. Cast vote (Voter mode)");
        System.out.println("3. Close election (Officer mode)");
        System.out.println("4. Review duplicate pattern groups (Officer mode)");
        System.out.println("5. View summary status and Audit Trail");
        System.out.println("0. Exit program");
    }

    // --- R2: Display candidates ---
    private void showCandidates() {
        System.out.println("\n--- All Candidates ---");
        controller.getCandidates().forEach(c -> 
            System.out.println("ID: " + c.getId() + " | Name: " + c.getName())
        );
    }

    // --- R2: Cast vote ---
    private void castVote() {
        if (controller.getElectionStatus() != ElectionStatus.OPEN) {
            System.out.println("❌ Election is closed. Voting is no longer allowed.");
            return;
        }

        System.out.println("\n--- Cast Vote ---");
        System.out.print("Enter Voter ID (e.g., V01-V07): ");
        String voterId = scanner.nextLine().trim();

        showCandidates();
        System.out.print("Select Rank 1 (Enter Candidate ID): ");
        String r1 = scanner.nextLine().trim();
        System.out.print("Select Rank 2 (Enter Candidate ID): ");
        String r2 = scanner.nextLine().trim();
        System.out.print("Select Rank 3 (Enter Candidate ID): ");
        String r3 = scanner.nextLine().trim();

        String response = controller.castVote(voterId, r1, r2, r3);
        System.out.println(response);
    }

    // --- R3: Close election ---
    private void closeElection() {
        String response = controller.closeElection();
        System.out.println(response);
    }

    // --- R4: Review duplicate pattern groups ---
    private void decidePendingGroups() {
        if (controller.getElectionStatus() != ElectionStatus.CLOSED) {
            System.out.println("❌ Pattern groups can only be reviewed when status is CLOSED.");
            return;
        }

        Map<String, PatternGroup> groups = controller.getPatternGroups();
        boolean hasPending = false;

        System.out.println("\n--- Pending Pattern Groups (PENDING) ---");
        for (Map.Entry<String, PatternGroup> entry : groups.entrySet()) {
            PatternGroup g = entry.getValue();
            if (g.getStatus() == BallotStatus.PENDING) {
                hasPending = true;
                System.out.println("Pattern Group: [" + g.getPatternKey() + "] | Count: " + g.getBallots().size() + " ballots");
            }
        }

        if (!hasPending) {
            System.out.println("No pending pattern groups to review.");
            return;
        }

        System.out.print("\nEnter Pattern Key to review (e.g., C01>C02>C03): ");
        String patternKey = scanner.nextLine().trim();

        System.out.print("Select decision (1 = Approve / 2 = Reject): ");
        String decision = scanner.nextLine().trim();

        if (decision.equals("1")) {
            System.out.println(controller.decideGroup(patternKey, true));
        } else if (decision.equals("2")) {
            System.out.println(controller.decideGroup(patternKey, false));
        } else {
            System.out.println("❌ Invalid decision choice.");
        }
    }

    // --- R5 & Audit Trail ---
    private void showDetailedStatus() {
        System.out.println("\n--- All Pattern Groups Summary ---");
        controller.getPatternGroups().values().forEach(g -> 
            System.out.println("Group: [" + g.getPatternKey() + "] | Status: " + g.getStatus() + " | Ballot Count: " + g.getBallots().size() + " ballots")
        );

        System.out.println("\n--- All Ballots Detail (Audit Trail) ---");
        controller.getBallots().forEach(b -> 
            System.out.println("Ballot " + b.getId() + " | Voter: " + b.getVoterId() + " | Ranking: " + b.getRanking() + " | Ballot Status: " + b.getStatus())
        );
    }

    private void printScores() {
        controller.getScores().forEach((candidateId, score) -> {
            String candidateName = controller.getCandidates().stream()
                    .filter(c -> c.getId().equals(candidateId))
                    .findFirst().map(c -> c.getName()).orElse("Unknown");
            System.out.println("  • " + candidateId + " (" + candidateName + "): " + score + " points");
        });
    }
}
