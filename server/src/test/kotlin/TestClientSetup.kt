import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

fun ApplicationTestBuilder.customSetupTestClient(): HttpClient {
  application {
//    initKtorConfiguration(dataHandler = InMemoryDataHandler)
  }

  return createClient {
    install(ContentNegotiation) {
      json(Json { isLenient = false })
    }
  }
}