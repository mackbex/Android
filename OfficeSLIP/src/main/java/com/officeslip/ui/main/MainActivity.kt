package com.officeslip.ui.main

import android.content.Intent
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView
import com.officeslip.*
import com.officeslip.base.BaseActivity
import com.officeslip.databinding.ActivityMainBinding
import com.officeslip.ui.addslip.AddSlipFragment
import com.officeslip.ui.login.LoginActivity
import com.officeslip.ui.qr.QRScanFragment
import com.officeslip.ui.search.SearchSlipFragment
import com.officeslip.ui.setting.SettingFragment
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private val m_C = Common()
    override val layoutResourceId: Int
        get() = R.layout.activity_main
    override val viewModel by viewModels<MainViewModel>()
    private lateinit var fragToSet: Fragment



    override fun initStartView() {

        binding.activity = this
        binding.sharedViewModel = viewModels<SharedMainViewModel>().value

        setViewPager()
        setupNavigation()
    }

    override fun initDataBinding() {


        viewModel.naviFlag.observe(this, Observer {
            with(binding) {
                if (layoutDrawer.isDrawerOpen(GravityCompat.START)) {
                    layoutDrawer.closeDrawer(GravityCompat.START)
                } else {
                    layoutDrawer.openDrawer(GravityCompat.START)
                }
            }
        })

        viewModel.logout.observe(this@MainActivity, Observer {
            Intent(this@MainActivity, LoginActivity::class.java).run {
                startActivity(this)
            }
            this.finish()
        })
        viewModel.pagerFlag.observe(this@MainActivity, {
//            when(it) {
//                PageType.MAIN -> {
//                    Intent(this@MainActivity, LoginActivity::class.java).run {
//                        startActivity(this)
//                    }
//                    this.finish()
//                }
//                else -> {}
//            }

            movePager(it)
        })
    }

    private fun movePager(flag: PageType) {

        binding.pager.let { vp ->
            (vp.adapter as MainViewPagerAdapter).run {

                var naviChecked = 0
                var currentTag = ""
                when (flag) {

                    PageType.ADD_SLIP-> {
                        naviChecked = R.id.menu_add_slip
                        currentTag = FRAG_ADD_SLIP
                    }
                    PageType.SEARCH_SLIP -> {
                        naviChecked = R.id.menu_search_slip
                        currentTag = FRAG_SEARCH_SLIP
                    }
                    PageType.QR -> {
                        naviChecked = R.id.menu_qr
                        currentTag = FRAG_QR
                    }
                    PageType.SETTING -> {
                        naviChecked = R.id.menu_setting
                        currentTag = FRAG_SETTING
                    }

                }

                vp.setCurrentItem(getFragItemAtPosition(currentTag), false)
                binding.naviMain.setCheckedItem(naviChecked)
                binding.layoutDrawer.closeDrawer(GravityCompat.START)

//                CoroutineScope(IO).launch {
//                    delay(500L)
                binding.sharedViewModel?.currentPageType?.postValue(flag)

//                }
            }
        }
    }

    override fun initAfterBinding() {
//        m_C.removeFolder(this@MainActivity, TEMP_PATH)
//        m_C.removeFolder(this@MainActivity, UPLOAD_PATH)
//        m_C.removeFolder(this@MainActivity, DOWNLOAD_PATH)
    }

    fun confirmLogout() {
        m_C.simpleConfirm(this@MainActivity, null, getString(R.string.logout_confirm), {
            viewModel.onLogout()
        }, { })
    }

    private fun setupNavigation() {
//        val navController = findNavController(R.id.nav_host_fragment)
        // get fragment
        //  val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
        // setup custom navigator
//        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)
//        navController.navigatorProvider += navigator
        // set navigation graph
//        navController.setGraph(R.navigation.main_navigation)

        with(binding) {
            naviMain.setNavigationItemSelectedListener(this@MainActivity)
//        navi_main.menu.getItem(0).isCheckable = true
//        navi_main.menu.getItem(0).isChecked = true
            naviMain.setCheckedItem(R.id.menu_add_slip)

            with(naviHeader.getHeaderView(0)) {
                findViewById<TextView>(R.id.view_textUserName).text = SysInfo.userInfo[userNm]?.asString
                findViewById<TextView>(R.id.view_textUserID).text = SysInfo.userInfo[userId]?.asString
                findViewById<TextView>(R.id.view_textUserInfo).text = "${SysInfo.userInfo[partNm]?.asString} / ${SysInfo.userInfo[corpNm]?.asString}"
            }

            SysInfo.userInfo["AUTH"]?.let {
                if(it.asInt <= 0) {
                    naviMain.menu.findItem(R.id.menu_qr)?.apply {
                        isVisible = false
                    }
                }
            }
        }

        viewModel.pagerFlag.postValue(PageType.ADD_SLIP)


    }

