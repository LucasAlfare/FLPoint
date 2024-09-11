package usecase

import com.lucasalfare.flpoint.server.b_usecase.rule.PointUsecasesRules
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPointUsecaseRules {

  @Test
  fun `test isAtMax1MinuteAwayFromServer() rule`() {
    val after1second = Instant.fromEpochMilliseconds(System.currentTimeMillis() + (1 * 1000))
    assertTrue(
      PointUsecasesRules.isAtMax1MinuteAwayFromServer(after1second.toLocalDateTime(TimeZone.currentSystemDefault()))
    )

    val after2seconds = Instant.fromEpochMilliseconds(System.currentTimeMillis() + (2 * 1000))
    assertFalse(
      PointUsecasesRules.isAtMax1MinuteAwayFromServer(after2seconds.toLocalDateTime(TimeZone.currentSystemDefault()))
    )
  }

  @Test
  fun `test passedAtLeast30MinutesFromLast() rule`() {
    booleanArrayOf(true, false).forEach {
      val nextTest = if (it) 30 else 29
      val nowMs = System.currentTimeMillis()

      val last =
        Instant.fromEpochMilliseconds(nowMs - (nextTest * 60 * 1000)).toLocalDateTime(TimeZone.currentSystemDefault())
      val check =
        Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(TimeZone.currentSystemDefault())

      assertEquals(
        expected = it,
        actual = PointUsecasesRules.passedAtLeast30MinutesFromLast(last, check)
      )
    }
  }
}