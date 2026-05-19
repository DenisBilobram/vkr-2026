package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.support

object Log4jTemplateSupport {

    const val DEFAULT_PACKAGES: String =
        "ru.vk.recommender.generated,ru.vk.recommender"

    fun disabledDebugAppenderBlock(): String = ""

    fun disabledPerfMetricsAppenderBlock(): String = ""

    fun disabledRuntimeMetricsAppenderBlock(): String = ""

    fun disabledRecommendsAppenderBlock(): String = ""

    fun disabledFeaturesAppenderBlock(): String = ""

    fun disabledFunnelAppenderBlock(): String = ""

    fun disabledRecommendsLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.RecommendItemLogger" level="error" additivity="false"/>
""".trimIndent()

    fun disabledFeaturesLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.FeatureLogger" level="error" additivity="false"/>
""".trimIndent()

    fun disabledFunnelLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.WebLogger" level="error" additivity="false"/>
""".trimIndent()

    fun disabledRuntimeMetricsLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.RuntimeMetricsLogger" level="error" additivity="false"/>
""".trimIndent()

    fun disabledPerfMetricsLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.PerformanceMetricsLogger" level="error" additivity="false"/>
""".trimIndent()

    fun recommendsAppenderBlock(): String = """
        <RollingRandomAccessFile name="Recommends" fileName="${'$'}{baseDir}/recommends-proto-base64.log"
                                 filePattern="${'$'}{archiveDir}/recommends-proto-base64.log.%i"
                                 immediateFlush="false" bufferSize="134217728">
            <FeatureLogProtoBase64Layout message="ru.vk.recommender.generated.RecommendItemLogEntry"/>
            <Policies>
                <ManualTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="1 GB"/>
            </Policies>
            <DefaultRolloverStrategy max="5" fileIndex="min"/>
        </RollingRandomAccessFile>
""".trimIndent()

    fun featuresAppenderBlock(): String = """
        <RollingRandomAccessFile name="ProtoseqFeatures" fileName="${'$'}{baseDir}/proto-features-base64.log"
                                 filePattern="${'$'}{archiveDir}/proto-features-base64.log.%i"
                                 immediateFlush="false" bufferSize="134217728">
            <FeatureLogProtoBase64Layout message="ru.vk.recommender.generated.FeaturesLogEntry"/>
            <Policies>
                <ManualTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="1 GB"/>
            </Policies>
            <DefaultRolloverStrategy max="5" fileIndex="min"/>
        </RollingRandomAccessFile>
""".trimIndent()

    fun funnelAppenderBlock(): String = """
        <RollingRandomAccessFile name="FunnelLogger" fileName="${'$'}{baseDir}/funnel.log"
                                 filePattern="${'$'}{archiveDir}/funnel.log.%i"
                                 immediateFlush="false" bufferSize="10000000">
            <FeatureLogProtoBase64Layout message="ru.vk.recommender.generated.WebRequestLogMessage"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="500 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="5" fileIndex="min"/>
        </RollingRandomAccessFile>
""".trimIndent()

    fun recommendsLoggerBlock(loggerName: String = "ru.vk.recommender.generated.RecommendItemLogger"): String = """
        <Logger name="$loggerName" level="info" additivity="false">
            <AppenderRef ref="Recommends"/>
        </Logger>
""".trimIndent()

    fun featuresLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.FeatureLogger" level="info" additivity="false">
            <AppenderRef ref="ProtoseqFeatures"/>
        </Logger>
""".trimIndent()

    fun funnelLoggerBlock(): String = """
        <Logger name="ru.vk.recommender.generated.WebLogger" level="info" additivity="false">
            <AppenderRef ref="FunnelLogger"/>
        </Logger>
""".trimIndent()

    fun dragonflyDockerfileFragment(): String = """
# NDA code removed: internal image distribution bootstrap was stripped for publication.
""".trimIndent()

}
