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
open class FowPartnershipListBySeason : AbstractRoutine<java.lang.Void>("fow_partnership_list_by_season", Cricketarchive.CRICKETARCHIVE) {
    companion object {

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.team_id</code>.
         */
        val TEAM_ID: Parameter<Int?> = Internal.createParameter("team_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.opponents_id</code>.
         */
        val OPPONENTS_ID: Parameter<Int?> = Internal.createParameter("opponents_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.match_type</code>.
         */
        val MATCH_TYPE: Parameter<String?> = Internal.createParameter("match_type", SQLDataType.VARCHAR(20), false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.match_subtype</code>.
         */
        val MATCH_SUBTYPE: Parameter<String?> = Internal.createParameter("match_subtype", SQLDataType.VARCHAR(20), false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.ground_id</code>.
         */
        val GROUND_ID: Parameter<Int?> = Internal.createParameter("ground_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.homecountry_id</code>.
         */
        val HOMECOUNTRY_ID: Parameter<Int?> = Internal.createParameter("homecountry_id", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.homeOrAway</code>.
         */
        val HOMEORAWAY: Parameter<Int?> = Internal.createParameter("homeOrAway", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.startDate</code>.
         */
        val STARTDATE: Parameter<String?> = Internal.createParameter("startDate", SQLDataType.CLOB, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.endDate</code>.
         */
        val ENDDATE: Parameter<String?> = Internal.createParameter("endDate", SQLDataType.CLOB, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.season</code>.
         */
        val SEASON: Parameter<String?> = Internal.createParameter("season", SQLDataType.VARCHAR(10), false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.matchResult</code>.
         */
        val MATCHRESULT: Parameter<Int?> = Internal.createParameter("matchResult", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.runs_limit</code>.
         */
        val RUNS_LIMIT: Parameter<Int?> = Internal.createParameter("runs_limit", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.sort_by</code>.
         */
        val SORT_BY: Parameter<Int?> = Internal.createParameter("sort_by", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.sort_direction</code>.
         */
        val SORT_DIRECTION: Parameter<String?> = Internal.createParameter("sort_direction", SQLDataType.VARCHAR(5), false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.start_row</code>.
         */
        val START_ROW: Parameter<Int?> = Internal.createParameter("start_row", SQLDataType.INTEGER, false, false)

        /**
         * The parameter
         * <code>cricketarchive.fow_partnership_list_by_season.page_size</code>.
         */
        val PAGE_SIZE: Parameter<Int?> = Internal.createParameter("page_size", SQLDataType.INTEGER, false, false)
    }

    init {
        addInParameter(FowPartnershipListBySeason.TEAM_ID)
        addInParameter(FowPartnershipListBySeason.OPPONENTS_ID)
        addInParameter(FowPartnershipListBySeason.MATCH_TYPE)
        addInParameter(FowPartnershipListBySeason.MATCH_SUBTYPE)
        addInParameter(FowPartnershipListBySeason.GROUND_ID)
        addInParameter(FowPartnershipListBySeason.HOMECOUNTRY_ID)
        addInParameter(FowPartnershipListBySeason.HOMEORAWAY)
        addInParameter(FowPartnershipListBySeason.STARTDATE)
        addInParameter(FowPartnershipListBySeason.ENDDATE)
        addInParameter(FowPartnershipListBySeason.SEASON)
        addInParameter(FowPartnershipListBySeason.MATCHRESULT)
        addInParameter(FowPartnershipListBySeason.RUNS_LIMIT)
        addInParameter(FowPartnershipListBySeason.SORT_BY)
        addInParameter(FowPartnershipListBySeason.SORT_DIRECTION)
        addInParameter(FowPartnershipListBySeason.START_ROW)
        addInParameter(FowPartnershipListBySeason.PAGE_SIZE)
    }

    /**
     * Set the <code>team_id</code> parameter IN value to the routine
     */
    fun setTeamId(value: Int?): Unit = setValue(FowPartnershipListBySeason.TEAM_ID, value)

    /**
     * Set the <code>opponents_id</code> parameter IN value to the routine
     */
    fun setOpponentsId(value: Int?): Unit = setValue(FowPartnershipListBySeason.OPPONENTS_ID, value)

    /**
     * Set the <code>match_type</code> parameter IN value to the routine
     */
    fun setMatchType(value: String?): Unit = setValue(FowPartnershipListBySeason.MATCH_TYPE, value)

    /**
     * Set the <code>match_subtype</code> parameter IN value to the routine
     */
    fun setMatchSubtype(value: String?): Unit = setValue(FowPartnershipListBySeason.MATCH_SUBTYPE, value)

    /**
     * Set the <code>ground_id</code> parameter IN value to the routine
     */
    fun setGroundId(value: Int?): Unit = setValue(FowPartnershipListBySeason.GROUND_ID, value)

    /**
     * Set the <code>homecountry_id</code> parameter IN value to the routine
     */
    fun setHomecountryId(value: Int?): Unit = setValue(FowPartnershipListBySeason.HOMECOUNTRY_ID, value)

    /**
     * Set the <code>homeOrAway</code> parameter IN value to the routine
     */
    fun setHomeoraway(value: Int?): Unit = setValue(FowPartnershipListBySeason.HOMEORAWAY, value)

    /**
     * Set the <code>startDate</code> parameter IN value to the routine
     */
    fun setStartdate(value: String?): Unit = setValue(FowPartnershipListBySeason.STARTDATE, value)

    /**
     * Set the <code>endDate</code> parameter IN value to the routine
     */
    fun setEnddate(value: String?): Unit = setValue(FowPartnershipListBySeason.ENDDATE, value)

    /**
     * Set the <code>season</code> parameter IN value to the routine
     */
    fun setSeason(value: String?): Unit = setValue(FowPartnershipListBySeason.SEASON, value)

    /**
     * Set the <code>matchResult</code> parameter IN value to the routine
     */
    fun setMatchresult(value: Int?): Unit = setValue(FowPartnershipListBySeason.MATCHRESULT, value)

    /**
     * Set the <code>runs_limit</code> parameter IN value to the routine
     */
    fun setRunsLimit(value: Int?): Unit = setValue(FowPartnershipListBySeason.RUNS_LIMIT, value)

    /**
     * Set the <code>sort_by</code> parameter IN value to the routine
     */
    fun setSortBy(value: Int?): Unit = setValue(FowPartnershipListBySeason.SORT_BY, value)

    /**
     * Set the <code>sort_direction</code> parameter IN value to the routine
     */
    fun setSortDirection(value: String?): Unit = setValue(FowPartnershipListBySeason.SORT_DIRECTION, value)

    /**
     * Set the <code>start_row</code> parameter IN value to the routine
     */
    fun setStartRow(value: Int?): Unit = setValue(FowPartnershipListBySeason.START_ROW, value)

    /**
     * Set the <code>page_size</code> parameter IN value to the routine
     */
    fun setPageSize(value: Int?): Unit = setValue(FowPartnershipListBySeason.PAGE_SIZE, value)
}