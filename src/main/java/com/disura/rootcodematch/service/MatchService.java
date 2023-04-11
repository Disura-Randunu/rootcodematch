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

    public Map<String, Object> getMatchSummary(MultipartFile file) throws MatchException {

        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

            Map<Integer, String> teamNames = new HashMap<>();
            Map<Integer, Integer> scores = new HashMap<>();
            Map<Integer, Double> overs = new HashMap<>();

            Map<String, Integer> runs = new HashMap<>();
            Map<String, Integer> wickets = new HashMap<>();

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

//                saveMatch(match);

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

            double team1Overs = overs.get(1);
            double team2Overs = overs.get(2);

            System.out.println("============== Match Summary ==============");
            System.out.println("Winning team: " + winningTeam);
            System.out.println();
            System.out.println("Scores:");
            System.out.println(teamNames.get(1) + ": " + team1Score + "/" + wickets.get(teamNames.get(1)) + " (" + String.format("%.1f", team1Overs) + " overs)");
            System.out.println(teamNames.get(2) + ": " + team2Score + "/" + wickets.get(teamNames.get(2)) + " (" + String.format("%.1f", team2Overs) + " overs)");
            System.out.println();
            System.out.println("Player with most runs: " + getMostRunsPlayer(runs));
            System.out.println("Player with most wickets: " + getMostWicketsPlayer(wickets));
            System.out.println("===========================================");

            Map<String, Object> scores_summary = new HashMap<>();
            scores_summary.put(teamNames.get(1), team1Score + "/" + wickets.get(teamNames.get(1)) + " (" + String.format("%.1f", team1Overs) + " overs)");
            scores_summary.put(teamNames.get(2), team2Score + "/" + wickets.get(teamNames.get(2)) + " (" + String.format("%.1f", team1Overs) + " overs)");
            Map<String, Object> summary = new HashMap<>();

            summary.put("winning_team", winningTeam);
            summary.put("scores_summary", scores_summary);
            summary.put("most_runs_player", getMostRunsPlayer(runs));
            summary.put("most_wickets_player", getMostWicketsPlayer(wickets));

            return summary;

        } catch (Exception e) {
            throw new MatchException(e);
        }

    }

    private static String getMostRunsPlayer(Map<String, Integer> runs) {
        int maxRuns = 0;
        String mostRunsPlayer = "";
        for (Map.Entry<String, Integer> entry : runs.entrySet()) {
            if (entry.getValue() > maxRuns) {
                maxRuns = entry.getValue();
                mostRunsPlayer = entry.getKey();
            }
        }
        return mostRunsPlayer;
    }

    private static String getMostWicketsPlayer(Map<String, Integer> wickets) {
        int maxWickets = 0;
        String mostWicketsPlayer = "";
        for (Map.Entry<String, Integer> entry : wickets.entrySet()) {
            if (entry.getValue() > maxWickets) {
                maxWickets = entry.getValue();
                mostWicketsPlayer = entry.getKey();
            }
        }
        return mostWicketsPlayer;
    }


    public void saveMatch(Match match) {
        matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        log.info("Getting all swimmers");
        return matchRepository.findAll();
    }
}
