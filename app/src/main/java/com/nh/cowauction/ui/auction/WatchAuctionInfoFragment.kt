package com.nh.cowauction.ui.auction

import androidx.fragment.app.activityViewModels
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseFragment
import com.nh.cowauction.databinding.FragmentAuctionInfoBinding
import com.nh.cowauction.viewmodels.AuctionViewModel
import com.nh.cowauction.viewmodels.WatchAuctionViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 개체 정보 화면 Fragment
 *
 * Created by jhlee on 2022-01-20
 */
@AndroidEntryPoint
class WatchAuctionInfoFragment : BaseFragment<FragmentAuctionInfoBinding, WatchAuctionViewModel>() {
    override val layoutId = R.layout.fragment_watch_auction_info
    override val viewModel: WatchAuctionViewModel by activityViewModels()
    override val bindingVariable = BR.viewModel
}