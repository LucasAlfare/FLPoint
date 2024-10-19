package rules

import com.lucasalfare.flpoint.server.instantIsAtLeast30MinutesAwayFromLast
import com.lucasalfare.flpoint.server.instantIsInValidTimeInterval
import getSomeUser
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// 2024-10-16T00:47:42.643891Z
class RulesTests {

  @Test
  fun `test instantIsAtLeast30MinutesAwayFromLast() success`() {
    val last = Instant.parse("2024-10-16T08:00:00.00Z")
    val current = Instant.parse("2024-10-16T08:40:00.00Z")

    val result = instantIsAtLeast30MinutesAwayFromLast(current, last)
    assertTrue(result)
  }

  @Test
  fun `test instantIsAtLeast30MinutesAwayFromLast() failure`() {
    val last = Instant.parse("2024-10-16T08:00:00.00Z")
    val current = Instant.parse("2024-10-16T08:10:00.00Z")

    val result = instantIsAtLeast30MinutesAwayFromLast(current, last)
    assertFalse(result)
  }

  @Test
  fun `test instantIsInValidTimeInterval() success`() {
    val user = getSomeUser()
    val now = Clock.System.now().toLocalDateTime(user.timeZone)
    val check = Instant.parse("2024-10-16T${(now.hour) + 3}:00:00.00Z")
    val result = instantIsInValidTimeInterval(check, user)
    assertTrue(result)
  }

  @Test
  fun `test instantIsInValidTimeInterval() failure`() {
    val user = getSomeUser()
    val check = Instant.parse("2024-10-16T${7 + 3}:00:00.00Z")
    val result = instantIsInValidTimeInterval(check, user)
    assertFalse(result)
  }
}