package com.moath.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wordle_results")
public class WordleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Integer tries;

    @Column(nullable = false)
    private Boolean missed;

}