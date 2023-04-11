package com.disura.rootcodematch.service;

import com.disura.rootcodematch.entity.Match;
import com.disura.rootcodematch.error.match.MatchException;
import com.disura.rootcodematch.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }


    public Map<String, Object> summary(MultipartFile file) throws IOException {

        Reader reader = new InputStreamReader(file.getInputStream());
        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

        for (CSVRecord record : parser) {

            int innings = Integer.parseInt(record.get("Innings number"));
            String[] overAndBall = record.get("Over and ball").split("\\.");
            int over = Integer.parseInt(overAndBall[0]);
            int ball = Integer.parseInt(overAndBall[1]);
            String teamName = record.get("Batting team name");
            String batsman = record.get("Batsman");
            String nonStriker = record.get("Non-striker");
            String bowler = record.get("Bowler");

            int runsOffBat = (record.get("Runs-off-bat").equals("")) ? 0 : Integer.parseInt(record.get("Runs-off-bat"));
            int extras = (record.get("Extras").equals("")) ? 0 : Integer.parseInt(record.get("Extras"));
            int wides = (record.get("Wides").equals("")) ? 0 : Integer.parseInt(record.get("Wides"));
            int noBalls = (record.get("No-balls").equals("")) ? 0 : Integer.parseInt(record.get("No-balls"));
            int byes = (record.get("Byes").equals("")) ? 0 : Integer.parseInt(record.get("Byes"));
            int legByes = (record.get("Leg-byes").equals("")) ? 0 : Integer.parseInt(record.get("Leg-byes"));

            String kindOfWicket = record.get("Kind of wicket, if any");
            String dismissedPlayed = record.get("Dismissed played, if there was a wicket");

            Match match = Match.builder()
                    .inning(innings)
                    .over(over)
                    .ball(ball)
                    .batting_team(teamName)
                    .batsman(batsman)
                    .non_striker(nonStriker)
                    .bowler(bowler)
                    .runs_off_bat(runsOffBat)
                    .extras(extras)
                    .wides(wides)
                    .no_balls(noBalls)
                    .byes(byes)
                    .leg_byes(legByes)
                    .wicket_type(kindOfWicket)
                    .dismissed_played(dismissedPlayed).build();

            saveMatch(match);

            Map<Integer, String> teamNames = new HashMap<>();
            Map<Integer, Integer> scores = new HashMap<>();
            Map<Integer, Double> overs = new HashMap<>();

            Map<String, Integer> runs = new HashMap<>();
            Map<String, Integer> wickets = new HashMap<>();

            teamNames.put(innings, teamName);
            int inningScore = scores.getOrDefault(innings, 0);
            scores.put(innings, inningScore + runsOffBat + extras);
            double inningOvers = overs.getOrDefault(innings, 0.0);
            overs.put(innings, inningOvers + (ball / 6.0) + (over - Math.floor(over)));

            runs.put(batsman, runs.getOrDefault(batsman, 0) + runsOffBat);
            runs.put(nonStriker, runs.getOrDefault(nonStriker, 0));
            wickets.put(bowler, wickets.getOrDefault(bowler, 0));
            if (!kindOfWicket.equals("")) {
                wickets.put(bowler, wickets.getOrDefault(bowler, 0) + 1);
            }
        }


        int team1Score = scores.get(1);
        int team2Score = scores.get(2);
        String winningTeam = (team1Score > team2Score) ? teamNames.get(1) : teamNames.get(2);
        int winningScore = Math.max(team1Score, team2Score);
        int losingScore = Math.min(team1Score, team2Score);

        double team1Overs = overs.get(1);
        double team2Overs = overs.get(2);

        System.out.println("Winning team: " + winningTeam);
        System.out.println("Scores:");
        System.out.println(teamNames.get(1) + ": " + team1Score + "/" + wickets.getOrDefault(teamNames.get(1), 0) + " (" + String.format("%.1f", team1Overs) + " overs)");
        System.out.println(teamNames.get(2) + ": " + team2Score + "/" + wickets.getOrDefault(teamNames.get(2), 0) + " (" + String.format("%.1f", team2Overs) + " overs)");
        System.out.println("Player with most runs: " + getPlayerWithMostRuns(runs));
        System.out.println("Player with most wickets: " + getPlayerWithMostWickets(wickets));


        return null;
    }

    private static String getPlayerWithMostRuns(Map<String, Integer> runs) {
        int maxRuns = 0;
        String playerWithMostRuns = "";
        for (Map.Entry<String, Integer> entry : runs.entrySet()) {
            if (entry.getValue() > maxRuns) {
                maxRuns = entry.getValue();
                playerWithMostRuns = entry.getKey();
            }
        }
        return playerWithMostRuns;
    }

    private static String getPlayerWithMostWickets(Map<String, Integer> wickets) {
        int maxWickets = 0;
        String playerWithMostWickets = "";
        for (Map.Entry<String, Integer> entry : wickets.entrySet()) {
            if (entry.getValue() > maxWickets) {
                maxWickets = entry.getValue();
                playerWithMostWickets = entry.getKey();
            }
        }
        return playerWithMostWickets;
    }

    public Map<String, Object> getMatchSummary(MultipartFile file) throws MatchException {

        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

            Map<String, Integer> teamScores = new HashMap<>();
            Map<String, Integer> teamOvers = new HashMap<>();
            Map<String, Integer> playerRuns = new HashMap<>();
            Map<String, Integer> playerWickets = new HashMap<>();
            String winningTeam = "";
            int highestScore = 0;
            int highestWickets = 0;

            for (CSVRecord record : parser) {

                int runsOffBat = Integer.parseInt(record.get("Runs-off-bat"));
                int extras = Integer.parseInt(record.get("Extras"));
                int wideRuns = (record.get("Wides").equals("")) ? 0 : Integer.parseInt(record.get("Wides"));
                int noBallRuns = (record.get("No-balls").equals("")) ? 0 : Integer.parseInt(record.get("No-balls"));
                int byeRuns = (record.get("Byes").equals("")) ? 0 : Integer.parseInt(record.get("Byes"));
                int legByeRuns = (record.get("Leg-byes").equals("")) ? 0 : Integer.parseInt(record.get("Leg-byes"));
                String wicketType = record.get("Kind of wicket, if any");
//            String dismissedPlayer = record.get("Dismissed player, if there was a wicket");
                String battingTeam = record.get("Batting team name");
                String batsman = record.get("Batsman");
//            String nonStriker = record.get("Non-striker");
                String bowler = record.get("Bowler");
                int totalRuns = runsOffBat + extras + wideRuns + noBallRuns + byeRuns + legByeRuns;
                String currentTeam = battingTeam;

                int currentTeamScore = teamScores.getOrDefault(currentTeam, 0) + totalRuns;
                teamScores.put(currentTeam, currentTeamScore);

                int currentOvers = teamOvers.getOrDefault(currentTeam, 0);
                double overBallNum = Double.parseDouble(record.get("Over and ball"));
                int intPart = (int) overBallNum;
                int decPart = (int) ((overBallNum - intPart) * 10);
                int currentBalls = intPart * 6 + decPart;
                if (currentBalls % 6 == 0) {
                    currentOvers++;
                }
                teamOvers.put(currentTeam, currentOvers);

                int currentRuns = playerRuns.getOrDefault(batsman, 0) + runsOffBat;
                playerRuns.put(batsman, currentRuns);

                if (!wicketType.equals("")) {
                    int currentWickets = playerWickets.getOrDefault(bowler, 0) + 1;
                    playerWickets.put(bowler, currentWickets);
                }

                if (currentTeamScore > highestScore) {
                    winningTeam = currentTeam;
                    highestScore = currentTeamScore;
                }

                if (playerWickets.containsKey(bowler) && playerWickets.get(bowler) > highestWickets) {
                    highestWickets = playerWickets.get(bowler);
                }
            }

            String highestRunScorer = "";
            int highestRuns = 0;
            for (Map.Entry<String, Integer> entry : playerRuns.entrySet()) {
                if (entry.getValue() > highestRuns) {
                    highestRuns = entry.getValue();
                    highestRunScorer = entry.getKey();
                }
            }

            Map<String, Object> scores = new HashMap<>();
            Map<String, Object> overs = new HashMap<>();
            Map<String, Object> summary = new HashMap<>();

            summary.put("winning_team", winningTeam);
            summary.put("scores", teamScores);
            summary.put("overs", teamOvers);
            summary.put("most_runs_player", highestRunScorer);
            summary.put("most_wickets_player", highestWickets);

            return summary;

        } catch (Exception e) {
            throw new MatchException(e);
        }

    }

    public void saveMatch(Match match) {
        matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        log.info("Getting all swimmers");
        return matchRepository.findAll();
    }
}
