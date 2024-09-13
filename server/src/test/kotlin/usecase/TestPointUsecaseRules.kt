package usecase

import com.lucasalfare.flpoint.server.b_usecase.rule.PointUsecasesRules
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TestPointUsecaseRules {

  @Test
  fun `test time within valid range`() {
    val now = Clock.System.now()
    val withinValidRange = now - 5.seconds // 5 seconds in the past
    val checkTime = withinValidRange
    assertTrue(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time exactly 1 second ahead`() {
    val now = Clock.System.now()
    val exactly1SecondInFuture = now + 1.seconds // 1 second in the future
    val checkTime = exactly1SecondInFuture
    assertTrue(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time more than 1 second ahead`() {
    val now = Clock.System.now()
    val moreThan1SecondInFuture = now + 2.seconds // 2 seconds in the future
    val checkTime = moreThan1SecondInFuture
    assertFalse(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time more than 10 seconds in the past`() {
    val now = Clock.System.now()
    val moreThan10SecondsInPast = now - 11.seconds // 11 seconds in the past
    val checkTime = moreThan10SecondsInPast
    assertFalse(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test time exactly 10 seconds in the past`() {
    val now = Clock.System.now()
    val exactly10SecondsPast = now - 10.seconds // Exactly 10 seconds in the past
    val checkTime = exactly10SecondsPast
    assertTrue(PointUsecasesRules.isWithinValidTimeRange(checkTime))
  }

  @Test
  fun `test passedAtLeast30MinFromLast() rule`() {
    // tests a valid and invalid amount of minutes
    booleanArrayOf(true, false).forEach {
      val nextTest = if (it) 30 else 29
      val now = Clock.System.now()
      val last = now - (nextTest.minutes)
      assertEquals(
        expected = it,
        actual = PointUsecasesRules.passedAtLeast30MinFromLast(last, now)
      )
    }
  }
}