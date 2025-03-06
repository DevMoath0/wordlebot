package com.moath.app.repository;

import com.moath.app.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByTelegramId(String telegramId);
}
