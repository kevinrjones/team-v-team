/*
 * This file is generated by jOOQ.
 */
package com.knowledgespike.db.routines


import com.knowledgespike.db.Cricketarchive

import org.jooq.Parameter
import org.jooq.impl.AbstractRoutine
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class TeamRecordsByHostAgainstOpponents : AbstractRoutine<java.lang.Void>("team_records_by_host_against_opponents", Cricketarchive.CRICKETARCHIVE) {
    companion object {

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.match_type</code>.
         */
        val MATCH_TYPE: Parameter<String?> = Internal.createParameter("match_type", SQLDataType.VARCHAR(20), false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.match_subtype</code>.
         */
        val MATCH_SUBTYPE: Parameter<String?> = Internal.createParameter("match_subtype", SQLDataType.VARCHAR(20), false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.opponents_id</code>.
         */
        val OPPONENTS_ID: Parameter<Int?> = Internal.createParameter("opponents_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.ground_id</code>.
         */
        val GROUND_ID: Parameter<Int?> = Internal.createParameter("ground_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.homecountry_id</code>.
         */
        val HOMECOUNTRY_ID: Parameter<Int?> = Internal.createParameter("homecountry_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.homeOrAway</code>.
         */
        val HOMEORAWAY: Parameter<Int?> = Internal.createParameter("homeOrAway", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.startDate</code>.
         */
        val STARTDATE: Parameter<String?> = Internal.createParameter("startDate", SQLDataType.CLOB, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.endDate</code>.
         */
        val ENDDATE: Parameter<String?> = Internal.createParameter("endDate", SQLDataType.CLOB, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.season</code>.
         */
        val SEASON: Parameter<String?> = Internal.createParameter("season", SQLDataType.VARCHAR(10), false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.matchResult</code>.
         */
        val MATCHRESULT: Parameter<Int?> = Internal.createParameter("matchResult", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.sort_by</code>.
         */
        val SORT_BY: Parameter<Int?> = Internal.createParameter("sort_by", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.sort_direction</code>.
         */
        val SORT_DIRECTION: Parameter<String?> = Internal.createParameter("sort_direction", SQLDataType.VARCHAR(5), false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.start_row</code>.
         */
        val START_ROW: Parameter<Int?> = Internal.createParameter("start_row", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.team_records_by_host_against_opponents.page_size</code>.
         */
        val PAGE_SIZE: Parameter<Int?> = Internal.createParameter("page_size", SQLDataType.INTEGER, false, false)
    }

    init {
        addInParameter(TeamRecordsByHostAgainstOpponents.MATCH_TYPE)
        addInParameter(TeamRecordsByHostAgainstOpponents.MATCH_SUBTYPE)
        addInParameter(TeamRecordsByHostAgainstOpponents.OPPONENTS_ID)
        addInParameter(TeamRecordsByHostAgainstOpponents.GROUND_ID)
        addInParameter(TeamRecordsByHostAgainstOpponents.HOMECOUNTRY_ID)
        addInParameter(TeamRecordsByHostAgainstOpponents.HOMEORAWAY)
        addInParameter(TeamRecordsByHostAgainstOpponents.STARTDATE)
        addInParameter(TeamRecordsByHostAgainstOpponents.ENDDATE)
        addInParameter(TeamRecordsByHostAgainstOpponents.SEASON)
        addInParameter(TeamRecordsByHostAgainstOpponents.MATCHRESULT)
        addInParameter(TeamRecordsByHostAgainstOpponents.SORT_BY)
        addInParameter(TeamRecordsByHostAgainstOpponents.SORT_DIRECTION)
        addInParameter(TeamRecordsByHostAgainstOpponents.START_ROW)
        addInParameter(TeamRecordsByHostAgainstOpponents.PAGE_SIZE)
    }

    /**
     * Set the <code>match_type</code> parameter IN value to the routine
     */
    fun setMatchType(value: String?): Unit = setValue(TeamRecordsByHostAgainstOpponents.MATCH_TYPE, value)

    /**
     * Set the <code>match_subtype</code> parameter IN value to the routine
     */
    fun setMatchSubtype(value: String?): Unit = setValue(TeamRecordsByHostAgainstOpponents.MATCH_SUBTYPE, value)

    /**
     * Set the <code>opponents_id</code> parameter IN value to the routine
     */
    fun setOpponentsId(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.OPPONENTS_ID, value)

    /**
     * Set the <code>ground_id</code> parameter IN value to the routine
     */
    fun setGroundId(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.GROUND_ID, value)

    /**
     * Set the <code>homecountry_id</code> parameter IN value to the routine
     */
    fun setHomecountryId(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.HOMECOUNTRY_ID, value)

    /**
     * Set the <code>homeOrAway</code> parameter IN value to the routine
     */
    fun setHomeoraway(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.HOMEORAWAY, value)

    /**
     * Set the <code>startDate</code> parameter IN value to the routine
     */
    fun setStartdate(value: String?): Unit = setValue(TeamRecordsByHostAgainstOpponents.STARTDATE, value)

    /**
     * Set the <code>endDate</code> parameter IN value to the routine
     */
    fun setEnddate(value: String?): Unit = setValue(TeamRecordsByHostAgainstOpponents.ENDDATE, value)

    /**
     * Set the <code>season</code> parameter IN value to the routine
     */
    fun setSeason(value: String?): Unit = setValue(TeamRecordsByHostAgainstOpponents.SEASON, value)

    /**
     * Set the <code>matchResult</code> parameter IN value to the routine
     */
    fun setMatchresult(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.MATCHRESULT, value)

    /**
     * Set the <code>sort_by</code> parameter IN value to the routine
     */
    fun setSortBy(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.SORT_BY, value)

    /**
     * Set the <code>sort_direction</code> parameter IN value to the routine
     */
    fun setSortDirection(value: String?): Unit = setValue(TeamRecordsByHostAgainstOpponents.SORT_DIRECTION, value)

    /**
     * Set the <code>start_row</code> parameter IN value to the routine
     */
    fun setStartRow(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.START_ROW, value)

    /**
     * Set the <code>page_size</code> parameter IN value to the routine
     */
    fun setPageSize(value: Int?): Unit = setValue(TeamRecordsByHostAgainstOpponents.PAGE_SIZE, value)
}