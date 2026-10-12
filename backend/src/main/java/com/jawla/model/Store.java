package com.jawla.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity public class Store {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=150) public String name;
 @Column(length=500) public String instagramUrl;
 @Column(length=500) public String mapsUrl;
 @Column(length=500) public String imageUrl;
 @Column(length=500) public String websiteUrl;
 @Column(length=150) public String city;
 @Column(length=500) public String categories;
 public Double latitude;
 public Double longitude;
 @Column(nullable=false) public String status="UNVISITED";
 public Long addedBy;
 public Long statusUpdatedBy;
 public Instant statusUpdatedAt;
 public Instant createdAt=Instant.now();
 public Instant deletedAt;
 public Long deletedBy;
}