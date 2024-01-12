package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

        /**
         * Creates a dependency provider for commonsCli (commons-cli:commons-cli)
     * with versionRef 'commons.cli.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCommonsCli() {
            return create("commonsCli");
    }

        /**
         * Creates a dependency provider for jooq (org.jooq:jooq)
     * with versionRef 'jooq.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJooq() {
            return create("jooq");
    }

        /**
         * Creates a dependency provider for jooqCodeGen (org.jooq:jooq-codegen)
     * with versionRef 'jooq.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJooqCodeGen() {
            return create("jooqCodeGen");
    }

        /**
         * Creates a dependency provider for jooqMeta (org.jooq:jooq-meta)
     * with versionRef 'jooq.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJooqMeta() {
            return create("jooqMeta");
    }

        /**
         * Creates a dependency provider for kotlinCoroutines (org.jetbrains.kotlinx:kotlinx-coroutines-core)
     * with versionRef 'kotlin.couroutines.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinCoroutines() {
            return create("kotlinCoroutines");
    }

        /**
         * Creates a dependency provider for kotlinReflect (org.jetbrains.kotlin:kotlin-reflect)
     * with version '1.9.20-Beta2'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinReflect() {
            return create("kotlinReflect");
    }

        /**
         * Creates a dependency provider for kotlinxDatetime (org.jetbrains.kotlinx:kotlinx-datetime)
     * with versionRef 'kotlinx.datetime.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinxDatetime() {
            return create("kotlinxDatetime");
    }

        /**
         * Creates a dependency provider for kotlinxHtml (org.jetbrains.kotlinx:kotlinx-html)
     * with versionRef 'kotlinx.html.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinxHtml() {
            return create("kotlinxHtml");
    }

        /**
         * Creates a dependency provider for kotlinxHtmlJvm (org.jetbrains.kotlinx:kotlinx-html-jvm)
     * with versionRef 'kotlinx.html.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinxHtmlJvm() {
            return create("kotlinxHtmlJvm");
    }

        /**
         * Creates a dependency provider for kotlinxSerialization (org.jetbrains.kotlinx:kotlinx-serialization-json)
     * with versionRef 'kotlinx.serialization.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKotlinxSerialization() {
            return create("kotlinxSerialization");
    }

        /**
         * Creates a dependency provider for logback (ch.qos.logback:logback-classic)
     * with versionRef 'logback.version'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLogback() {
            return create("logback");
    }

        /**
         * Creates a dependency provider for mariadb (org.mariadb.jdbc:mariadb-java-client)
     * with versionRef 'mariaDbVersion'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMariadb() {
            return create("mariadb");
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class VersionAccessors extends VersionFactory  {

        private final CatalogUpdateVersionVersionAccessors vaccForCatalogUpdateVersionVersionAccessors = new CatalogUpdateVersionVersionAccessors(providers, config);
        private final CommonsVersionAccessors vaccForCommonsVersionAccessors = new CommonsVersionAccessors(providers, config);
        private final JooqVersionAccessors vaccForJooqVersionAccessors = new JooqVersionAccessors(providers, config);
        private final KotlinVersionAccessors vaccForKotlinVersionAccessors = new KotlinVersionAccessors(providers, config);
        private final KotlinxVersionAccessors vaccForKotlinxVersionAccessors = new KotlinxVersionAccessors(providers, config);
        private final LogbackVersionAccessors vaccForLogbackVersionAccessors = new LogbackVersionAccessors(providers, config);
        private final UpdateVersionVersionAccessors vaccForUpdateVersionVersionAccessors = new UpdateVersionVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: mariaDbVersion (3.3.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getMariaDbVersion() { return getVersion("mariaDbVersion"); }

        /**
         * Returns the group of versions at versions.catalogUpdateVersion
         */
        public CatalogUpdateVersionVersionAccessors getCatalogUpdateVersion() {
            return vaccForCatalogUpdateVersionVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.commons
         */
        public CommonsVersionAccessors getCommons() {
            return vaccForCommonsVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.jooq
         */
        public JooqVersionAccessors getJooq() {
            return vaccForJooqVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.kotlin
         */
        public KotlinVersionAccessors getKotlin() {
            return vaccForKotlinVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.kotlinx
         */
        public KotlinxVersionAccessors getKotlinx() {
            return vaccForKotlinxVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.logback
         */
        public LogbackVersionAccessors getLogback() {
            return vaccForLogbackVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.updateVersion
         */
        public UpdateVersionVersionAccessors getUpdateVersion() {
            return vaccForUpdateVersionVersionAccessors;
        }

    }

    public static class CatalogUpdateVersionVersionAccessors extends VersionFactory  {

        public CatalogUpdateVersionVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: catalogUpdateVersion.version (0.8.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("catalogUpdateVersion.version"); }

    }

    public static class CommonsVersionAccessors extends VersionFactory  {

        private final CommonsCliVersionAccessors vaccForCommonsCliVersionAccessors = new CommonsCliVersionAccessors(providers, config);
        public CommonsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.commons.cli
         */
        public CommonsCliVersionAccessors getCli() {
            return vaccForCommonsCliVersionAccessors;
        }

    }

    public static class CommonsCliVersionAccessors extends VersionFactory  {

        public CommonsCliVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: commons.cli.version (1.6.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("commons.cli.version"); }

    }

    public static class JooqVersionAccessors extends VersionFactory  {

        public JooqVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jooq.version (3.18.8)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("jooq.version"); }

    }

    public static class KotlinVersionAccessors extends VersionFactory  {

        private final KotlinCouroutinesVersionAccessors vaccForKotlinCouroutinesVersionAccessors = new KotlinCouroutinesVersionAccessors(providers, config);
        public KotlinVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotlin.version (1.9.22)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("kotlin.version"); }

        /**
         * Returns the group of versions at versions.kotlin.couroutines
         */
        public KotlinCouroutinesVersionAccessors getCouroutines() {
            return vaccForKotlinCouroutinesVersionAccessors;
        }

    }

    public static class KotlinCouroutinesVersionAccessors extends VersionFactory  {

        public KotlinCouroutinesVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotlin.couroutines.version (1.7.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("kotlin.couroutines.version"); }

    }

    public static class KotlinxVersionAccessors extends VersionFactory  {

        private final KotlinxDatetimeVersionAccessors vaccForKotlinxDatetimeVersionAccessors = new KotlinxDatetimeVersionAccessors(providers, config);
        private final KotlinxHtmlVersionAccessors vaccForKotlinxHtmlVersionAccessors = new KotlinxHtmlVersionAccessors(providers, config);
        private final KotlinxSerializationVersionAccessors vaccForKotlinxSerializationVersionAccessors = new KotlinxSerializationVersionAccessors(providers, config);
        public KotlinxVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.kotlinx.datetime
         */
        public KotlinxDatetimeVersionAccessors getDatetime() {
            return vaccForKotlinxDatetimeVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.kotlinx.html
         */
        public KotlinxHtmlVersionAccessors getHtml() {
            return vaccForKotlinxHtmlVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.kotlinx.serialization
         */
        public KotlinxSerializationVersionAccessors getSerialization() {
            return vaccForKotlinxSerializationVersionAccessors;
        }

    }

    public static class KotlinxDatetimeVersionAccessors extends VersionFactory  {

        public KotlinxDatetimeVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotlinx.datetime.version (0.5.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("kotlinx.datetime.version"); }

    }

    public static class KotlinxHtmlVersionAccessors extends VersionFactory  {

        public KotlinxHtmlVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotlinx.html.version (0.10.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("kotlinx.html.version"); }

    }

    public static class KotlinxSerializationVersionAccessors extends VersionFactory  {

        public KotlinxSerializationVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotlinx.serialization.version (1.6.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("kotlinx.serialization.version"); }

    }

    public static class LogbackVersionAccessors extends VersionFactory  {

        public LogbackVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: logback.version (1.4.14)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("logback.version"); }

    }

    public static class UpdateVersionVersionAccessors extends VersionFactory  {

        public UpdateVersionVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: updateVersion.version (0.50.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVersion() { return getVersion("updateVersion.version"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for catalogUpdate to the plugin id 'nl.littlerobots.version-catalog-update'
             * with versionRef 'catalogUpdateVersion.version'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getCatalogUpdate() { return createPlugin("catalogUpdate"); }

            /**
             * Creates a plugin provider for jooq to the plugin id 'org.jooq.jooq-codegen-gradle'
             * with version '3.19.1'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getJooq() { return createPlugin("jooq"); }

            /**
             * Creates a plugin provider for kotlinJvm to the plugin id 'org.jetbrains.kotlin.jvm'
             * with versionRef 'kotlin.version'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKotlinJvm() { return createPlugin("kotlinJvm"); }

            /**
             * Creates a plugin provider for kotlinSerialization to the plugin id 'org.jetbrains.kotlin.plugin.serialization'
             * with versionRef 'kotlin.version'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKotlinSerialization() { return createPlugin("kotlinSerialization"); }

            /**
             * Creates a plugin provider for versionUpdate to the plugin id 'com.github.ben-manes.versions'
             * with versionRef 'updateVersion.version'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getVersionUpdate() { return createPlugin("versionUpdate"); }

    }

}
