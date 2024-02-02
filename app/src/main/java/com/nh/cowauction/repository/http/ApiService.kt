package com.nh.cowauction.repository.http

import com.nh.cowauction.model.favorite.BiddingInfoResponse
import com.nh.cowauction.model.favorite.FavoriteEntryResponse
import com.nh.cowauction.model.kakaolive.KakaoLiveResponse
import com.nh.cowauction.model.version.VersionResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/appversion")
    fun fetchVersions(
            @Query("osType") osType: String = "AND"
    ): Single<VersionResponse>

    @GET("/api/v1/my/{auctionHouseCode}/favorite")
    fun fetchFavoriteEntry(
            @Path("auctionHouseCode") houseCode: String,
            @Query("aucClass") entryType: String,
            @Query("userMemNum") userMngNum: String,
            @Query("aucSeq") entryNum: String
    ): Single<FavoriteEntryResponse>

    @GET("/api/v1/biz/{auctionHouseCode}/kakao")
    fun fetchKakaoLiveService(
            @Path("auctionHouseCode") houseCode: String
    ): Single<KakaoLiveResponse>

    @GET("/api/v1/my/select/nearAtdrAm")
    fun fetchBiddingInfo(
        @Query("naBzplc") houseCode: String, //조합 코드
        @Query("lvstAucPtcMnNo") userMngNum: String, // 응찰자 참가 번호
        @Query("aucObjDsc") entryType: String, // 출장우 구분
        @Query("aucPrgSq") entryNum: String // 출장우 번호
    ) : Single<BiddingInfoResponse>
}