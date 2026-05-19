package ru.vk.recommender.sre.discoveryportalflow.config

import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.convert.ReadingConverter

@Configuration
class JdbcConvertersConfig {

    @Bean
    fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                JsonbReadConverter(),
            )
        )

    @ReadingConverter
    class JsonbReadConverter : Converter<PGobject, String> {
        override fun convert(source: PGobject): String =
            source.value ?: "{}"
    }
}
