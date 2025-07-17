package com.ddd.demo.entity.board;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tbl_freeboard_001")
public class Freeboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    private String title;
    private String writer;
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp regDate;

    @UpdateTimestamp
    private Timestamp updateDate;

    @OneToMany(mappedBy = "freeBoard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<FreeBoardReply> replies = new ArrayList<>();
}
