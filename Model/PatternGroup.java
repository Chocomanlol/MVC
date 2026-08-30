package Model;
import java.util.ArrayList;
import java.util.List;

public class PatternGroup {
    private String patternKey; // เช่น "C01>C02>C03"
    private List<Ballot> ballots = new ArrayList<>();
    private BallotStatus status; // PENDING, APPROVED, REJECTED

    public PatternGroup(String patternKey, BallotStatus status) {
        this.patternKey = patternKey;
        this.status = status;
    }

    public void addBallot(Ballot b) { 
        ballots.add(b); 
    }

    public String getPatternKey() { 
        return patternKey; 
    }

    public List<Ballot> getBallots() { 
        return ballots; 
    }

    public BallotStatus getStatus() { 
        return status; 
    }

    public void setStatus(BallotStatus status) { 
        this.status = status;
        for (Ballot b : ballots) {
            b.setStatus(status); // อัปเดตสถานะของบัตรทุกใบในกลุ่มตามผลการตัดสิน (R4)
        }
    }
}
