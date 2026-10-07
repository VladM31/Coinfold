package com.vm.coinfold.app

import androidx.compose.ui.window.ComposeUIViewController
import com.vm.coinfold.app.config.initKoin

fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) { App() }
