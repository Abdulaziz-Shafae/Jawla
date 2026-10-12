package com.jawla.service;
import com.jawla.api.ApiException;
import com.jawla.model.Member;
import com.jawla.repo.MemberRepository;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import java.util.Map;
@Service public class MemberService {
 private final MemberRepository repo;
 public MemberService(MemberRepository repo){this.repo=repo;}
 private String hash(String token){try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 public Map<String,Object> join(String name){if(name==null||name.isBlank()||name.length()>80)throw new ApiException("Enter a name (1-80 characters)");String token=UUID.randomUUID()+"-"+UUID.randomUUID();Member m=new Member();m.name=name.trim();m.deviceTokenHash=hash(token);repo.save(m);return Map.of("id",m.id,"name",m.name,"token",token);}
 public Member require(String token){if(token==null||token.isBlank())throw new ApiException("Member token required");String h=hash(token);return repo.findAll().stream().filter(m->m.deviceTokenHash.equals(h)).findFirst().orElseThrow(()->new ApiException("Invalid member token"));}
 public String name(Long id){return id==null?"—":repo.findById(id).map(m->m.name).orElse("—");}
}