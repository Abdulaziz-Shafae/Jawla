package com.jawla.api;

import com.jawla.model.Store;
import com.jawla.model.StoreImage;
import com.jawla.repo.StoreRepository;
import com.jawla.repo.StoreImageRepository;
import com.jawla.service.MemberService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/stores/{storeId}/image")
public class StoreImageUploadController {
    private static final long MAX_BYTES = 3 * 1024 * 1024;
    private static final Set<String> TYPES = Set.of("image/png","image/jpeg","image/webp");
    private final StoreRepository stores;
    private final StoreImageRepository images;
    private final MemberService members;

    public StoreImageUploadController(StoreRepository stores, StoreImageRepository images, MemberService members) {
        this.stores=stores; this.images=images; this.members=members;
    }

    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@PathVariable Long storeId,@RequestHeader("X-Member-Token") String token,@RequestPart("image") MultipartFile file) throws IOException {
        members.require(token);
        Store store=stores.findById(storeId).orElseThrow(()->new ApiException("Store not found"));
        if(store.deletedAt != null) throw new ApiException("Cannot update an item in trash");
        if(file.isEmpty() || file.getSize()>MAX_BYTES) throw new ApiException("Image must be under 3 MB");
        String type=file.getContentType();
        if(type==null || !TYPES.contains(type)) throw new ApiException("Only JPEG, PNG and WebP images are supported");
        byte[] data=file.getBytes();
        if(!validSignature(data,type)) throw new ApiException("Invalid image content");
        StoreImage image=new StoreImage();
        image.storeId=storeId;
        image.contentType=type;
        image.imageData=data;
        images.save(image);
        return ResponseEntity.ok(Map.of("message","Image uploaded","hasImage",true));
    }

    @GetMapping
    public ResponseEntity<byte[]> get(@PathVariable Long storeId) {
        Store store=stores.findById(storeId).orElseThrow(()->new ApiException("Store not found"));
        if(store.deletedAt!=null) return ResponseEntity.notFound().build();
        StoreImage image=images.findById(storeId).orElse(null);
        if(image==null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType))
          .cacheControl(CacheControl.noStore()).body(image.imageData);
    }

    @DeleteMapping
    public ResponseEntity<?> remove(@PathVariable Long storeId,@RequestHeader("X-Member-Token") String token) {
        members.require(token);
        Store store=stores.findById(storeId).orElseThrow(()->new ApiException("Store not found"));
        if(store.deletedAt!=null) throw new ApiException("Cannot update an item in trash");
        images.deleteById(storeId);
        return ResponseEntity.ok(Map.of("hasImage",false));
    }

    private boolean validSignature(byte[] bytes, String type) {
        if("image/jpeg".equals(type)) return bytes.length>3 && (bytes[0]&255)==255 && (bytes[1]&255)==216 && (bytes[2]&255)==255;
        if("image/png".equals(type)) return bytes.length>8 && (bytes[0]&255)==137 && bytes[1]==80 && bytes[2]==78 && bytes[3]==71;
        if("image/webp".equals(type)) return bytes.length>12 && new String(bytes,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(bytes,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
        return false;
    }
}
