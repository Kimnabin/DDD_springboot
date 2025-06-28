package com.ddd.demo.entity.member;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "java_profile_001")
@EqualsAndHashCode(of = "fno")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fno;

    @Column(name = "profile_name", nullable = false, length = 255, columnDefinition = "varchar(255) comment 'profile name'")
    private String fname;

    @Column(name = "current", nullable = false, columnDefinition = "tinyint(1) comment 'current profile flag'")
    private boolean current;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false) // Foreign key to Member entity
    private Member member;
}
