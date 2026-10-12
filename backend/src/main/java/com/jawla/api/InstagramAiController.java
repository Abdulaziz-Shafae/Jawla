package com.jawla.api;

import com.jawla.service.InstagramAiService;
import com.jawla.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/instagram")
public class InstagramAiController {
    private final InstagramAiService instagramAiService;
    private final MemberService memberService;

    public InstagramAiController(InstagramAiService instagramAiService, MemberService memberService) {
        this.instagramAiService = instagramAiService;
        this.memberService = memberService;
    }

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<?> analyzeInstagramScreenshot(@RequestHeader("X-Member-Token") String memberToken, @RequestParam("instagramUrl") String instagramUrl, @RequestPart("image") MultipartFile image) {
        memberService.require(memberToken);
        return ResponseEntity.status(200).body(instagramAiService.analyzeInstagramScreenshot(instagramUrl, image));
    }
}
