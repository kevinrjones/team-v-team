/*
 * This file is generated by jOOQ.
 */
package com.knowledgespike.db.indexes


import com.knowledgespike.db.tables.Battingdetails
import com.knowledgespike.db.tables.Bowlingdetails
import com.knowledgespike.db.tables.Closeofplay
import com.knowledgespike.db.tables.Competitions
import com.knowledgespike.db.tables.Countries
import com.knowledgespike.db.tables.Extramatchdetails
import com.knowledgespike.db.tables.Fallofwickets
import com.knowledgespike.db.tables.Fielding
import com.knowledgespike.db.tables.Grounds
import com.knowledgespike.db.tables.Groundsmatchtypes
import com.knowledgespike.db.tables.Groundsname
import com.knowledgespike.db.tables.Innings
import com.knowledgespike.db.tables.Matches
import com.knowledgespike.db.tables.Matchreferees
import com.knowledgespike.db.tables.Matchrefereesmatches
import com.knowledgespike.db.tables.Matchsubtype
import com.knowledgespike.db.tables.Notes
import com.knowledgespike.db.tables.Partnerships
import com.knowledgespike.db.tables.Partnershipsplayers
import com.knowledgespike.db.tables.Players
import com.knowledgespike.db.tables.Playersmatches
import com.knowledgespike.db.tables.Playersofthematchmatches
import com.knowledgespike.db.tables.Playersteams
import com.knowledgespike.db.tables.Reserveumpires
import com.knowledgespike.db.tables.Reserveumpiresmatches
import com.knowledgespike.db.tables.Scorers
import com.knowledgespike.db.tables.Scorersmatches
import com.knowledgespike.db.tables.Teams
import com.knowledgespike.db.tables.Teamsmatchtypes
import com.knowledgespike.db.tables.Tvumpires
import com.knowledgespike.db.tables.Tvumpiresmatches
import com.knowledgespike.db.tables.Umpires
import com.knowledgespike.db.tables.Umpiresmatches

import org.jooq.Index
import org.jooq.impl.DSL
import org.jooq.impl.Internal



// -------------------------------------------------------------------------
// INDEX definitions
// -------------------------------------------------------------------------

