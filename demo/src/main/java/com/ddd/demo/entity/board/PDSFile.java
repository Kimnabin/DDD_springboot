package com.ddd.demo.entity.board;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "tbl_pds_file_001")
@EqualsAndHashCode(of = "fno")
public class PDSFile {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long fno;
    private String pdsFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pds_no")
    private PDSBoard board;
}

