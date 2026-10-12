package com.jawla.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"store_id","member_id"})) public class Review {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="store_id",nullable=false) public Long storeId;
 @Column(name="member_id",nullable=false) public Long memberId;
 @Column(nullable=false) public Integer stars;
 @Column(length=1000) public String note;
 public Instant updatedAt=Instant.now();
}