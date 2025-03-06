package com.moath.app.service;

public interface TrackerService {

    boolean recordResult(String telegramId, String username, int tries);

    void processEndOfDay();

    String getLeaderboard();

    String getHelp();

    String getNafar();
}
