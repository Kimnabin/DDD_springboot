package com.ddd.demo.entity.board;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "tbl_free_replies_001")
public class FreeBoardReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno; // Reply ID

    @Column(nullable = false, length = 255)
    private String replyText; // Nội dung trả lời

    private String replier; // Người trả lời

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp replyDate; // Thời gian tạo

    @UpdateTimestamp
    private Timestamp updateDate; // Thời gian cập nhật

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bno", nullable = false)
    @ToString.Exclude
    private Freeboard freeBoard; // Quan hệ N-1 với Freeboard
}

