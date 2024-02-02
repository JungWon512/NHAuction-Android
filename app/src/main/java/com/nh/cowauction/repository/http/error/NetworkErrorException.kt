package com.nh.cowauction.repository.http.error


class NetworkErrorException(val code: Int, val msg: String) : Exception()
