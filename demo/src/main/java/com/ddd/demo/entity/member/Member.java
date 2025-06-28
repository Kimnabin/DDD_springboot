package com.ddd.demo.entity.member;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "java_member_001")
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    private String id;

    @Column(name = "member_name", nullable = false, length = 255, columnDefinition = "varchar(255) comment 'member name'")
    private String name;

    @Column(name = "member_password", nullable = false, length = 255, columnDefinition = "varchar(255) comment 'member password'")
    private String pwd;
}
