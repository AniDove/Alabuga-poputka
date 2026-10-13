package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppStorage
import com.example.data.DemoData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Попутка Алабуга", appName)
  }

  @Test
  fun `demo state initialized and persistent`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = AppStorage(context)
    val state = storage.loadInitialState()

    assertNotNull(state)
    assertTrue("Should have initial demo rides", state.rides.isNotEmpty())
    assertTrue("Should have initial demo users", state.users.isNotEmpty())
    assertTrue("Should have popular Yelabuga locations", state.locations.isNotEmpty())
  }
}
