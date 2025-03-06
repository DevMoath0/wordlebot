//package com.moath.wordlebot.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Data
//@Entity
//@Table(name = "players")
//public class Player {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String username;
//
//    @Column(nullable = false)
//    private Integer score;
//
//    @Column(name = "telegram_id", nullable = false, unique = true)
//    private String telegramId;
//}