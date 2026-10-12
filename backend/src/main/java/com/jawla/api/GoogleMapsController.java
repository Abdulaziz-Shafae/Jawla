package com.jawla.api;

import com.jawla.service.GoogleMapsService;
import com.jawla.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maps")
public class GoogleMapsController {
    private final GoogleMapsService googleMapsService;
    private final MemberService memberService;

    public GoogleMapsController(GoogleMapsService googleMapsService, MemberService memberService) {
        this.googleMapsService = googleMapsService;
        this.memberService = memberService;
    }

    public record GoogleMapsUrlDtoIn(@NotBlank String mapsUrl) {}

    @PostMapping("/resolve")
    public ResponseEntity<?> resolveGoogleMapsLocation(@RequestHeader("X-Member-Token") String memberToken, @Valid @RequestBody GoogleMapsUrlDtoIn googleMapsUrlDtoIn) {
        memberService.require(memberToken);
        return ResponseEntity.status(200).body(googleMapsService.resolveGoogleMapsLocation(googleMapsUrlDtoIn.mapsUrl()));
    }
}
