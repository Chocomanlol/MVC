package Model;

import java.util.List;

public class Ballot {
    private String id;
    private String voterId;
    private List<String> ranking; // [Rank1, Rank2, Rank3]
    private BallotStatus status;

    public Ballot(String id, String voterId, List<String> ranking) {
        this.id = id;
        this.voterId = voterId;
        this.ranking = ranking;
        this.status = BallotStatus.APPROVED; // เริ่มต้นถือว่ารับรองชั่วคราว จนกว่าจะสั่งปิดรับคะแนนแล้วสแกน pattern
    }

    public String getId() { 
        return id; 
    }

    public String getVoterId() { 
        return voterId; 
    }

    public List<String> getRanking() { 
        return ranking; 
    }

    public BallotStatus getStatus() { 
        return status; 
    }

    public void setStatus(BallotStatus status) { 
        this.status = status; 
    }

    // คืนค่ารูปแบบ เช่น "C01>C02>C03" ไว้ใช้เปรียบเทียบซ้ำ
    public String getPatternKey() {
        return String.join(">", ranking);
    }
}
