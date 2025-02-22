/*
 * This file is generated by jOOQ.
 */
package com.knowledgespike.db.tables


import com.knowledgespike.db.Cricketarchive
import com.knowledgespike.db.indexes.PLAYERS_PLAYERID
import com.knowledgespike.db.keys.BATTINGDETAILS_IBFK_2
import com.knowledgespike.db.keys.BATTINGDETAILS_IBFK_3
import com.knowledgespike.db.keys.BOWLINGDETAILS_IBFK_2
import com.knowledgespike.db.keys.FALLOFWICKETS_IBFK_2
import com.knowledgespike.db.keys.FIELDING_IBFK_2
import com.knowledgespike.db.keys.KEY_PLAYERS_PRIMARY
import com.knowledgespike.db.keys.PARTNERSHIPSPLAYERS_IBFK_1
import com.knowledgespike.db.keys.PLAYERSMATCHES_IBFK_1
import com.knowledgespike.db.keys.PLAYERSOFTHEMATCHMATCHES_IBFK_1
import com.knowledgespike.db.keys.PLAYERSTEAMS_IBFK_1
import com.knowledgespike.db.tables.Battingdetails.BattingdetailsPath
import com.knowledgespike.db.tables.Bowlingdetails.BowlingdetailsPath
import com.knowledgespike.db.tables.Fallofwickets.FallofwicketsPath
import com.knowledgespike.db.tables.Fielding.FieldingPath
import com.knowledgespike.db.tables.Partnershipsplayers.PartnershipsplayersPath
import com.knowledgespike.db.tables.Playersmatches.PlayersmatchesPath
import com.knowledgespike.db.tables.Playersofthematchmatches.PlayersofthematchmatchesPath
import com.knowledgespike.db.tables.Playersteams.PlayersteamsPath
import com.knowledgespike.db.tables.records.PlayersRecord

