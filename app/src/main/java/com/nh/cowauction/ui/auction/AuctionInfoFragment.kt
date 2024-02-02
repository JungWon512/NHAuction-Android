package com.nh.cowauction.ui.auction

import androidx.fragment.app.activityViewModels
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseFragment
import com.nh.cowauction.databinding.FragmentAuctionInfoBinding
import com.nh.cowauction.viewmodels.AuctionViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 개체 정보 화면 Fragment
 *
 * Created by hmju on 2021-06-09
 */
@AndroidEntryPoint
class AuctionInfoFragment : BaseFragment<FragmentAuctionInfoBinding, AuctionViewModel>() {
    override val layoutId = R.layout.fragment_auction_info
    override val viewModel: AuctionViewModel by activityViewModels()
    override val bindingVariable = BR.viewModel
}