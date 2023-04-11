package com.disura.rootcodematch.controller;

import com.disura.rootcodematch.error.match.MatchException;
import com.disura.rootcodematch.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AppResponse> getMatchSummary(@RequestParam("file") MultipartFile file) throws MatchException {
        try {
            return ResponseEntity.ok(new AppResponse(true, "Match data saved. Summary generated", matchService.getMatchSummary(file)));
        } catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/")
    public ResponseEntity<AppResponse> getAllMatches() throws MatchException {
        return ResponseEntity.ok(new AppResponse(true, "All Match Records", matchService.getAllMatches()));
    }



}
