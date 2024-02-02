package com.nh.cowauction.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.nh.cowauction.R
import com.nh.cowauction.contants.Config
import com.nh.cowauction.databinding.DialogSelectIpBinding

/**
 * Description : 디버그용 아이피 포트번호 입력 Dialog
 *
 * Created by hmju on 2021-06-28
 */
class SelectIpDialog(
	context: Context,
	private val callback: (String, String) -> Unit
) : Dialog(context, R.style.CommonDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        DialogSelectIpBinding.inflate(LayoutInflater.from(context)).apply {

            etIp.setText(Config.AUCTION_IP)
            etPort.setText(Config.AUCTION_PORT.toString())
            onConfirm = View.OnClickListener {
                callback(etIp.text.toString(), etPort.text.toString())
                dismiss()
//				multiNullCheck(etIp.text.toString(), etPort.text.toString()) { ip, port ->
//					DLogger.d("IP $ip $port")
//					if (ip.isIpCheck() && port.isPortCheck()) {
//						callback(ip, port)
//						dismiss()
//					} else {
//						tvMsg.text = "아이피 또는 포트번호를 입력해주세요."
//					}
//				} ?: run {
//					tvMsg.text = "아이피 또는 포트번호를 입력해주세요."
//				}
            }
            setContentView(this.root)
        }
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }
}