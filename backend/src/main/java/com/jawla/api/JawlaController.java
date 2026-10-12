package com.jawla.api;
import com.jawla.model.*;
import com.jawla.repo.*;
import com.jawla.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController @RequestMapping("/api") public class JawlaController {
 private final StoreRepository stores; private final ReviewRepository reviews; private final MemberService members; private final StoreImageRepository images;
 public JawlaController(StoreRepository s,ReviewRepository r,MemberService m,StoreImageRepository images){stores=s;reviews=r;members=m;this.images=images;}
 public record JoinInput(@NotBlank String name){}
 public record StoreInput(@NotBlank String name,String instagramUrl,String mapsUrl,String imageUrl,String websiteUrl,String city,String categories,Double latitude,Double longitude){}
 public record ReviewInput(@Min(1) @Max(5) int stars,String note){}
 public record StatusInput(String status){}
 @PostMapping("/members") public Object join(@Valid @RequestBody JoinInput input){return members.join(input.name());}
 private Member actor(String token){return members.require(token);}
 private Store store(Long id){return stores.findById(id).orElseThrow(()->new ApiException("Store not found"));}
 private void active(Store s){if(s.deletedAt!=null)throw new ApiException("Store is in trash");}
 private void assign(Store s,StoreInput i){if(i.name()==null||i.name().isBlank()||i.name().length()>150)throw new ApiException("Invalid store name");if(i.latitude()!=null&&(i.latitude() < -90||i.latitude()>90))throw new ApiException("Invalid latitude");if(i.longitude()!=null&&(i.longitude() < -180||i.longitude()>180))throw new ApiException("Invalid longitude");s.name=i.name().trim();s.instagramUrl=i.instagramUrl();s.mapsUrl=i.mapsUrl();s.imageUrl=i.imageUrl();s.websiteUrl=i.websiteUrl();s.city=i.city();s.categories=i.categories();s.latitude=i.latitude();s.longitude=i.longitude();}
 private Object output(Store s){var rs=reviews.findAll().stream().filter(r->r.storeId.equals(s.id)).toList();Map<String,Object> o=new LinkedHashMap<>();o.put("id",s.id);o.put("name",s.name);o.put("instagramUrl",s.instagramUrl);o.put("mapsUrl",s.mapsUrl);o.put("imageUrl",s.imageUrl);o.put("hasImage",images.existsById(s.id));o.put("websiteUrl",s.websiteUrl);o.put("city",s.city);o.put("categories",s.categories);o.put("latitude",s.latitude);o.put("longitude",s.longitude);o.put("status",s.status);o.put("addedByName",members.name(s.addedBy));o.put("statusUpdatedByName",members.name(s.statusUpdatedBy));o.put("statusUpdatedAt",s.statusUpdatedAt);o.put("createdAt",s.createdAt);o.put("deletedAt",s.deletedAt);o.put("deletedByName",members.name(s.deletedBy));o.put("rating",rs.isEmpty()?null:rs.stream().mapToInt(r->r.stars).average().orElse(0));o.put("reviewCount",rs.size());o.put("reviews",rs.stream().map(r->Map.of("memberId",r.memberId,"memberName",members.name(r.memberId),"stars",r.stars,"note",r.note==null?"":r.note)).toList());return o;}
 @GetMapping("/stores") public Object list(@RequestHeader("X-Member-Token") String token){actor(token);return stores.findAll().stream().filter(s->s.deletedAt==null).sorted(Comparator.comparing((Store s)->s.createdAt).reversed()).map(this::output).toList();}
 @GetMapping("/stores/trash") public Object trash(@RequestHeader("X-Member-Token") String token){actor(token);return stores.findAll().stream().filter(s->s.deletedAt!=null).map(this::output).toList();}
 @PostMapping("/stores") public Object create(@RequestHeader("X-Member-Token") String token,@Valid @RequestBody StoreInput i){Member m=actor(token);Store s=new Store();assign(s,i);s.addedBy=m.id;return output(stores.save(s));}
 @PutMapping("/stores/{id}") public Object update(@RequestHeader("X-Member-Token") String token,@PathVariable Long id,@Valid @RequestBody StoreInput i){actor(token);Store s=store(id);active(s);assign(s,i);return output(stores.save(s));}
 @PutMapping("/stores/{id}/status") public Object status(@RequestHeader("X-Member-Token") String token,@PathVariable Long id,@RequestBody StatusInput i){Member m=actor(token);Store s=store(id);active(s);if(i.status()==null||!List.of("UNVISITED","VISITED","SKIPPED").contains(i.status()))throw new ApiException("Invalid status");s.status=i.status();s.statusUpdatedBy=m.id;s.statusUpdatedAt=Instant.now();return output(stores.save(s));}
 @DeleteMapping("/stores/{id}") public Object delete(@RequestHeader("X-Member-Token") String token,@PathVariable Long id){Member m=actor(token);Store s=store(id);active(s);s.deletedAt=Instant.now();s.deletedBy=m.id;stores.save(s);return Map.of("message","Moved to trash");}
 @PostMapping("/stores/{id}/restore") public Object restore(@RequestHeader("X-Member-Token") String token,@PathVariable Long id){actor(token);Store s=store(id);s.deletedAt=null;s.deletedBy=null;return output(stores.save(s));}
 @PutMapping("/stores/{id}/review") public Object review(@RequestHeader("X-Member-Token") String token,@PathVariable Long id,@Valid @RequestBody ReviewInput i){Member m=actor(token);Store s=store(id);active(s);Review r=reviews.findAll().stream().filter(x->x.storeId.equals(id)&&x.memberId.equals(m.id)).findFirst().orElseGet(Review::new);r.storeId=id;r.memberId=m.id;r.stars=i.stars();r.note=i.note();r.updatedAt=Instant.now();reviews.save(r);return output(s);}
}