package com.disura.rootcodematch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "inning")
    private Integer inning;

    @Column(name = "over")
    private Integer over;

    @Column(name = "ball")
    private Integer ball;

    @Column(name = "batting_team")
    private String batting_team;

    @Column(name = "batsman")
    private String batsman;

    @Column(name = "non_striker")
    private String non_striker;

    @Column(name = "bowler")
    private String bowler;

    @Column(name = "runs_off_bat")
    private Integer runs_off_bat;

    @Column(name = "extras")
    private Integer extras;

    @Column(name = "wides")
    private Integer wides;

    @Column(name = "no_balls")
    private Integer no_balls;

    @Column(name = "byes")
    private Integer byes;

    @Column(name = "leg_byes")
    private Integer leg_byes;

    @Column(name = "wicket_type")
    private String wicket_type;

    @Column(name = "dismissed_played")
    private String dismissed_played;
}