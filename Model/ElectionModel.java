package Model;

import java.util.*;

public class ElectionModel {
    private String title = "การเลือกตั้งประธานชมรมโปร่งใสจริง ๆ นะ";
    private ElectionStatus status = ElectionStatus.OPEN;
    private int threshold = 3;

    private Map<String, Candidate> candidates = new LinkedHashMap<>();
    private Map<String, Voter> voters = new LinkedHashMap<>();
    private List<Ballot> ballots = new ArrayList<>();
    private Map<String, PatternGroup> patternGroups = new LinkedHashMap<>();
    private int ballotCounter = 1;

    // --- R2: การดูผู้สมัครและการลงคะแนน ---
    public Collection<Candidate> getCandidates() { 
        return candidates.values(); 
    }

    public Collection<Voter> getVoters() { 
        return voters.values(); 
    }

    public ElectionStatus getStatus() { 
        return status; 
    }

    public List<Ballot> getBallots() { 
        return ballots; 
    }

    public Map<String, PatternGroup> getPatternGroups() { 
        return patternGroups; 
    }

    public void castVote(String voterId, String r1, String r2, String r3) {
        // 1. ตรวจสอบสถานะการเลือกตั้ง
        if (status != ElectionStatus.OPEN) {
            throw new IllegalStateException("ปฏิเสธ: ระบบไม่ได้อยู่ในสถานะเปิดรับคะแนน (OPEN)");
        }

        // 2. ตรวจสอบผู้มีสิทธิ์
        Voter voter = voters.get(voterId);
        if (voter == null || !voter.isActive()) {
            throw new IllegalArgumentException("ปฏิเสธ: ผู้มีสิทธิ์ไม่มีอยู่หรือไม่ได้ Active");
        }

        // 3. ตรวจสอบการลงคะแนนซ้ำ (1 ผู้มีสิทธิ์ลงได้ 1 ครั้ง)
        boolean hasVoted = ballots.stream().anyMatch(b -> b.getVoterId().equals(voterId));
        if (hasVoted) {
            throw new IllegalArgumentException("ปฏิเสธ: ผู้มีสิทธิ์เคยลงคะแนนแล้ว");
        }

        // 4. ตรวจสอบว่าเลือกผู้สมัคร 3 คน และไม่ซ้ำกัน
        if (r1 == null || r2 == null || r3 == null) {
            throw new IllegalArgumentException("ปฏิเสธ: ต้องเลือกผู้สมัครให้ครบ 3 อันดับ");
        }
        if (r1.equals(r2) || r1.equals(r3) || r2.equals(r3)) {
            throw new IllegalArgumentException("ปฏิเสธ: ห้ามเลือกผู้สมัครซ้ำกันในบัตรใบเดียวกัน");
        }
        if (!candidates.containsKey(r1) || !candidates.containsKey(r2) || !candidates.containsKey(r3)) {
            throw new IllegalArgumentException("ปฏิเสธ: พบรหัสผู้สมัครที่ไม่มีอยู่ในระบบ");
        }

        // บันทึกบัตรลงคะแนนใหม่
        String ballotId = String.format("B%02d", ballotCounter++);
        ballots.add(new Ballot(ballotId, voterId, List.of(r1, r2, r3)));
    }

