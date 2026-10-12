package com.jawla.api;

import com.jawla.service.GeoapifyService;
import com.jawla.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
public class GeoapifyController {
    private final GeoapifyService geoapifyService;
    private final MemberService memberService;

    public GeoapifyController(GeoapifyService geoapifyService, MemberService memberService) {
        this.geoapifyService = geoapifyService;
        this.memberService = memberService;
    }

    public record SearchStoreLocationDtoIn(@NotBlank String name, String city) {}

    @PostMapping("/search")
    public ResponseEntity<?> searchFurnitureStoreLocations(@RequestHeader("X-Member-Token") String memberToken, @Valid @RequestBody SearchStoreLocationDtoIn searchStoreLocationDtoIn) {
        memberService.require(memberToken);
        return ResponseEntity.status(200).body(geoapifyService.searchFurnitureStoreLocations(searchStoreLocationDtoIn.name(), searchStoreLocationDtoIn.city()));
    }
}
