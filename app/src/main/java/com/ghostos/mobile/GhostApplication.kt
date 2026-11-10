package com.ghostos.mobile

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import id.or.oo.pr.engine.NetworkDns
import id.or.oo.pr.engine.ProotHost
import id.or.oo.pr.session.PrBootstrap
import java.io.File
import java.net.Inet6Address

/** Android host contract consumed by the isolated PRoot guest runtime. */
class GhostApplication : Application(), ProotHost {
    override val prefixDir: File get() = File(filesDir, "ghost-prefix")
    override val homeDir: File get() = File(filesDir, "ghost-home")
    override val packageName: String get() = super.getPackageName()
    override val cacheDir: File get() = super.getCacheDir()

    override val dnsServers: List<String>
        get() {
            val connectivity = getSystemService(ConnectivityManager::class.java) ?: return emptyList()
            val network: Network = connectivity.activeNetwork ?: return emptyList()
            val properties = connectivity.getLinkProperties(network) ?: return emptyList()
            val hasIpv6DefaultRoute = properties.routes.any {
                it.destination.prefixLength == 0 && it.destination.address is Inet6Address
            }
            return NetworkDns.usableServers(properties.dnsServers, hasIpv6DefaultRoute)
        }

    override fun onCreate() {
        super.onCreate()
        PrBootstrap(this, this, BOOTSTRAP_VERSION).run()
    }

    private companion object {
        const val BOOTSTRAP_VERSION = 1
    }
}