    // --- R3: การปิดรับคะแนนและตรวจจับรูปแบบบัตรซ้ำ ---
    public void closeElection() {
        if (status != ElectionStatus.OPEN) {
            throw new IllegalStateException("ปฏิเสธ: สามารถปิดรับคะแนนได้เฉพาะสถานะ OPEN เท่านั้น");
        }
        status = ElectionStatus.CLOSED;

        // จัดกลุ่มตาม pattern อันดับ 1 > 2 > 3
        Map<String, List<Ballot>> grouped = new HashMap<>();
        for (Ballot b : ballots) {
            grouped.computeIfAbsent(b.getPatternKey(), k -> new ArrayList<>()).add(b);
        }

        patternGroups.clear();
        for (Map.Entry<String, List<Ballot>> entry : grouped.entrySet()) {
            String key = entry.getKey();
            List<Ballot> list = entry.getValue();

            if (list.size() >= threshold) {
                // ซ้ำตั้งแต่ 3 บัตรขึ้นไป -> รอตรวจสอบ (PENDING)
                PatternGroup group = new PatternGroup(key, BallotStatus.PENDING);
                list.forEach(group::addBallot);
                group.setStatus(BallotStatus.PENDING); // อัปเดตบัตรด้านในด้วย
                patternGroups.put(key, group);
            } else {
                // ซ้ำน้อยกว่า threshold -> รับรองแล้ว (APPROVED)
                PatternGroup group = new PatternGroup(key, BallotStatus.APPROVED);
                list.forEach(group::addBallot);
                group.setStatus(BallotStatus.APPROVED);
                patternGroups.put(key, group);
            }
        }
    }

    // --- R4: การตรวจกลุ่มบัตรและการสรุปผล ---
    public void decideGroup(String patternKey, boolean approve) {
        if (status != ElectionStatus.CLOSED) {
            throw new IllegalStateException("ปฏิเสธ: สามารถตัดสินกลุ่มได้เฉพาะในสถานะ CLOSED เท่านั้น");
        }

        PatternGroup group = patternGroups.get(patternKey);
        if (group == null || group.getStatus() != BallotStatus.PENDING) {
            throw new IllegalArgumentException("ปฏิเสธ: ไม่พบกลุ่ม หรือกลุ่มนี้ไม่ได้อยู่ในสถานะรอตรวจสอบ (PENDING)");
        }

        // เจ้าหน้าที่ตัดสินเป็น รับรอง (APPROVED) หรือ ไม่นับ (REJECTED)
        group.setStatus(approve ? BallotStatus.APPROVED : BallotStatus.REJECTED);

        // เช็คว่าตัดสินกลุ่ม PENDING หมดแล้วหรือยัง
        boolean anyPending = patternGroups.values().stream()
                .anyMatch(g -> g.getStatus() == BallotStatus.PENDING);

        if (!anyPending) {
            status = ElectionStatus.FINALIZED; // สรุปผลแล้วอัตโนมัติ
        }
    }

    // --- R4 & R5: คำนวณคะแนนรวมของผู้สมัคร ---
    public Map<String, Integer> calculateScores() {
        Map<String, Integer> scores = new HashMap<>();
        candidates.keySet().forEach(id -> scores.put(id, 0));

        // คำนวณคะแนนเฉพาะบัตรที่ได้รับการ APPROVED เท่านั้น (อันดับ 1=3, 2=2, 3=1 คะแนน)
        for (Ballot b : ballots) {
            if (b.getStatus() == BallotStatus.APPROVED) {
                List<String> rank = b.getRanking();
                scores.put(rank.get(0), scores.get(rank.get(0)) + 3);
                scores.put(rank.get(1), scores.get(rank.get(1)) + 2);
                scores.put(rank.get(2), scores.get(rank.get(2)) + 1);
            }
        }
        return scores;
    }

    // Helper ในการเพิ่มข้อมูลจาก Data Loader
    public void addCandidate(Candidate c) { candidates.put(c.getId(), c); }
    public void addVoter(Voter v) { voters.put(v.getId(), v); }
    public void addInitialBallot(Ballot b) { 
        ballots.add(b); 
        int idNum = Integer.parseInt(b.getId().replaceAll("\\D", ""));
        if (idNum >= ballotCounter) ballotCounter = idNum + 1;
    }

    // เพิ่มต่อท้ายใน ElectionModel.java
    public long getApprovedBallotCount() {
    return ballots.stream()
            .filter(b -> b.getStatus() == BallotStatus.APPROVED)
            .count();
}

    public long getRejectedBallotCount() {
    return ballots.stream()
            .filter(b -> b.getStatus() == BallotStatus.REJECTED)
            .count();
}
}
