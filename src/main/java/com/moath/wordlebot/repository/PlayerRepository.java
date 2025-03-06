//package com.moath.wordlebot.repository;
//
//
//import com.moath.wordlebot.model.Player;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.Optional;
//
//public interface PlayerRepository extends JpaRepository<Player, Long> {
//    Optional<Player> findByTelegramId(String telegramId);
//}