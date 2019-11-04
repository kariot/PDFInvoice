package me.kariot.pdfinvoice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import java.io.ByteArrayOutputStream


fun drawableToByteArray(context: Context, resId: Int): kotlin.ByteArray {
    val drawable = context.resources.getDrawable(resId)
    val bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()

}