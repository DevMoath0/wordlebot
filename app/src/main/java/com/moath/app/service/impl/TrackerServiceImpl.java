package com.moath.app.service.impl;

import com.moath.app.model.Player;
import com.moath.app.model.WordleResult;
import com.moath.app.repository.PlayerRepository;
import com.moath.app.repository.WordleResultRepository;
import com.moath.app.service.TrackerService;
import com.moath.common.constant.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackerServiceImpl implements TrackerService {

    private final PlayerRepository playerRepository;
    private final WordleResultRepository wordleResultRepository;

    Constant constants;
    LocalDateTime now = LocalDateTime.now();

    @Override
    public boolean recordResult(String telegramId, String username, int tries) {
        if(telegramId == null || telegramId.isEmpty()) {
            throw new IllegalArgumentException("telegramId is null or empty");
        }

        Player player = playerRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    Player newPlayer = new Player();
                    newPlayer.setUsername(username);
                    newPlayer.setTelegramId(telegramId);
                    newPlayer.setScore(0);
                    return playerRepository.save(newPlayer);
                });

        List<WordleResult> existingResults = wordleResultRepository.findAllByPlayerAndDateOnly(player, now);

        if(existingResults.isEmpty()) {
            return false;
        }

        WordleResult wordleResult = new WordleResult();
        wordleResult.setPlayer(player);
        wordleResult.setDate(now);
        wordleResult.setTries(tries);
        wordleResultRepository.save(wordleResult);

        return true;
    }

    @Transactional
    @Scheduled(cron = "55 59 23 * * *")
    @Override
    public void processEndOfDay() {
        List<WordleResult> todayResult = wordleResultRepository.findByDateOnly(now);
        List<Player> players = playerRepository.findAll();

        Optional<WordleResult> maxTries = todayResult.stream().max(Comparator.comparing(WordleResult::getTries));

        for(Player player : players) {
            boolean played = todayResult.stream()
                    .anyMatch(r -> r.getPlayer().equals(player.getId()));

            if (!played) {
                WordleResult missedResult = new WordleResult();
                missedResult.setPlayer(player);
                missedResult.setDate(now);
                missedResult.setMissed(true);
                missedResult.setTries(0);
                wordleResultRepository.save(missedResult);

                player.setScore(player.getScore() + 2);
                playerRepository.save(player);
            } else if (maxTries.isPresent() &&
                    todayResult.stream()
                            .anyMatch(r -> r.getPlayer().getId().equals(player.getId()) &&
                                    r.getTries().equals(maxTries.get().getTries()))) {
                player.setScore(player.getScore() + 1);
                playerRepository.save(player);
            }
        }
    }

    @Override
    public String getLeaderboard() {
        List<Player> players = playerRepository.findAll();
        players.sort(Comparator.comparing(Player::getScore));

        StringBuilder leaderboard = new StringBuilder("🏆 *Wordle Leaderboard* 🏆\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            String lastPlayed = wordleResultRepository.findLastPlayedByPlayer(player)
                    .map(result -> result.getDate().format(formatter))
                    .orElse("Never");

            // Add player details in a neat format with additional spacing for readability
            leaderboard.append(String.format("*%d.* %s\n", i + 1, player.getUsername()));
            leaderboard.append(String.format("  🏅 Score: *%d* %s\n", player.getScore(), Constant.SCORE_TITLE.getString()));
            leaderboard.append(String.format("  🗓️ _Last played:_ %s\n", lastPlayed));

            // Add a separator line if it's not the last player
            if (players.size() > 1 && i < players.size() - 1) {
                leaderboard.append("\n──────────────\n\n");
            }
        }

        // Add final space for clarity
        leaderboard.append("\n*End of Leaderboard*");

        return leaderboard.toString();
    }

    @Override
    public String getHelp() {
        return "Available commands: \n/leaderboard\n/nafar";
    }

    @Override
    public String getNafar() {
        return Constant.NAFAR.getString();
    }
}
