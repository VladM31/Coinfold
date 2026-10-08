package com.vm.coinfold.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.vm.coinfold.app.shared.launch.LaunchAction
import com.vm.coinfold.app.shared.launch.LaunchActions

// FragmentActivity (a ComponentActivity subclass) is what BiometricPrompt requires.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Only on a fresh start: after a rotation the same intent must not open the sheet again.
        if (savedInstanceState == null) handleLaunchIntent(intent)

        setContent {
            App()
        }
    }

    // singleTask: a shortcut or the widget tapped while the app is open arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        LaunchAction.fromKey(intent?.getStringExtra(EXTRA_ACTION))?.let(LaunchActions::post)
    }

    companion object {
        const val EXTRA_ACTION = "coinfold_action"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
