package rules

import com.lucasalfare.flpoint.server.instantIsAtLeast10SecondsAwayFromLast
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

// 2024-10-16T00:47:42.643891Z
class RulesTests {

  @Test
  fun `test instantIsAtLeast30MinutesAwayFromLast() success`() {
    val last = Instant.parse("2024-10-16T08:00:00.00Z")
    val current = Instant.parse("2024-10-16T08:40:00.00Z")

    val result = instantIsAtLeast10SecondsAwayFromLast(current, last)
    assertTrue(result)
  }

  @Test
  fun `test instantIsAtLeast30MinutesAwayFromLast() failure`() {
    val last = Instant.parse("2024-10-16T08:00:00.00Z")
    val current = Instant.parse("2024-10-16T08:10:00.00Z")

    val result = instantIsAtLeast10SecondsAwayFromLast(current, last)
    assertFalse(result)
  }
}