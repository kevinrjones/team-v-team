/*
 * This file is generated by jOOQ.
 */
package com.knowledgespike.db.tables


import com.knowledgespike.db.Cricketarchive
import com.knowledgespike.db.indexes.PLAYERSMATCHES_MATCHID
import com.knowledgespike.db.indexes.PLAYERSMATCHES_PLAYERID
import com.knowledgespike.db.keys.KEY_PLAYERSMATCHES_PRIMARY
import com.knowledgespike.db.keys.PLAYERSMATCHES_IBFK_1
import com.knowledgespike.db.keys.PLAYERSMATCHES_IBFK_2
import com.knowledgespike.db.tables.Matches.MatchesPath
import com.knowledgespike.db.tables.Players.PlayersPath
import com.knowledgespike.db.tables.records.PlayersmatchesRecord

import kotlin.collections.Collection
import kotlin.collections.List

import org.jooq.Condition
import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.Index
import org.jooq.InverseForeignKey
import org.jooq.Name
import org.jooq.Path
import org.jooq.PlainSQL
import org.jooq.QueryPart
import org.jooq.Record
import org.jooq.SQL
import org.jooq.Schema
import org.jooq.Select
import org.jooq.Stringly
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Playersmatches(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, PlayersmatchesRecord>?,
    parentPath: InverseForeignKey<out Record, PlayersmatchesRecord>?,
    aliased: Table<PlayersmatchesRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<PlayersmatchesRecord>(
    alias,
    Cricketarchive.CRICKETARCHIVE,
    path,
    childPath,
    parentPath,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table(),
    where,
) {
    companion object {

        /**
         * The reference instance of <code>cricketarchive.PlayersMatches</code>
         */
        val PLAYERSMATCHES: Playersmatches = Playersmatches()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<PlayersmatchesRecord> = PlayersmatchesRecord::class.java

    /**
     * The column <code>cricketarchive.PlayersMatches.Id</code>.
     */
    val ID: TableField<PlayersmatchesRecord, Int?> = createField(DSL.name("Id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.PlayerId</code>.
     */
    val PLAYERID: TableField<PlayersmatchesRecord, Int?> = createField(DSL.name("PlayerId"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.MatchId</code>.
     */
    val MATCHID: TableField<PlayersmatchesRecord, Int?> = createField(DSL.name("MatchId"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.Teamid</code>.
     */
    val TEAMID: TableField<PlayersmatchesRecord, Int?> = createField(DSL.name("Teamid"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.FullName</code>.
     */
    val FULLNAME: TableField<PlayersmatchesRecord, String?> = createField(DSL.name("FullName"), SQLDataType.VARCHAR(200).nullable(false), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.SortNamePart</code>.
     */
    val SORTNAMEPART: TableField<PlayersmatchesRecord, String?> = createField(DSL.name("SortNamePart"), SQLDataType.VARCHAR(200).nullable(false), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.OtherNamePart</code>.
     */
    val OTHERNAMEPART: TableField<PlayersmatchesRecord, String?> = createField(DSL.name("OtherNamePart"), SQLDataType.VARCHAR(200).nullable(false), this, "")

    /**
     * The column <code>cricketarchive.PlayersMatches.IsFullSubstitute</code>.
     */
    val ISFULLSUBSTITUTE: TableField<PlayersmatchesRecord, Byte?> = createField(DSL.name("IsFullSubstitute"), SQLDataType.TINYINT.nullable(false), this, "")

    /**
     * The column
     * <code>cricketarchive.PlayersMatches.IsSubstituteFielder</code>.
     */
    val ISSUBSTITUTEFIELDER: TableField<PlayersmatchesRecord, Byte?> = createField(DSL.name("IsSubstituteFielder"), SQLDataType.TINYINT.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<PlayersmatchesRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<PlayersmatchesRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<PlayersmatchesRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>cricketarchive.PlayersMatches</code> table
     * reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>cricketarchive.PlayersMatches</code> table
     * reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>cricketarchive.PlayersMatches</code> table reference
     */
    constructor(): this(DSL.name("PlayersMatches"), null)

    constructor(path: Table<out Record>, childPath: ForeignKey<out Record, PlayersmatchesRecord>?, parentPath: InverseForeignKey<out Record, PlayersmatchesRecord>?): this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, PLAYERSMATCHES, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class PlayersmatchesPath : Playersmatches, Path<PlayersmatchesRecord> {
        constructor(path: Table<out Record>, childPath: ForeignKey<out Record, PlayersmatchesRecord>?, parentPath: InverseForeignKey<out Record, PlayersmatchesRecord>?): super(path, childPath, parentPath)
        private constructor(alias: Name, aliased: Table<PlayersmatchesRecord>): super(alias, aliased)
        override fun `as`(alias: String): PlayersmatchesPath = PlayersmatchesPath(DSL.name(alias), this)
        override fun `as`(alias: Name): PlayersmatchesPath = PlayersmatchesPath(alias, this)
        override fun `as`(alias: Table<*>): PlayersmatchesPath = PlayersmatchesPath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else Cricketarchive.CRICKETARCHIVE
    override fun getIndexes(): List<Index> = listOf(PLAYERSMATCHES_MATCHID, PLAYERSMATCHES_PLAYERID)
    override fun getIdentity(): Identity<PlayersmatchesRecord, Int?> = super.getIdentity() as Identity<PlayersmatchesRecord, Int?>
    override fun getPrimaryKey(): UniqueKey<PlayersmatchesRecord> = KEY_PLAYERSMATCHES_PRIMARY
    override fun getReferences(): List<ForeignKey<PlayersmatchesRecord, *>> = listOf(PLAYERSMATCHES_IBFK_1, PLAYERSMATCHES_IBFK_2)

    private lateinit var _players: PlayersPath

    /**
     * Get the implicit join path to the <code>cricketarchive.Players</code>
     * table.
     */
    fun players(): PlayersPath {
        if (!this::_players.isInitialized)
            _players = PlayersPath(this, PLAYERSMATCHES_IBFK_1, null)

        return _players;
    }

    val players: PlayersPath
        get(): PlayersPath = players()

    private lateinit var _matches: MatchesPath

    /**
     * Get the implicit join path to the <code>cricketarchive.Matches</code>
     * table.
     */
    fun matches(): MatchesPath {
        if (!this::_matches.isInitialized)
            _matches = MatchesPath(this, PLAYERSMATCHES_IBFK_2, null)

        return _matches;
    }

    val matches: MatchesPath
        get(): MatchesPath = matches()
    override fun `as`(alias: String): Playersmatches = Playersmatches(DSL.name(alias), this)
    override fun `as`(alias: Name): Playersmatches = Playersmatches(alias, this)
    override fun `as`(alias: Table<*>): Playersmatches = Playersmatches(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Playersmatches = Playersmatches(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Playersmatches = Playersmatches(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Playersmatches = Playersmatches(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Playersmatches = Playersmatches(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Playersmatches = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Playersmatches = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Playersmatches = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): Playersmatches = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): Playersmatches = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Playersmatches = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Playersmatches = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Playersmatches = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Playersmatches = where(DSL.notExists(select))
}
