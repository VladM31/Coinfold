package com.vm.coinfold.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform