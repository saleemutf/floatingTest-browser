package arun.com.chromer.settings.browsingmode

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import arun.com.chromer.R
import arun.com.chromer.settings.Preferences
import arun.com.chromer.settings.RxPreferences
import arun.com.chromer.shared.base.activity.BaseActivity
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.ui.theme.LynketTheme
import arun.com.chromer.util.Utils
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import javax.inject.Inject

class BrowsingModeActivity : BaseActivity() {

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var tabsManager: TabsManager

    override val layoutRes: Int = 0 // Using setContent

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LynketTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.title_activity_browsing_mode)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        val isWebHeadsEnabled = Preferences.get(this@BrowsingModeActivity).webHeads()
                        val isNativeBubblesEnabled = rxPreferences.nativeBubbles.get()

                        val items = remember(isWebHeadsEnabled, isNativeBubblesEnabled) {
                            listOf(
                                BrowsingModeItem(
                                    title = getString(R.string.browsing_mode_slide_over),
                                    subtitle = getString(R.string.browsing_mode_slide_over_explanation),
                                    backgroundColor = Color(0xFF00B0FF), // md_light_blue_A700
                                    contentColor = Color.White,
                                    isSelected = !isWebHeadsEnabled && !isNativeBubblesEnabled
                                ),
                                BrowsingModeItem(
                                    title = getString(R.string.browsing_mode_web_heads),
                                    subtitle = getString(R.string.browsing_mode_web_heads_explanation),
                                    backgroundColor = Color(0xFF388E3C), // md_green_700
                                    contentColor = Color.White,
                                    isSelected = isWebHeadsEnabled
                                ),
                                BrowsingModeItem(
                                    title = getString(R.string.browsing_mode_native_bubbles),
                                    subtitle = getString(R.string.browsing_mode_native_bubbles_explanation),
                                    backgroundColor = Color(0xFF00ACC1), // android_10_color
                                    contentColor = Color(0xFF212121), // material_dark_color
                                    isSelected = isNativeBubblesEnabled
                                )
                            )
                        }

                        BrowsingModeScreen(
                            items = items,
                            onItemClick = { position -> onModeClicked(position) }
                        )

                        // For the preference fragment, we'll host it in a FragmentContainerView
                        AndroidView(
                            factory = { context ->
                                FragmentContainerView(context).apply {
                                    id = R.id.browse_faster_preferences_container
                                    supportFragmentManager.commit {
                                        replace(id, BrowseFasterPreferenceFragment.newInstance())
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    private fun onModeClicked(position: Int) {
        val webHeadsEnabled = position == 1
        val nativeBubbles = position == 2

        rxPreferences.nativeBubbles.set(nativeBubbles)
        Preferences.get(this).webHeads(webHeadsEnabled)

        if (webHeadsEnabled) {
            if (Utils.isOverlayGranted(this)) {
                Preferences.get(this).webHeads(true)
            } else {
                Preferences.get(this).webHeads(false)
                val snackbar = Snackbar.make(window.decorView, R.string.overlay_permission_content, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(R.string.grant) {
                    snackbar.dismiss()
                    Utils.openDrawOverlaySettings(this@BrowsingModeActivity)
                }
                snackbar.show()
            }
        } else if (nativeBubbles) {
            MaterialDialog.Builder(this)
                .title(R.string.browsing_mode_native_bubbles)
                .content(R.string.browsing_mode_native_bubbles_warning)
                .positiveText(R.string.browsing_mode_native_bubbles_guide)
                .onPositive { _, _ ->
                    tabsManager.openUrl(
                        this,
                        arun.com.chromer.data.website.model.Website("https://github.com/arunkumar9t2/lynket-browser/wiki/Android-10-Bubbles-Guide"),
                        true,
                        false,
                        false,
                        false,
                        false
                    )
                }
                .icon(
                    IconicsDrawable(this)
                        .icon(CommunityMaterial.Icon.cmd_android_head)
                        .colorRes(R.color.material_dark_color)
                        .sizeDp(24)
                ).show()
        }
        // Force recompose by refreshing activity (simple way for now without full state management)
        recreate()
    }
}

