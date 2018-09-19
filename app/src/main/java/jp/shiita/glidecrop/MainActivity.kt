package jp.shiita.glidecrop

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlideApp.with(this)
                .load(URL)
                .into(defaultImageView)

        GlideApp.with(this)
                .load(URL)
                .circleCrop()
                .into(circleCropImageView)

        GlideApp.with(this)
                .load(URL)
                .transform(CropTransformation(R.drawable.ic_brightness))
                .into(drawableCropImageView)

        GlideApp.with(this)
                .load(URL)
                .transform(CropTransformation(R.drawable.ic_brightness, true))
                .into(drawableCropOutImageView)

        GlideApp.with(this)
                .load(URL)
                .transform(CropTransformation(R.drawable.rounded_rectangle))
                .into(roundedRectangleImageView)
    }

    companion object {
        const val URL = "https://avatars0.githubusercontent.com/u/20969270?s=460&v=4"
    }
}
