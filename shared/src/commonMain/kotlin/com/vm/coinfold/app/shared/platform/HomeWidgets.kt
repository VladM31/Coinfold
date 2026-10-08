package com.vm.coinfold.app.shared.platform

/** Asks the home screen widgets (where the platform has them) to redraw with fresh numbers. */
expect object HomeWidgets {
    fun refresh()
}
