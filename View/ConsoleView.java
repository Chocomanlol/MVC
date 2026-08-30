package View;

import Controller.ElectionController;
import java.util.Map;
import java.util.Scanner;

import Model.BallotStatus;
import Model.ElectionStatus;
import Model.PatternGroup;


import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConsoleView {
    private final ElectionController controller;
    private final Scanner scanner;

    public ConsoleView(ElectionController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printHeader();
            printMenu();
            System.out.print("เลือกเมนู (0-5): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showCandidates();
                case "2" -> castVote();
                case "3" -> closeElection();
                case "4" -> decidePendingGroups();
                case "5" -> showDetailedStatus();
                case "0" -> {
                    System.out.println("\n👋 ปิดโปรแกรมเรียบร้อยแล้ว");
                    running = false;
                }
                default -> System.out.println("❌ เมนูไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง");
            }
        }
    }

    // --- R5: หน้าสรุปสถานะตามวงจรชีวิตการเลือกตั้ง ---
    private void printHeader() {
        System.out.println("\n==================================================");
        System.out.println("🗳️  ระบบเลือกตั้งประธานชมรม");
        System.out.println("สถานะปัจจุบัน: [" + controller.getElectionStatus() + "]");
        System.out.println("--------------------------------------------------");

        ElectionStatus status = controller.getElectionStatus();
        if (status == ElectionStatus.OPEN) {
            System.out.println("📊 จำนวนบัตรที่รับแล้วทั้งหมด: " + controller.getBallots().size() + " ใบ");
        } else if (status == ElectionStatus.CLOSED) {
            System.out.println("📊 ผลคะแนนชั่วคราว (จากบัตรที่รับรองแล้ว):");
            printScores();
        } else if (status == ElectionStatus.FINALIZED) {
            System.out.println("🏆 ผลการเลือกตั้งอย่างเป็นทางการ (FINALIZED):");
            printScores();
            System.out.println("✅ บัตรที่รับรอง: " + controller.getApprovedBallotCount() + " ใบ | ❌ บัตรที่ไม่นับ: " + controller.getRejectedBallotCount() + " ใบ");
        }
        System.out.println("==================================================");
    }

    private void printMenu() {
        System.out.println("1. ดูรายชื่อผู้สมัครทั้งหมด");
        System.out.println("2. ลงคะแนนเสียง (โหมดผู้มีสิทธิ์เลือกตั้ง)");
        System.out.println("3. ปิดรับคะแนน (โหมดเจ้าหน้าที่)");
        System.out.println("4. ตรวจตัดสินกลุ่มบัตรซ้ำ (โหมดเจ้าหน้าที่)");
        System.out.println("5. ดูสรุปสถานะและ Audit Trail ทั้งหมด");
        System.out.println("0. ออกจากโปรแกรม");
    }

    // --- R2: แสดงผู้สมัคร ---
    private void showCandidates() {
        System.out.println("\n--- รายชื่อผู้สมัครทั้งหมด ---");
        controller.getCandidates().forEach(c -> 
            System.out.println("รหัส: " + c.getId() + " | ชื่อ: " + c.getName())
        );
    }

    // --- R2: รับค่าการลงคะแนน ---
    private void castVote() {
        if (controller.getElectionStatus() != ElectionStatus.OPEN) {
            System.out.println("❌ ระบบปิดรับคะแนนแล้ว ไม่สามารถลงคะแนนเพิ่มได้");
            return;
        }

        System.out.println("\n--- ลงคะแนนเสียง ---");
        System.out.print("กรอกรหัสผู้มีสิทธิ์ (เช่น V01-V07): ");
        String voterId = scanner.nextLine().trim();

        showCandidates();
        System.out.print("เลือกอันดับ 1 (กรอกรหัสผู้สมัคร): ");
        String r1 = scanner.nextLine().trim();
        System.out.print("เลือกอันดับ 2 (กรอกรหัสผู้สมัคร): ");
        String r2 = scanner.nextLine().trim();
        System.out.print("เลือกอันดับ 3 (กรอกรหัสผู้สมัคร): ");
        String r3 = scanner.nextLine().trim();

        String response = controller.castVote(voterId, r1, r2, r3);
        System.out.println(response);
    }

    // --- R3: ปิดรับคะแนน ---
    private void closeElection() {
        String response = controller.closeElection();
        System.out.println(response);
    }

    // --- R4: ตรวจตัดสินกลุ่มบัตรซ้ำ ---
    private void decidePendingGroups() {
        if (controller.getElectionStatus() != ElectionStatus.CLOSED) {
            System.out.println("❌ สามารถตรวจตัดสินได้เฉพาะตอนสถานะ CLOSED เท่านั้น");
            return;
        }

        Map<String, PatternGroup> groups = controller.getPatternGroups();
        boolean hasPending = false;

        System.out.println("\n--- รายการกลุ่มรูปแบบบัตรที่รอตรวจสอบ (PENDING) ---");
        for (Map.Entry<String, PatternGroup> entry : groups.entrySet()) {
            PatternGroup g = entry.getValue();
            if (g.getStatus() == BallotStatus.PENDING) {
                hasPending = true;
                System.out.println("กลุ่มรูปแบบ: [" + g.getPatternKey() + "] | จำนวน: " + g.getBallots().size() + " ใบ");
            }
        }

        if (!hasPending) {
            System.out.println("ไม่มีกลุ่มรูปแบบที่รอการตรวจสอบแล้ว");
            return;
        }

        System.out.print("\nกรอก Pattern ที่ต้องการตัดสิน (เช่น C01>C02>C03): ");
        String patternKey = scanner.nextLine().trim();

        System.out.print("เลือกคำตัดสิน (1 = รับรอง / 2 = ไม่นับ): ");
        String decision = scanner.nextLine().trim();

        if (decision.equals("1")) {
            System.out.println(controller.decideGroup(patternKey, true));
        } else if (decision.equals("2")) {
            System.out.println(controller.decideGroup(patternKey, false));
        } else {
            System.out.println("❌ ตัวเลือกคำตัดสินไม่ถูกต้อง");
        }
    }

    // --- R5 & Audit Trail ---
    private void showDetailedStatus() {
        System.out.println("\n--- รายละเอียดกลุ่มรูปแบบบัตรทั้งหมด ---");
        controller.getPatternGroups().values().forEach(g -> 
            System.out.println("กลุ่ม: [" + g.getPatternKey() + "] | สถานะ: " + g.getStatus() + " | จำนวนบัตร: " + g.getBallots().size() + " ใบ")
        );

        System.out.println("\n--- รายละเอียดบัตรลงคะแนนทั้งหมด (Audit Trail) ---");
        controller.getBallots().forEach(b -> 
            System.out.println("บัตร " + b.getId() + " | ผู้ลงคะแนน: " + b.getVoterId() + " | เลือก: " + b.getRanking() + " | สถานะบัตร: " + b.getStatus())
        );
    }

    private void printScores() {
        controller.getScores().forEach((candidateId, score) -> {
            String candidateName = controller.getCandidates().stream()
                    .filter(c -> c.getId().equals(candidateId))
                    .findFirst().map(c -> c.getName()).orElse("Unknown");
            System.out.println("  • " + candidateId + " (" + candidateName + "): " + score + " คะแนน");
        });
    }
}
