package cc.typex.base.page

import androidx.fragment.app.Fragment
import cc.typex.app.R
import cc.typex.app.databinding.FragmentContainerActivityBinding

open class FragmentContainerActivity : DataBindingActivity<FragmentContainerActivityBinding>() {

    override fun getDataBindingLayoutId() = R.layout.fragment_container_activity

    protected fun addFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.fcv_fragment_container, fragment)
        if (addToBackStack) ft.addToBackStack(null)
        ft.commit()
    }

    protected fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fcv_fragment_container, fragment)
        if (addToBackStack) ft.addToBackStack(null)
        ft.commit()
    }
}