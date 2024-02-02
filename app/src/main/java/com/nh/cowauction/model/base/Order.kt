package com.nh.cowauction.model.base

/**
 * Description : TCP 통신중 데이터 가공할때 필요한 annotation class
 * Type 을 제외한 순서는 1 부터 시작
 * Created by hmju on 2021-05-28
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Order (
    val num : Int = 0
)