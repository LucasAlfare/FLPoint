package exposed_data_crud

import com.lucasalfare.flpoint.server.ExposedDataCRUD
import com.lucasalfare.flpoint.server.Users
import disposeTestingDatabase
import getSomeUser
import initTestingDatabase
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedDataCRUDTests {

  @BeforeTest
  fun setup() {
    initTestingDatabase()
  }

  @AfterTest
  fun dispose() {
    disposeTestingDatabase()
  }

  @Test
  fun `test createUser() success`() {
    runBlocking {
      val someUser = getSomeUser()

      ExposedDataCRUD.createUser(
        name = someUser.name,
        email = someUser.email,
        hashedPassword = someUser.hashedPassword,
        timeIntervals = someUser.timeIntervals,
        timeZone = someUser.timeZone,
        isAdmin = someUser.isAdmin
      )

      transaction {
        val result = Users.selectAll().singleOrNull()
        assertEquals(someUser.id, result?.get(Users.id)?.value)
        assertEquals(someUser.name, result?.get(Users.name))
        assertEquals(someUser.email, result?.get(Users.email))
        assertEquals(someUser.hashedPassword, result?.get(Users.hashedPassword))

        // TODO: correct assert content equals
//        assertEquals(someUser.timeIntervals, TimeInterval.fromStringList(result?.get(Users.timeIntervalsStringList)))

        assertEquals(someUser.timeZone.toString(), result?.get(Users.timeZone))
        assertEquals(someUser.isAdmin, result?.get(Users.isAdmin))
      }
    }
  }
}