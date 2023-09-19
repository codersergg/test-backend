package mobi.sevenwinds.modules

import com.papsign.ktor.openapigen.APITag
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import io.ktor.application.*
import mobi.sevenwinds.Const
import kotlin.reflect.KType

fun Application.initSwagger() {

    install(OpenAPIGen) {
        // basic info
        info {
            version = Const.version
            title = "Construction Dashboard"
            description = "Backend API"
        }

        environment.config.configList("swagger.servers")
            .forEach {
                val url = it.property("url").getString().trimEnd('/')
                server(url) {
                    description = it.property("description").getString()
                }
                println("Swagger available at $url/swagger-ui/index.html?url=/openapi.json")
            }

        //optional custom schema object namer
        replaceModule(DefaultSchemaNamer, object : SchemaNamer {
            val regex = Regex("[A-Za-z0-9_.]+")
            override fun get(type: KType): String {
                return type.toString().replace(regex) { it.value.split(".").last() }.replace(Regex(">|<|, "), "_")
            }
        })
    }
}

@Suppress("NonAsciiCharacters", "EnumEntryName")
enum class SwaggerTag(override val description: String = "") : APITag {
    Бюджет,
    Автор;
}