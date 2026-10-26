package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.localization.BusinessCategoriesEn
import com.example.localization.BusinessCategoriesUr
import com.example.localization.KarobarFormatter
import com.example.localization.KarobarMath
import com.example.localization.KarobarPaymentMethods
import com.example.localization.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context verifies Smart Shop branding`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Shop", appName)
    assertEquals("سمارٹ شاپ", Strings.appName(isUr = true))
    assertEquals("Smart Shop", Strings.appName(isUr = false))
  }

  @Test
  fun `pakistani number formatting correctly displays Rs amounts`() {
    val formatted1k = KarobarFormatter.formatCurrency(1000.0, "Rs.")
    val formatted25k = KarobarFormatter.formatCurrency(25000.0, "Rs.")
    val formatted150k = KarobarFormatter.formatCurrency(150000.0, "Rs.")

    assertEquals("Rs. 1,000", formatted1k)
    assertEquals("Rs. 25,000", formatted25k)
    assertEquals("Rs. 150,000", formatted150k)
  }

  @Test
  fun `decimal-safe math ensures accurate financial computations`() {
    val subtotal = 1000.0
    val tax = KarobarMath.calculateTax(subtotal, 16.0) // 16% sales tax
    val discount = 50.0
    val total = KarobarMath.calculateTotal(subtotal, discount, tax)

    assertEquals(160.0, tax, 0.001)
    assertEquals(1110.0, total, 0.001)

    // Test float precision safety (0.1 + 0.2 precision issue)
    val precisionSum = KarobarMath.add(0.1, 0.2)
    assertEquals(0.3, precisionSum, 0.0001)
  }

  @Test
  fun `pakistani phone numbers validated and normalized`() {
    // Standard 03... format
    val check1 = KarobarFormatter.validatePakistaniPhone("03001234567")
    assertTrue(check1.isValid)
    assertEquals("+923001234567", check1.normalizedE164)
    assertEquals("03001234567", check1.normalizedLocal)

    // Standard +923... format
    val check2 = KarobarFormatter.validatePakistaniPhone("+923001234567")
    assertTrue(check2.isValid)
    assertEquals("+923001234567", check2.normalizedE164)

    // Formats with hyphens or spaces
    val check3 = KarobarFormatter.validatePakistaniPhone("0300-1234567")
    assertTrue(check3.isValid)
    assertEquals("+923001234567", check3.normalizedE164)

    // Invalid numbers
    val checkInvalid = KarobarFormatter.validatePakistaniPhone("12345")
    assertFalse(checkInvalid.isValid)

    val checkInvalidPrefix = KarobarFormatter.validatePakistaniPhone("0421234567")
    assertFalse(checkInvalidPrefix.isValid)
  }

  @Test
  fun `pakistani business categories contains required options`() {
    val required = listOf(
      "General Store",
      "Grocery Store",
      "Garments",
      "Electronics",
      "Cosmetics",
      "Pharmacy",
      "Mobile Accessories",
      "Hardware",
      "Stationery",
      "Restaurant",
      "Other"
    )

    for (cat in required) {
      assertTrue("Missing category: $cat", BusinessCategoriesEn.contains(cat))
    }
    assertEquals(BusinessCategoriesEn.size, BusinessCategoriesUr.size)
  }

  @Test
  fun `pakistani payment methods provides 6 recording options`() {
    val codes = KarobarPaymentMethods.map { it.code }
    assertTrue(codes.contains("CASH"))
    assertTrue(codes.contains("BANK"))
    assertTrue(codes.contains("JAZZCASH"))
    assertTrue(codes.contains("EASYPAISA"))
    assertTrue(codes.contains("CREDIT"))
    assertTrue(codes.contains("OTHER"))
    assertEquals(6, KarobarPaymentMethods.size)
  }
}
