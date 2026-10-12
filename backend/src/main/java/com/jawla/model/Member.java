package com.jawla.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity public class Member {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=80) public String name;
 @Column(nullable=false,unique=true,length=64) public String deviceTokenHash;
 public Instant createdAt=Instant.now();
}