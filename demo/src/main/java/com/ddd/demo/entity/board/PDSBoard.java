package com.ddd.demo.entity.board;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "tbl_pds_board_001")
@EqualsAndHashCode(of = "pid")
@Entity
@ToString(exclude = "files") // Avoid circular reference in toString
public class PDSBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;
    private String pName;
    private String pWriter;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PDSFile> files;
}

