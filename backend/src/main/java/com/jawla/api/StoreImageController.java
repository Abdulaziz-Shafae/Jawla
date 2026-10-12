package com.jawla.api;

import com.jawla.service.MemberService;
import com.jawla.service.StoreImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-images")
public class StoreImageController {
    private final StoreImageService images;
    private final MemberService members;

    public StoreImageController(StoreImageService images, MemberService members) {
        this.images = images;
        this.members = members;
    }

    public record StoreImageRequest(String websiteUrl, String instagramUrl) {}

    @PostMapping("/find")
    public ResponseEntity<?> findStoreImage(@RequestHeader("X-Member-Token") String token, @RequestBody StoreImageRequest input) {
        members.require(token);
        return ResponseEntity.status(200).body(images.findStoreImage(input.websiteUrl(), input.instagramUrl()));
    }
}
