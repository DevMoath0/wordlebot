//package com.moath.wordlebot.dto;
//
//import lombok.Data;
//
//@Data
//public class WordleResultDto {
//    private String telegramId;
//    private String username;
//    private Integer tries;
//    private String message;
//    private boolean success;
//
//    public static WordleResultDto success(String message) {
//        WordleResultDto response = new WordleResultDto();
//        response.setSuccess(true);
//        response.setMessage(message);
//        return response;
//    }
//
//    public static WordleResultDto error(String message) {
//        WordleResultDto response = new WordleResultDto();
//        response.setSuccess(false);
//        response.setMessage(message);
//        return response;
//    }
//}