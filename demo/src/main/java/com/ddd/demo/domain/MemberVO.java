package com.ddd.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class MemberVO {
    private int mno;
    private String mid;
    private String mpw;
    private String mname;
    private Timestamp regdate;
    private boolean active = true; // Default active status

    // Constructor với active status
    public MemberVO(int mno, String mid, String mpw, String mname, Timestamp regdate, boolean active) {
        this.mno = mno;
        this.mid = mid;
        this.mpw = mpw;
        this.mname = mname;
        this.regdate = regdate;
        this.active = active;
    }

    // Original constructor
    public MemberVO(int mno, String mid, String mpw, String mname, Timestamp regdate) {
        this(mno, mid, mpw, mname, regdate, true);
    }
}