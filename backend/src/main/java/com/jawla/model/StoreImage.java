package com.jawla.model;
import jakarta.persistence.*;
@Entity
@Table(name="store_image")
public class StoreImage {
 @Id public Long storeId;
 @Lob @Column(name="image_data",nullable=false,columnDefinition="LONGBLOB") public byte[] imageData;
 @Column(nullable=false,length=20) public String contentType;
}