val MATCHES_AWAYTEAMID: Index = Internal.createIndex(DSL.name("AwayTeamId"), Matches.MATCHES, arrayOf(Matches.MATCHES.AWAYTEAMID), false)
val BOWLINGDETAILS_BALLS: Index = Internal.createIndex(DSL.name("Balls"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.BALLS), false)
val BATTINGDETAILS_BOWLERID: Index = Internal.createIndex(DSL.name("BowlerId"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.BOWLERID), false)
val BATTINGDETAILS_CAPTAIN: Index = Internal.createIndex(DSL.name("Captain"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.CAPTAIN), false)
val FIELDING_CAUGHTF: Index = Internal.createIndex(DSL.name("CaughtF"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.CAUGHTF), false)
val FIELDING_CAUGHTWK: Index = Internal.createIndex(DSL.name("CaughtWk"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.CAUGHTWK), false)
val COUNTRIES_COUNTRYID: Index = Internal.createIndex(DSL.name("CountryId"), Countries.COUNTRIES, arrayOf(Countries.COUNTRIES.COUNTRYID), false)
val PLAYERSTEAMS_DEBUTID: Index = Internal.createIndex(DSL.name("DebutId"), Playersteams.PLAYERSTEAMS, arrayOf(Playersteams.PLAYERSTEAMS.DEBUTID), false)
val MATCHREFEREES_DEBUTMATCHID: Index = Internal.createIndex(DSL.name("DebutMatchId"), Matchreferees.MATCHREFEREES, arrayOf(Matchreferees.MATCHREFEREES.DEBUTMATCHID), false)
val RESERVEUMPIRES_DEBUTMATCHID: Index = Internal.createIndex(DSL.name("DebutMatchId"), Reserveumpires.RESERVEUMPIRES, arrayOf(Reserveumpires.RESERVEUMPIRES.DEBUTMATCHID), false)
val SCORERS_DEBUTMATCHID: Index = Internal.createIndex(DSL.name("DebutMatchId"), Scorers.SCORERS, arrayOf(Scorers.SCORERS.DEBUTMATCHID), false)
val TVUMPIRES_DEBUTMATCHID: Index = Internal.createIndex(DSL.name("DebutMatchId"), Tvumpires.TVUMPIRES, arrayOf(Tvumpires.TVUMPIRES.DEBUTMATCHID), false)
val UMPIRES_DEBUTMATCHID: Index = Internal.createIndex(DSL.name("DebutMatchId"), Umpires.UMPIRES, arrayOf(Umpires.UMPIRES.DEBUTMATCHID), false)
val FIELDING_DISMISSALS: Index = Internal.createIndex(DSL.name("Dismissals"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.DISMISSALS), false)
val BOWLINGDETAILS_DOTS: Index = Internal.createIndex(DSL.name("Dots"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.DOTS), false)
val BOWLINGDETAILS_FOURS: Index = Internal.createIndex(DSL.name("Fours"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.FOURS), false)
val BATTINGDETAILS_GROUNDID: Index = Internal.createIndex(DSL.name("GroundId"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.GROUNDID), false)
val BOWLINGDETAILS_GROUNDID: Index = Internal.createIndex(DSL.name("GroundId"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.GROUNDID), false)
val GROUNDS_GROUNDID: Index = Internal.createIndex(DSL.name("GroundId"), Grounds.GROUNDS, arrayOf(Grounds.GROUNDS.GROUNDID), false)
val GROUNDSMATCHTYPES_GROUNDID: Index = Internal.createIndex(DSL.name("GroundId"), Groundsmatchtypes.GROUNDSMATCHTYPES, arrayOf(Groundsmatchtypes.GROUNDSMATCHTYPES.GROUNDID), false)
val GROUNDSNAME_GROUNDID: Index = Internal.createIndex(DSL.name("GroundId"), Groundsname.GROUNDSNAME, arrayOf(Groundsname.GROUNDSNAME.GROUNDID), false)
val MATCHES_HOMETEAMID: Index = Internal.createIndex(DSL.name("HomeTeamId"), Matches.MATCHES, arrayOf(Matches.MATCHES.HOMETEAMID), false)
val PARTNERSHIPS_INNINGS: Index = Internal.createIndex(DSL.name("Innings"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.INNINGS), false)
val MATCHES_LOCATIONID: Index = Internal.createIndex(DSL.name("LocationId"), Matches.MATCHES, arrayOf(Matches.MATCHES.LOCATIONID), false)
val BOWLINGDETAILS_MAIDENS: Index = Internal.createIndex(DSL.name("Maidens"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.MAIDENS), false)
val BATTINGDETAILS_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.MATCHID, Battingdetails.BATTINGDETAILS.PLAYERID), false)
val BOWLINGDETAILS_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.MATCHID), false)
val CLOSEOFPLAY_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Closeofplay.CLOSEOFPLAY, arrayOf(Closeofplay.CLOSEOFPLAY.MATCHID), false)
val EXTRAMATCHDETAILS_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Extramatchdetails.EXTRAMATCHDETAILS, arrayOf(Extramatchdetails.EXTRAMATCHDETAILS.MATCHID), false)
val FALLOFWICKETS_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Fallofwickets.FALLOFWICKETS, arrayOf(Fallofwickets.FALLOFWICKETS.MATCHID), false)
val FIELDING_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.MATCHID), false)
val INNINGS_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Innings.INNINGS, arrayOf(Innings.INNINGS.MATCHID), false)
val MATCHREFEREESMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Matchrefereesmatches.MATCHREFEREESMATCHES, arrayOf(Matchrefereesmatches.MATCHREFEREESMATCHES.MATCHID), false)
val MATCHSUBTYPE_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Matchsubtype.MATCHSUBTYPE, arrayOf(Matchsubtype.MATCHSUBTYPE.MATCHID), false)
val NOTES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Notes.NOTES, arrayOf(Notes.NOTES.MATCHID), false)
val PARTNERSHIPS_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.MATCHID), false)
val PLAYERSMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Playersmatches.PLAYERSMATCHES, arrayOf(Playersmatches.PLAYERSMATCHES.MATCHID), false)
val PLAYERSOFTHEMATCHMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Playersofthematchmatches.PLAYERSOFTHEMATCHMATCHES, arrayOf(Playersofthematchmatches.PLAYERSOFTHEMATCHMATCHES.MATCHID), false)
val RESERVEUMPIRESMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Reserveumpiresmatches.RESERVEUMPIRESMATCHES, arrayOf(Reserveumpiresmatches.RESERVEUMPIRESMATCHES.MATCHID), false)
val SCORERSMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Scorersmatches.SCORERSMATCHES, arrayOf(Scorersmatches.SCORERSMATCHES.MATCHID), false)
val TVUMPIRESMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Tvumpiresmatches.TVUMPIRESMATCHES, arrayOf(Tvumpiresmatches.TVUMPIRESMATCHES.MATCHID), false)
val UMPIRESMATCHES_MATCHID: Index = Internal.createIndex(DSL.name("MatchId"), Umpiresmatches.UMPIRESMATCHES, arrayOf(Umpiresmatches.UMPIRESMATCHES.MATCHID), false)
val EXTRAMATCHDETAILS_MATCHID_2: Index = Internal.createIndex(DSL.name("MatchId_2"), Extramatchdetails.EXTRAMATCHDETAILS, arrayOf(Extramatchdetails.EXTRAMATCHDETAILS.MATCHID, Extramatchdetails.EXTRAMATCHDETAILS.TEAMID), false)
val EXTRAMATCHDETAILS_MATCHID_3: Index = Internal.createIndex(DSL.name("MatchId_3"), Extramatchdetails.EXTRAMATCHDETAILS, arrayOf(Extramatchdetails.EXTRAMATCHDETAILS.MATCHID, Extramatchdetails.EXTRAMATCHDETAILS.TEAMID, Extramatchdetails.EXTRAMATCHDETAILS.OPPONENTSID), false)
val BATTINGDETAILS_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.MATCHTYPE), false)
val BOWLINGDETAILS_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.MATCHTYPE), false)
val COMPETITIONS_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Competitions.COMPETITIONS, arrayOf(Competitions.COMPETITIONS.MATCHTYPE), false)
val EXTRAMATCHDETAILS_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Extramatchdetails.EXTRAMATCHDETAILS, arrayOf(Extramatchdetails.EXTRAMATCHDETAILS.MATCHTYPE), false)
val FIELDING_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.MATCHTYPE), false)
val GROUNDSMATCHTYPES_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Groundsmatchtypes.GROUNDSMATCHTYPES, arrayOf(Groundsmatchtypes.GROUNDSMATCHTYPES.MATCHTYPE), false)
val MATCHES_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Matches.MATCHES, arrayOf(Matches.MATCHES.MATCHTYPE), false)
val MATCHSUBTYPE_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Matchsubtype.MATCHSUBTYPE, arrayOf(Matchsubtype.MATCHSUBTYPE.MATCHTYPE), false)
val PARTNERSHIPS_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.MATCHTYPE), false)
val PLAYERSTEAMS_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Playersteams.PLAYERSTEAMS, arrayOf(Playersteams.PLAYERSTEAMS.MATCHTYPE), false)
val TEAMSMATCHTYPES_MATCHTYPE: Index = Internal.createIndex(DSL.name("MatchType"), Teamsmatchtypes.TEAMSMATCHTYPES, arrayOf(Teamsmatchtypes.TEAMSMATCHTYPES.MATCHTYPE), false)
val BOWLINGDETAILS_NOBALLS: Index = Internal.createIndex(DSL.name("NoBalls"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.NOBALLS), false)
val BATTINGDETAILS_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.OPPONENTSID), false)
val BOWLINGDETAILS_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.OPPONENTSID), false)
val EXTRAMATCHDETAILS_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Extramatchdetails.EXTRAMATCHDETAILS, arrayOf(Extramatchdetails.EXTRAMATCHDETAILS.OPPONENTSID), false)
val FALLOFWICKETS_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Fallofwickets.FALLOFWICKETS, arrayOf(Fallofwickets.FALLOFWICKETS.OPPONENTSID), false)
val FIELDING_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.OPPONENTSID), false)
val INNINGS_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Innings.INNINGS, arrayOf(Innings.INNINGS.OPPONENTSID), false)
val PARTNERSHIPS_OPPONENTSID: Index = Internal.createIndex(DSL.name("OpponentsId"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.OPPONENTSID), false)
val PARTNERSHIPSPLAYERS_PARTNERSHIPID: Index = Internal.createIndex(DSL.name("PartnershipId"), Partnershipsplayers.PARTNERSHIPSPLAYERS, arrayOf(Partnershipsplayers.PARTNERSHIPSPLAYERS.PARTNERSHIPID), false)
val MATCHREFEREESMATCHES_PERSONID: Index = Internal.createIndex(DSL.name("PersonId"), Matchrefereesmatches.MATCHREFEREESMATCHES, arrayOf(Matchrefereesmatches.MATCHREFEREESMATCHES.PERSONID), false)
val PLAYERSOFTHEMATCHMATCHES_PERSONID: Index = Internal.createIndex(DSL.name("PersonId"), Playersofthematchmatches.PLAYERSOFTHEMATCHMATCHES, arrayOf(Playersofthematchmatches.PLAYERSOFTHEMATCHMATCHES.PERSONID), false)
val RESERVEUMPIRESMATCHES_PERSONID: Index = Internal.createIndex(DSL.name("PersonId"), Reserveumpiresmatches.RESERVEUMPIRESMATCHES, arrayOf(Reserveumpiresmatches.RESERVEUMPIRESMATCHES.PERSONID), false)
val SCORERSMATCHES_PERSONID: Index = Internal.createIndex(DSL.name("PersonId"), Scorersmatches.SCORERSMATCHES, arrayOf(Scorersmatches.SCORERSMATCHES.PERSONID), false)
val TVUMPIRESMATCHES_PERSONID: Index = Internal.createIndex(DSL.name("PersonId"), Tvumpiresmatches.TVUMPIRESMATCHES, arrayOf(Tvumpiresmatches.TVUMPIRESMATCHES.PERSONID), false)
val UMPIRESMATCHES_PERSONID: Index = Internal.createIndex(DSL.name("PersonId"), Umpiresmatches.UMPIRESMATCHES, arrayOf(Umpiresmatches.UMPIRESMATCHES.PERSONID), false)
val BATTINGDETAILS_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.PLAYERID), false)
val BOWLINGDETAILS_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.PLAYERID), false)
val FALLOFWICKETS_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Fallofwickets.FALLOFWICKETS, arrayOf(Fallofwickets.FALLOFWICKETS.PLAYERID), false)
val FIELDING_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.PLAYERID, Fielding.FIELDING.MATCHID), false)
val PARTNERSHIPSPLAYERS_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Partnershipsplayers.PARTNERSHIPSPLAYERS, arrayOf(Partnershipsplayers.PARTNERSHIPSPLAYERS.PLAYERID), false)
val PLAYERS_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Players.PLAYERS, arrayOf(Players.PLAYERS.PLAYERID), false)
val PLAYERSMATCHES_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Playersmatches.PLAYERSMATCHES, arrayOf(Playersmatches.PLAYERSMATCHES.PLAYERID, Playersmatches.PLAYERSMATCHES.MATCHID), false)
val PLAYERSTEAMS_PLAYERID: Index = Internal.createIndex(DSL.name("PlayerId"), Playersteams.PLAYERSTEAMS, arrayOf(Playersteams.PLAYERSTEAMS.PLAYERID, Playersteams.PLAYERSTEAMS.TEAMID, Playersteams.PLAYERSTEAMS.MATCHTYPE), false)
val PARTNERSHIPS_PLAYERIDS: Index = Internal.createIndex(DSL.name("PlayerIds"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.PLAYERIDS), false)
val MATCHREFEREES_REFEREESID: Index = Internal.createIndex(DSL.name("RefereesId"), Matchreferees.MATCHREFEREES, arrayOf(Matchreferees.MATCHREFEREES.REFEREESID), false)
val RESERVEUMPIRES_RESERVEUMPIRESID: Index = Internal.createIndex(DSL.name("ReserveUmpiresId"), Reserveumpires.RESERVEUMPIRES, arrayOf(Reserveumpires.RESERVEUMPIRES.RESERVEUMPIRESID), false)
val BOWLINGDETAILS_RUNS: Index = Internal.createIndex(DSL.name("Runs"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.RUNS), false)
val BATTINGDETAILS_SCORE: Index = Internal.createIndex(DSL.name("Score"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.SCORE), false)
val SCORERS_SCORERSID: Index = Internal.createIndex(DSL.name("ScorersId"), Scorers.SCORERS, arrayOf(Scorers.SCORERS.SCORERSID), false)
val BOWLINGDETAILS_SIXES: Index = Internal.createIndex(DSL.name("Sixes"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.SIXES), false)
val FIELDING_STUMPED: Index = Internal.createIndex(DSL.name("Stumped"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.STUMPED), false)
val BATTINGDETAILS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.TEAMID, Battingdetails.BATTINGDETAILS.MATCHID), false)
val BOWLINGDETAILS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.TEAMID), false)
val EXTRAMATCHDETAILS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Extramatchdetails.EXTRAMATCHDETAILS, arrayOf(Extramatchdetails.EXTRAMATCHDETAILS.TEAMID), false)
val FALLOFWICKETS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Fallofwickets.FALLOFWICKETS, arrayOf(Fallofwickets.FALLOFWICKETS.TEAMID), false)
val FIELDING_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Fielding.FIELDING, arrayOf(Fielding.FIELDING.TEAMID), false)
val INNINGS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Innings.INNINGS, arrayOf(Innings.INNINGS.TEAMID), false)
val PARTNERSHIPS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.TEAMID), false)
val PLAYERSTEAMS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Playersteams.PLAYERSTEAMS, arrayOf(Playersteams.PLAYERSTEAMS.TEAMID), false)
val TEAMS_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Teams.TEAMS, arrayOf(Teams.TEAMS.TEAMID), false)
val TEAMSMATCHTYPES_TEAMID: Index = Internal.createIndex(DSL.name("TeamId"), Teamsmatchtypes.TEAMSMATCHTYPES, arrayOf(Teamsmatchtypes.TEAMSMATCHTYPES.TEAMID), false)
val MATCHES_TOSSTEAMID: Index = Internal.createIndex(DSL.name("TossTeamId"), Matches.MATCHES, arrayOf(Matches.MATCHES.TOSSTEAMID), false)
val TVUMPIRES_TVUMPIRESID: Index = Internal.createIndex(DSL.name("TvUmpiresId"), Tvumpires.TVUMPIRES, arrayOf(Tvumpires.TVUMPIRES.TVUMPIRESID), false)
val UMPIRES_UMPIRESID: Index = Internal.createIndex(DSL.name("UmpiresId"), Umpires.UMPIRES, arrayOf(Umpires.UMPIRES.UMPIRESID), false)
val PARTNERSHIPS_UNBROKEN: Index = Internal.createIndex(DSL.name("Unbroken"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.UNBROKEN), false)
val MATCHES_WHOLOSTID: Index = Internal.createIndex(DSL.name("WhoLostId"), Matches.MATCHES, arrayOf(Matches.MATCHES.WHOLOSTID), false)
val MATCHES_WHOWONID: Index = Internal.createIndex(DSL.name("WhoWonId"), Matches.MATCHES, arrayOf(Matches.MATCHES.WHOWONID), false)
val PARTNERSHIPS_WICKET: Index = Internal.createIndex(DSL.name("Wicket"), Partnerships.PARTNERSHIPS, arrayOf(Partnerships.PARTNERSHIPS.WICKET), false)
val BATTINGDETAILS_WICKETKEEPER: Index = Internal.createIndex(DSL.name("WicketKeeper"), Battingdetails.BATTINGDETAILS, arrayOf(Battingdetails.BATTINGDETAILS.WICKETKEEPER), false)
val BOWLINGDETAILS_WICKETS: Index = Internal.createIndex(DSL.name("Wickets"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.WICKETS), false)
val BOWLINGDETAILS_WIDES: Index = Internal.createIndex(DSL.name("Wides"), Bowlingdetails.BOWLINGDETAILS, arrayOf(Bowlingdetails.BOWLINGDETAILS.WIDES), false)