import java.time.LocalDate

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
open class Players(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, PlayersRecord>?,
    parentPath: InverseForeignKey<out Record, PlayersRecord>?,
    aliased: Table<PlayersRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<PlayersRecord>(
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
         * The reference instance of <code>cricketarchive.Players</code>
         */
        val PLAYERS: Players = Players()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<PlayersRecord> = PlayersRecord::class.java

    /**
     * The column <code>cricketarchive.Players.Id</code>.
     */
    val ID: TableField<PlayersRecord, Int?> = createField(DSL.name("Id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "")

    /**
     * The column <code>cricketarchive.Players.PlayerId</code>.
     */
    val PLAYERID: TableField<PlayersRecord, Int?> = createField(DSL.name("PlayerId"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.FullName</code>.
     */
    val FULLNAME: TableField<PlayersRecord, String?> = createField(DSL.name("FullName"), SQLDataType.VARCHAR(200).nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.SortNamePart</code>.
     */
    val SORTNAMEPART: TableField<PlayersRecord, String?> = createField(DSL.name("SortNamePart"), SQLDataType.VARCHAR(200).nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.OtherNamePart</code>.
     */
    val OTHERNAMEPART: TableField<PlayersRecord, String?> = createField(DSL.name("OtherNamePart"), SQLDataType.VARCHAR(200).nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.Birthdate</code>.
     */
    val BIRTHDATE: TableField<PlayersRecord, LocalDate?> = createField(DSL.name("Birthdate"), SQLDataType.LOCALDATE.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.LOCALDATE)), this, "")

    /**
     * The column <code>cricketarchive.Players.BirthdateAsOffset</code>.
     */
    val BIRTHDATEASOFFSET: TableField<PlayersRecord, Long?> = createField(DSL.name("BirthdateAsOffset"), SQLDataType.BIGINT.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINT)), this, "")

    /**
     * The column <code>cricketarchive.Players.BirthdateAsText</code>.
     */
    val BIRTHDATEASTEXT: TableField<PlayersRecord, String?> = createField(DSL.name("BirthdateAsText"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "")

    /**
     * The column <code>cricketarchive.Players.PlaceOfBirth</code>.
     */
    val PLACEOFBIRTH: TableField<PlayersRecord, String?> = createField(DSL.name("PlaceOfBirth"), SQLDataType.VARCHAR(300).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "")

    /**
     * The column <code>cricketarchive.Players.DateDied</code>.
     */
    val DATEDIED: TableField<PlayersRecord, LocalDate?> = createField(DSL.name("DateDied"), SQLDataType.LOCALDATE.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.LOCALDATE)), this, "")

    /**
     * The column <code>cricketarchive.Players.DateDiedAsOffset</code>.
     */
    val DATEDIEDASOFFSET: TableField<PlayersRecord, Long?> = createField(DSL.name("DateDiedAsOffset"), SQLDataType.BIGINT.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINT)), this, "")

    /**
     * The column <code>cricketarchive.Players.DateDiedAsText</code>.
     */
    val DATEDIEDASTEXT: TableField<PlayersRecord, String?> = createField(DSL.name("DateDiedAsText"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "")

    /**
     * The column <code>cricketarchive.Players.PlaceOfDeath</code>.
     */
    val PLACEOFDEATH: TableField<PlayersRecord, String?> = createField(DSL.name("PlaceOfDeath"), SQLDataType.VARCHAR(300).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "")

    /**
     * The column <code>cricketarchive.Players.NameChanged</code>.
     */
    val NAMECHANGED: TableField<PlayersRecord, LocalDate?> = createField(DSL.name("NameChanged"), SQLDataType.LOCALDATE.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.NameChangedAsOffset</code>.
     */
    val NAMECHANGEDASOFFSET: TableField<PlayersRecord, Long?> = createField(DSL.name("NameChangedAsOffset"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.Debut</code>.
     */
    val DEBUT: TableField<PlayersRecord, LocalDate?> = createField(DSL.name("Debut"), SQLDataType.LOCALDATE.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.ActiveUntil</code>.
     */
    val ACTIVEUNTIL: TableField<PlayersRecord, LocalDate?> = createField(DSL.name("ActiveUntil"), SQLDataType.LOCALDATE.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.DebutAsOffset</code>.
     */
    val DEBUTASOFFSET: TableField<PlayersRecord, Long?> = createField(DSL.name("DebutAsOffset"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.ActiveUntilAsOffset</code>.
     */
    val ACTIVEUNTILASOFFSET: TableField<PlayersRecord, Long?> = createField(DSL.name("ActiveUntilAsOffset"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>cricketarchive.Players.BattingHand</code>.
     */
    val BATTINGHAND: TableField<PlayersRecord, Int?> = createField(DSL.name("BattingHand"), SQLDataType.INTEGER.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.INTEGER)), this, "")

    /**
     * The column <code>cricketarchive.Players.BowlingHand</code>.
     */
    val BOWLINGHAND: TableField<PlayersRecord, Int?> = createField(DSL.name("BowlingHand"), SQLDataType.INTEGER.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.INTEGER)), this, "")

    /**
     * The column <code>cricketarchive.Players.BowlingStyle</code>.
     */
    val BOWLINGSTYLE: TableField<PlayersRecord, Int?> = createField(DSL.name("BowlingStyle"), SQLDataType.INTEGER.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.INTEGER)), this, "")

    /**
     * The column <code>cricketarchive.Players.BowlingMode</code>.
     */
    val BOWLINGMODE: TableField<PlayersRecord, Int?> = createField(DSL.name("BowlingMode"), SQLDataType.INTEGER.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.INTEGER)), this, "")

    /**
     * The column <code>cricketarchive.Players.ShortBowlingStyles</code>.
     */
    val SHORTBOWLINGSTYLES: TableField<PlayersRecord, String?> = createField(DSL.name("ShortBowlingStyles"), SQLDataType.VARCHAR(100).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "")

    /**
     * The column <code>cricketarchive.Players.WicketKeeper</code>.
     */
    val WICKETKEEPER: TableField<PlayersRecord, Byte?> = createField(DSL.name("WicketKeeper"), SQLDataType.TINYINT.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<PlayersRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<PlayersRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<PlayersRecord>?, where: Condition?): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>cricketarchive.Players</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>cricketarchive.Players</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>cricketarchive.Players</code> table reference
     */
    constructor(): this(DSL.name("Players"), null)

    constructor(path: Table<out Record>, childPath: ForeignKey<out Record, PlayersRecord>?, parentPath: InverseForeignKey<out Record, PlayersRecord>?): this(Internal.createPathAlias(path, childPath, parentPath), path, childPath, parentPath, PLAYERS, null, null)

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    open class PlayersPath : Players, Path<PlayersRecord> {
        constructor(path: Table<out Record>, childPath: ForeignKey<out Record, PlayersRecord>?, parentPath: InverseForeignKey<out Record, PlayersRecord>?): super(path, childPath, parentPath)
        private constructor(alias: Name, aliased: Table<PlayersRecord>): super(alias, aliased)
        override fun `as`(alias: String): PlayersPath = PlayersPath(DSL.name(alias), this)
        override fun `as`(alias: Name): PlayersPath = PlayersPath(alias, this)
        override fun `as`(alias: Table<*>): PlayersPath = PlayersPath(alias.qualifiedName, this)
    }
    override fun getSchema(): Schema? = if (aliased()) null else Cricketarchive.CRICKETARCHIVE
    override fun getIndexes(): List<Index> = listOf(PLAYERS_PLAYERID)
    override fun getIdentity(): Identity<PlayersRecord, Int?> = super.getIdentity() as Identity<PlayersRecord, Int?>
    override fun getPrimaryKey(): UniqueKey<PlayersRecord> = KEY_PLAYERS_PRIMARY

    private lateinit var _battingdetailsIbfk_2: BattingdetailsPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.BattingDetails</code> table, via the
     * <code>battingdetails_ibfk_2</code> key
     */
    fun battingdetailsIbfk_2(): BattingdetailsPath {
        if (!this::_battingdetailsIbfk_2.isInitialized)
            _battingdetailsIbfk_2 = BattingdetailsPath(this, null, BATTINGDETAILS_IBFK_2.inverseKey)

        return _battingdetailsIbfk_2;
    }

    val battingdetailsIbfk_2: BattingdetailsPath
        get(): BattingdetailsPath = battingdetailsIbfk_2()

    private lateinit var _battingdetailsIbfk_3: BattingdetailsPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.BattingDetails</code> table, via the
     * <code>battingdetails_ibfk_3</code> key
     */
    fun battingdetailsIbfk_3(): BattingdetailsPath {
        if (!this::_battingdetailsIbfk_3.isInitialized)
            _battingdetailsIbfk_3 = BattingdetailsPath(this, null, BATTINGDETAILS_IBFK_3.inverseKey)

        return _battingdetailsIbfk_3;
    }

    val battingdetailsIbfk_3: BattingdetailsPath
        get(): BattingdetailsPath = battingdetailsIbfk_3()

    private lateinit var _bowlingdetails: BowlingdetailsPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.BowlingDetails</code> table
     */
    fun bowlingdetails(): BowlingdetailsPath {
        if (!this::_bowlingdetails.isInitialized)
            _bowlingdetails = BowlingdetailsPath(this, null, BOWLINGDETAILS_IBFK_2.inverseKey)

        return _bowlingdetails;
    }

    val bowlingdetails: BowlingdetailsPath
        get(): BowlingdetailsPath = bowlingdetails()

    private lateinit var _fallofwickets: FallofwicketsPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.FallOfWickets</code> table
     */
    fun fallofwickets(): FallofwicketsPath {
        if (!this::_fallofwickets.isInitialized)
            _fallofwickets = FallofwicketsPath(this, null, FALLOFWICKETS_IBFK_2.inverseKey)

        return _fallofwickets;
    }

    val fallofwickets: FallofwicketsPath
        get(): FallofwicketsPath = fallofwickets()

    private lateinit var _fielding: FieldingPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.Fielding</code> table
     */
    fun fielding(): FieldingPath {
        if (!this::_fielding.isInitialized)
            _fielding = FieldingPath(this, null, FIELDING_IBFK_2.inverseKey)

        return _fielding;
    }

    val fielding: FieldingPath
        get(): FieldingPath = fielding()

    private lateinit var _partnershipsplayers: PartnershipsplayersPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.PartnershipsPlayers</code> table
     */
    fun partnershipsplayers(): PartnershipsplayersPath {
        if (!this::_partnershipsplayers.isInitialized)
            _partnershipsplayers = PartnershipsplayersPath(this, null, PARTNERSHIPSPLAYERS_IBFK_1.inverseKey)

        return _partnershipsplayers;
    }

    val partnershipsplayers: PartnershipsplayersPath
        get(): PartnershipsplayersPath = partnershipsplayers()

    private lateinit var _playersmatches: PlayersmatchesPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.PlayersMatches</code> table
     */
    fun playersmatches(): PlayersmatchesPath {
        if (!this::_playersmatches.isInitialized)
            _playersmatches = PlayersmatchesPath(this, null, PLAYERSMATCHES_IBFK_1.inverseKey)

        return _playersmatches;
    }

    val playersmatches: PlayersmatchesPath
        get(): PlayersmatchesPath = playersmatches()

    private lateinit var _playersofthematchmatches: PlayersofthematchmatchesPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.PlayersOfTheMatchMatches</code> table
     */
    fun playersofthematchmatches(): PlayersofthematchmatchesPath {
        if (!this::_playersofthematchmatches.isInitialized)
            _playersofthematchmatches = PlayersofthematchmatchesPath(this, null, PLAYERSOFTHEMATCHMATCHES_IBFK_1.inverseKey)

        return _playersofthematchmatches;
    }

    val playersofthematchmatches: PlayersofthematchmatchesPath
        get(): PlayersofthematchmatchesPath = playersofthematchmatches()

    private lateinit var _playersteams: PlayersteamsPath

    /**
     * Get the implicit to-many join path to the
     * <code>cricketarchive.PlayersTeams</code> table
     */
    fun playersteams(): PlayersteamsPath {
        if (!this::_playersteams.isInitialized)
            _playersteams = PlayersteamsPath(this, null, PLAYERSTEAMS_IBFK_1.inverseKey)

        return _playersteams;
    }

    val playersteams: PlayersteamsPath
        get(): PlayersteamsPath = playersteams()
    override fun `as`(alias: String): Players = Players(DSL.name(alias), this)
    override fun `as`(alias: Name): Players = Players(alias, this)
    override fun `as`(alias: Table<*>): Players = Players(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Players = Players(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Players = Players(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Players = Players(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition?): Players = Players(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Players = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition?): Players = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>?): Players = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): Players = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): Players = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Players = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Players = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Players = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Players = where(DSL.notExists(select))
}
