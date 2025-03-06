//package com.moath.wordlebot.service;
//
//import com.moath.wordlebot.model.Player;
//import com.moath.wordlebot.repository.PlayerRepository;
//import com.moath.wordlebot.repository.WordleResultRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Optional;
//
//import com.moath.wordlebot.model.WordleResult;
//
//
//@Service
//@RequiredArgsConstructor
//public class WordleTrackerService {
//    private final PlayerRepository playerRepository;
//    private final WordleResultRepository resultRepository;
//    Constants constants = new Constants();
//
//    @Transactional
//    public boolean recordResult(String telegramId, String username, int tries) {
//        if (telegramId == null || telegramId.isEmpty()) {
//            throw new IllegalArgumentException("Telegram ID cannot be null or empty");
//        }
//
//        // Get or create the player
//        Player player = playerRepository.findByTelegramId(telegramId)
//                .orElseGet(() -> {
//                    Player newPlayer = new Player();
//                    newPlayer.setUsername(username);
//                    newPlayer.setTelegramId(telegramId);
//                    newPlayer.setScore(0);
//                    return playerRepository.save(newPlayer);
//                });
//
//        LocalDateTime now = LocalDateTime.now();
//
//        // Check if player already submitted today
//        List<WordleResult> existingResults = resultRepository.findAllByPlayerAndDateOnly(player, now);
//
//        if (!existingResults.isEmpty()) {
//            return false; // Already submitted
//        }
//
//        // Create new result with full timestamp
//        WordleResult result = new WordleResult();
//        result.setPlayer(player);
//        result.setDate(now);
//        result.setTries(tries);
//        result.setMissed(false);
//        resultRepository.save(result);
//
//        return true; // Successfully recorded
//    }
//
//    @Transactional
//    @Scheduled(cron = "55 59 23 * * *")
//    public void processEndOfDay() {
//        LocalDateTime now = LocalDateTime.now();
//
//        // Get today's results using date-only comparison
//        List<WordleResult> todayResults = resultRepository.findByDateOnly(now);
//        List<Player> allPlayers = playerRepository.findAll();
//
//        Optional<WordleResult> maxTries = todayResults.stream()
//                .max(Comparator.comparing(WordleResult::getTries));
//
//        for (Player player : allPlayers) {
//            boolean played = todayResults.stream()
//                    .anyMatch(r -> r.getPlayer().getId().equals(player.getId()));
//
//            if (!played) {
//                WordleResult missedResult = new WordleResult();
//                missedResult.setPlayer(player);
//                missedResult.setDate(now);
//                missedResult.setMissed(true);
//                missedResult.setTries(0);
//                resultRepository.save(missedResult);
//
//                player.setScore(player.getScore() + 2);
//                playerRepository.save(player);
//            } else if (maxTries.isPresent() &&
//                    todayResults.stream()
//                            .anyMatch(r -> r.getPlayer().getId().equals(player.getId()) &&
//                                    r.getTries().equals(maxTries.get().getTries()))) {
//                player.setScore(player.getScore() + 1);
//                playerRepository.save(player);
//            }
//        }
//    }
//
//    public String getLeaderboard() {
//        List<Player> players = playerRepository.findAll();
//        players.sort(Comparator.comparing(Player::getScore));
//
//        StringBuilder leaderboard = new StringBuilder("🏆 *Wordle Leaderboard* 🏆\n\n");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
//
//        for (int i = 0; i < players.size(); i++) {
//            Player player = players.get(i);
//
//            String lastPlayed = resultRepository.findLastPlayedByPlayer(player)
//                    .map(result -> result.getDate().format(formatter))
//                    .orElse("Never");
//
//            // Add player details in a neat format with additional spacing for readability
//            leaderboard.append(String.format("*%d.* %s\n", i + 1, player.getUsername()));
//            leaderboard.append(String.format("  🏅 Score: *%d* %s\n", player.getScore(), constants.SCORE_TITLE));
//            leaderboard.append(String.format("  🗓️ _Last played:_ %s\n", lastPlayed));
//
//            // Add a separator line if it's not the last player
//            if (players.size() > 1 && i < players.size() - 1) {
//                leaderboard.append("\n──────────────\n\n");
//            }
//        }
//
//        // Add final space for clarity
//        leaderboard.append("\n*End of Leaderboard*");
//
//        return leaderboard.toString();
//    }
//
//    public String getHelp() {
//        return "Available commands: \n/leaderboard\n/nafar";
//    }
//
//    public String getNafar() {
//        return constants.NAFAR;
//    }
//}