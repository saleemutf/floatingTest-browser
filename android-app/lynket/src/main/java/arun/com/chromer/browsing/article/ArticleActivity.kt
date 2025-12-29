/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.browsing.article

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import arun.com.chromer.ui.theme.LynketTheme
import javax.inject.Inject

class ArticleActivity : BrowsingActivity() {
  override fun inject(activityComponent: ActivityComponent) = activityComponent.inject(this)

  private lateinit var browsingArticleViewModel: BrowsingArticleViewModel

  private var url: String? = null

  override val layoutRes: Int get() = 0 // Using setContent

  private var primaryColor: Int = 0
  private var accentColor: Int = 0

  @Inject
  lateinit var tabsManager: TabsManager

  @Inject
  lateinit var menuDelegate: MenuDelegate

  @Inject
  lateinit var searchProviders: SearchProviders

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readCustomizations()
    url = intent.dataString

    observeViewModel()
    if (savedInstanceState == null) {
      browsingArticleViewModel.loadArticle(url!!)
    }

    setContent {
      LynketTheme {
        val articleResult by browsingArticleViewModel.articleLiveData.observeAsState(Result.Loading())
        val textSizeIncrement = preferences.articleTextSizeIncrement()

        ArticleScreen(
          articleResult = articleResult,
          accentColor = if (accentColor != 0) Color(accentColor) else MaterialTheme.colorScheme.primary,
          textSizeIncrementSp = textSizeIncrement,
          onKeywordClick = { keyword ->
            browsingArticleViewModel.selectedSearchProvider
              .take(1)
              .subscribe { searchProvider ->
                tabsManager.openUrl(this@ArticleActivity, Website(searchProvider.getSearchUrl(keyword)))
              }
          },
          onImageClick = { imageUrl ->
            val intent = Intent(this, ImageViewActivity::class.java)
            intent.data = Uri.parse(imageUrl)
            startActivity(intent)
          }
        )
      }
    }
  }

  private fun observeViewModel() {
    browsingArticleViewModel =
      ViewModelProvider(this, viewModelFactory).get(BrowsingArticleViewModel::class.java)
    browsingArticleViewModel.articleLiveData.watch(this) { result ->
      if (result is Result.Failure) {
        onArticleLoadingFailed()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // System UI visibility handled by Activity/Theme
  }

  override fun onWebsiteLoaded(website: Website) {}

  override fun onToolbarColorSet(websiteThemeColor: Int) {
    primaryColor = websiteThemeColor
    accentColor = ContextCompat.getColor(this, R.color.accent)

    if (preferences.dynamiceToolbarEnabledAndWebEnabled() && canUseAsAccentColor(primaryColor)) {
      accentColor = primaryColor
    }
  }

  override fun onCreateOptionsMenu(menu: Menu) = menuDelegate.createOptionsMenu(menu)

  override fun onPrepareOptionsMenu(menu: Menu) = menuDelegate.prepareOptionsMenu(menu)

  override fun onOptionsItemSelected(item: MenuItem) = if (item.itemId == R.id.menu_text_size) {
    // For now, we skip the text size dialog implementation in Compose for simplicity
    // or we could show a standard Material Dialog
    true
  } else menuDelegate.handleItemSelected(item.itemId)

  private fun onArticleLoadingFailed() {
    // Loading failed, try to go back to normal url tab if it exists, else start a new normal
    // rendering tab.
    finish()
    Toast.makeText(this, R.string.article_loading_failed, Toast.LENGTH_SHORT).show()
    tabsManager.openBrowsingTab(
      this,
      Website(intent.dataString!!),
      smart = true,
      fromNewTab = false,
      activityNames = TabsManager.FULL_BROWSING_ACTIVITIES
    )
  }

  private fun setNavigationBarColor(@ColorInt color: Int) {
    window.navigationBarColor = color
  }

  private fun canUseAsAccentColor(primaryColor: Int): Boolean {
    val isDark = preferences.articleTheme() != THEME_LIGHT
    return if (isDark) {
      !ColorUtil.shouldUseLightForegroundOnBackground(primaryColor)
    } else {
      ColorUtil.shouldUseLightForegroundOnBackground(primaryColor)
    }
  }

  private fun readCustomizations() {
    when (preferences.articleTheme()) {
      THEME_LIGHT -> delegate.localNightMode = MODE_NIGHT_NO
      THEME_DARK, THEME_BLACK -> delegate.localNightMode = MODE_NIGHT_YES
      else -> delegate.localNightMode = MODE_NIGHT_AUTO_BATTERY
    }
  }

  companion object {
    private const val MIN_NUM_ELEMENTS = 1
  }
}

