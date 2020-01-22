package co.droidchef.zfotos.feature.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import co.droidchef.zfotos.R

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container,
                    GalleryFragment.newInstance()
                )
                .commitNow()
        }
    }

}
