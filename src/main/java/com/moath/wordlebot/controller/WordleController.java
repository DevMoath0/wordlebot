package com.moath.wordlebot.controller;

import com.moath.wordlebot.dto.WordleResultDto;
import com.moath.wordlebot.service.WordleTrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/wordle")
@RequiredArgsConstructor
public class WordleController {
    private final WordleTrackerService wordleTrackerService;

    @PostMapping("/result")
    public ResponseEntity<WordleResultDto> recordResult(@RequestBody WordleResultDto result) {
        try {
            log.info("Recording result...");
            if(result.getTries() >= 1 && result.getTries() <= 6) {
                wordleTrackerService.recordResult(result.getTelegramId(),
                        result.getUsername(),
                        result.getTries());
                return ResponseEntity.ok(WordleResultDto.success("Result recorded successfully"));
            }else return ResponseEntity.ok(WordleResultDto.error("Result are smaller than 1 or bigger than 6"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(WordleResultDto.error(e.getMessage()));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<String> getLeaderboard() {
        return ResponseEntity.ok(wordleTrackerService.getLeaderboard());
    }

    @PostMapping("/process-daily")
    public ResponseEntity<WordleResultDto> processEndOfDay() {
        try {
            wordleTrackerService.processEndOfDay();
            return ResponseEntity.ok(WordleResultDto.success("Daily processing completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(WordleResultDto.error(e.getMessage()));
        }
    }

    @GetMapping("/help")
    public ResponseEntity<String> getHelp() {
        return ResponseEntity.ok(wordleTrackerService.getHelp());
    }

    @GetMapping("/nafar")
    public ResponseEntity<String> getNafar() {
        return ResponseEntity.ok(wordleTrackerService.getNafar());
    }
}
