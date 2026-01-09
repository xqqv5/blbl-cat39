package blbl.cat3399.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import blbl.cat3399.R
import blbl.cat3399.core.api.BiliApi
import blbl.cat3399.core.log.AppLog
import blbl.cat3399.core.net.BiliClient
import blbl.cat3399.core.tv.TvMode
import blbl.cat3399.core.ui.Immersive
import blbl.cat3399.databinding.ActivityMainBinding
import blbl.cat3399.feature.category.CategoryFragment
import blbl.cat3399.feature.dynamic.DynamicFragment
import blbl.cat3399.feature.home.HomeFragment
import blbl.cat3399.feature.login.QrLoginActivity
import blbl.cat3399.feature.settings.SettingsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navAdapter: SidebarNavAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Immersive.apply(this, BiliClient.prefs.fullscreenEnabled)
        applyUiMode()

        binding.btnSidebarLogin.setOnClickListener { openQrLogin() }
        binding.ivSidebarUser.setOnClickListener {
            if (!BiliClient.cookies.hasSessData()) {
                openQrLogin()
                return@setOnClickListener
            }
            UserInfoDialogFragment.show(supportFragmentManager)
        }
        binding.btnSidebarSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        navAdapter = SidebarNavAdapter(
            onClick = { item ->
                when (item.id) {
                    SidebarNavAdapter.ID_HOME -> showRoot(HomeFragment.newInstance())
                    SidebarNavAdapter.ID_CATEGORY -> showRoot(CategoryFragment.newInstance())
                    SidebarNavAdapter.ID_DYNAMIC -> showRoot(DynamicFragment.newInstance())
                    else -> false
                }
            },
        )
        binding.recyclerSidebar.layoutManager = LinearLayoutManager(this)
        binding.recyclerSidebar.adapter = navAdapter
        navAdapter.submit(
            listOf(
                SidebarNavAdapter.NavItem(SidebarNavAdapter.ID_HOME, getString(R.string.tab_recommend), R.drawable.ic_nav_home),
                SidebarNavAdapter.NavItem(SidebarNavAdapter.ID_CATEGORY, getString(R.string.tab_category), R.drawable.ic_nav_category),
                SidebarNavAdapter.NavItem(SidebarNavAdapter.ID_DYNAMIC, getString(R.string.tab_dynamic), R.drawable.ic_nav_dynamic),
            ),
            selectedId = SidebarNavAdapter.ID_HOME,
        )

        if (savedInstanceState == null) {
            navAdapter.select(SidebarNavAdapter.ID_HOME, trigger = true)
        }

        refreshSidebarUser()
    }

    override fun onResume() {
        super.onResume()
        applyUiMode()
        ensureInitialFocus()
        refreshSidebarUser()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) Immersive.apply(this, BiliClient.prefs.fullscreenEnabled)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && currentFocus == null && isNavKey(event.keyCode)) {
            ensureInitialFocus()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showRoot(fragment: androidx.fragment.app.Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .commitAllowingStateLoss()
        return true
    }

    private fun openQrLogin() {
        AppLog.i("MainActivity", "openQrLogin")
        startActivity(Intent(this, QrLoginActivity::class.java))
    }

    private fun refreshSidebarUser() {
        val hasCookie = BiliClient.cookies.hasSessData()
        if (!hasCookie) {
            showLoggedOut()
            return
        }
        lifecycleScope.launch {
            runCatching {
                val nav = BiliApi.nav()
                val data = nav.optJSONObject("data")
                val isLogin = data?.optBoolean("isLogin") ?: false
                val avatarUrl = data?.optString("face")?.takeIf { it.isNotBlank() }
                if (isLogin) showLoggedIn(avatarUrl) else showLoggedOut()
            }.onFailure {
                AppLog.w("MainActivity", "refreshSidebarUser failed", it)
            }
        }
    }

    private fun showLoggedIn(avatarUrl: String?) {
        binding.btnSidebarLogin.visibility = android.view.View.GONE
        binding.ivSidebarUser.visibility = android.view.View.VISIBLE
        blbl.cat3399.core.image.ImageLoader.loadInto(binding.ivSidebarUser, avatarUrl)
    }

    private fun showLoggedOut() {
        binding.ivSidebarUser.visibility = android.view.View.GONE
        binding.btnSidebarLogin.visibility = android.view.View.VISIBLE
    }

    private fun applyUiMode() {
        val tvMode = TvMode.isEnabled(this)
        if (::navAdapter.isInitialized) navAdapter.setShowLabelsAlways(tvMode)

        val widthDp = if (tvMode) 160f else 48f
        val widthPx = dp(widthDp)
        val lp = binding.sidebar.layoutParams
        if (lp.width != widthPx) {
            lp.width = widthPx
            binding.sidebar.layoutParams = lp
        }
    }

    private fun ensureInitialFocus() {
        if (currentFocus != null) return
        val pos = navAdapter.selectedAdapterPosition().takeIf { it >= 0 } ?: 0
        binding.recyclerSidebar.post {
            val vh = binding.recyclerSidebar.findViewHolderForAdapterPosition(pos)
            if (vh != null) {
                vh.itemView.requestFocus()
                return@post
            }
            binding.recyclerSidebar.scrollToPosition(pos)
            binding.recyclerSidebar.post {
                binding.recyclerSidebar.findViewHolderForAdapterPosition(pos)?.itemView?.requestFocus()
            }
        }
    }

    private fun dp(valueDp: Float): Int {
        val dm = resources.displayMetrics
        return (valueDp * dm.density).toInt()
    }

    private fun isNavKey(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_TAB,
            -> true

            else -> false
        }
    }
}
