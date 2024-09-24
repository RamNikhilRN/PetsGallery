package com.example.petsgallery

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PetGalleryUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testSortButtonsTriggerCallbacks() {
        var sortAscending = true
        composeTestRule.setContent {
            SortButtons(sortAscending) { newSortAscending ->
                sortAscending = newSortAscending
            }
        }

        // Check initial state (A-Z should be enabled)
        composeTestRule.onNodeWithText("A-Z").assertIsEnabled()
        composeTestRule.onNodeWithText("Z-A").assertIsEnabled()

        // Click A-Z button
        composeTestRule.onNodeWithText("A-Z").performClick()
        composeTestRule.waitForIdle()
        Assert.assertTrue(sortAscending)

        // Click Z-A button
        composeTestRule.onNodeWithText("Z-A").performClick()
        composeTestRule.waitForIdle()
        Assert.assertFalse(sortAscending)
    }

    @Composable
    fun SortButtons(sortAscending: Boolean, onSortChange: (Boolean) -> Unit) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { onSortChange(true) }) {
                Text("A-Z")
            }
            Button(onClick = { onSortChange(false) }) {
                Text("Z-A")
            }
        }
    }
}
