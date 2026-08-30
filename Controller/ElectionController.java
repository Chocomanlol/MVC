package Controller;

import java.util.Collection;
import java.util.List; 
import java.util.Map;

import Model.*; 

public class ElectionController {
    private final ElectionModel model;

    public ElectionController(ElectionModel model) {
        this.model = model;
    }

    // --- R2: จัดการการลงคะแนน ---
    public String castVote(String voterId, String rank1, String rank2, String rank3) {
        try {
            model.castVote(voterId, rank1, rank2, rank3);
            return "✅ สำเร็จ: บันทึกการลงคะแนนเรียบร้อยแล้ว";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "❌ " + e.getMessage();
        }
    }

    // --- R3: จัดการปิดรับคะแนน ---
    public String closeElection() {
        try {
            model.closeElection();
            return "✅ สำเร็จ: ปิดรับคะแนนและตรวจจับกลุ่มบัตรซ้ำเรียบร้อยแล้ว";
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        }
    }

    // --- R4: จัดการการตัดสินกลุ่มบัตรซ้ำโดยเจ้าหน้าที่ ---
    public String decideGroup(String patternKey, boolean approve) {
        try {
            model.decideGroup(patternKey, approve);
            String resultText = approve ? "รับรอง" : "ไม่นับคะแนน";
            return "✅ สำเร็จ: ตัดสินกลุ่ม [" + patternKey + "] เป็น \"" + resultText + "\" เรียบร้อยแล้ว";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "❌ " + e.getMessage();
        }
    }

    // --- R5 & Query Methods: ดึงข้อมูลส่งให้ View แสดงผล ---
    public ElectionStatus getElectionStatus() { 
        return model.getStatus(); 
    }

    public Collection<Candidate> getCandidates() { 
        return model.getCandidates(); 
    }

    public Collection<Voter> getVoters() { 
        return model.getVoters(); 
    }

    public Map<String, PatternGroup> getPatternGroups() { 
        return model.getPatternGroups(); 
    }

    public Map<String, Integer> getScores() { 
        return model.calculateScores(); 
    }

    public List<Ballot> getBallots() { 
        return model.getBallots(); 
    }

    // เพิ่มต่อท้ายใน ElectionController.java
    public long getApprovedBallotCount() { 
        return model.getApprovedBallotCount(); 
}

    public long getRejectedBallotCount() { 
        return model.getRejectedBallotCount(); 
}
}