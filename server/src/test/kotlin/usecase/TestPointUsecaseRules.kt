package usecase

import com.lucasalfare.flpoint.server.b_usecase.rule.PointUsecasesRules
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPointUsecaseRules {

  @Test
  fun `test time within valid range`() {
    val now = Clock.System.now()
    val withinValidRange = now.minus(5, DateTimeUnit.SECOND) // 5 seconds in the past
    val checkTime = withinValidRange
    assertTrue(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time exactly 1 second ahead`() {
    val now = Clock.System.now()
    val exactly1SecondInFuture = now.plus(1, DateTimeUnit.SECOND) // 1 second in the future
    val checkTime = exactly1SecondInFuture
    assertTrue(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time more than 1 second ahead`() {
    val now = Clock.System.now()
    val moreThan1SecondInFuture = now.plus(2, DateTimeUnit.SECOND) // 2 seconds in the future
    val checkTime = moreThan1SecondInFuture
    assertFalse(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time more than 10 seconds in the past`() {
    val now = Clock.System.now()
    val moreThan10SecondsInPast = now.minus(11, DateTimeUnit.SECOND) // 11 seconds in the past
    val checkTime = moreThan10SecondsInPast

    assertFalse(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time exactly 10 seconds in the past`() {
    val now = Clock.System.now()
    val exactly10SecondsPast = now.minus(10, DateTimeUnit.SECOND) // Exactly 10 seconds in the past
    val checkTime = exactly10SecondsPast
    assertTrue(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test passedAtLeast30MinutesFromLast() rule`() {
    booleanArrayOf(true, false).forEach {
      val nextTest = if (it) 30 else 29
      val nowMs = System.currentTimeMillis()

      val last = Instant.fromEpochMilliseconds(nowMs - (nextTest * 60 * 1000))
      val check = Instant.fromEpochMilliseconds(nowMs)

      assertEquals(
        expected = it,
        actual = PointUsecasesRules.passedAtLeast30MinutesFromLast(last, check)
      )
    }
  }
}