//    private fun setActionBar() {
//        setSupportActionBar(toolbar)
//        supportActionBar?.title = null
//    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var destination = when(item.title) {
            getString(R.string.add_slip) -> {
                PageType.ADD_SLIP
            }
            getString(R.string.navi_menu_search_slip) -> {
                PageType.SEARCH_SLIP
            }

            getString(R.string.navi_menu_receive_slip) -> {
                PageType.QR
            }
            getString(R.string.setting) -> {
                PageType.SETTING
            }
            else -> {
                PageType.NULL
            }
        }

        viewModel.pagerFlag.postValue(destination)

        return true
    }


    private fun setViewPager() {
        with(binding) {
            pager.apply {
                adapter = MainViewPagerAdapter(this@MainActivity).apply {

                    addFragment(AddSlipFragment(), FRAG_ADD_SLIP)
                    addFragment(SearchSlipFragment(), FRAG_SEARCH_SLIP)
                    addFragment(QRScanFragment(), FRAG_QR)
                    addFragment(SettingFragment(), FRAG_SETTING)
                }


                binding.layoutDrawer.closeDrawer(GravityCompat.START)
                isUserInputEnabled = false
            }
        }
    }

    //    private fun setupNavigation() {
//
//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//
//        val host: NavHostFragment = supportFragmentManager
//            .findFragmentById(R.id.host_navi) as NavHostFragment? ?: return
//        val navController = host.navController
//
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//
//        setupNavigation(navController) //setup navigation
//        setupActionBar(navController, appBarConfiguration) // setup action bar
//
//        //hear for event changes
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            val dest: String = try {
//                resources.getResourceName(destination.id)
//            } catch (e: Resources.NotFoundException) {
//                Integer.toString(destination.id)
//            }
//            Toast.makeText(
//                this@MainActivity, "Navigated to $dest",
//                Toast.LENGTH_SHORT
//            ).show()
//            Log.d("NavigationActivity", "Navigated to $dest")
//        }
//    }
//
//    private fun setupActionBar(
//        navController: NavController,
//        appBarConfig: AppBarConfiguration
//    ) {
//        setupActionBarWithNavController(navController, appBarConfig)
//    }
//    private fun setupNavigation(navController: NavController) {
//        val sideNavView = findViewById<NavigationView>(R.id.navi_main)
//        sideNavView?.setupWithNavController(navController)
//        val drawerLayout: DrawerLayout? = findViewById(R.id.layout_drawer)
//
//        //fragments load from here but how ?
//        appBarConfiguration = AppBarConfiguration(
//            navController.graph,
//            drawerLayout
//        )
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        return (Navigation.findNavController(this, R.id.host_navi).navigateUp()
//                || super.onSupportNavigateUp())
//    }
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val retValue = super.onCreateOptionsMenu(menu)
//        val navigationView = findViewById<NavigationView>(R.id.navi_main)
//        if (navigationView == null) {
//            //android needs to know what menu I need
//            menuInflater.inflate(R.menu.navi_menu, menu)
//            return true
//        }
//        return retValue
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        //I need to open the drawer onClick
//        when (item!!.itemId) {
//            android.R.id.home ->
//                layout_drawer.openDrawer(GravityCompat.START)
//        }
//        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
//                || super.onOptionsItemSelected(item)
//    }
    override fun onBackPressed() {
        //the code is beautiful enough without comments
        if (binding.layoutDrawer.isDrawerOpen(GravityCompat.START)) {
            binding.layoutDrawer.closeDrawer(GravityCompat.START)
        }
        else {
            binding.pager.let { vp ->
                (vp.adapter as MainViewPagerAdapter).run {

                    val frag = getCurrentFragTag()?.let {
                        getFragmentByTag(it)
                    } ?: run {
                        getFragmentByTag(FRAG_ADD_SLIP)
                    }
//
                    (frag as? OnBackPressedListener)?.onBackPressed()?: run {
                        m_C.simpleConfirm(
                                this@MainActivity,
                                null,
                                getString(R.string.confirm_exit),
                                {
                                    super.finish()
                                },
                                { })
                    }
                }
            }
        }
    }

}


interface OnBackPressedListener {
    fun onBackPressed()
}