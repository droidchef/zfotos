package co.droidchef.zfotos.utils

import io.reactivex.Scheduler

interface SchedulersProvider {

    fun io() : Scheduler
    fun computation(): Scheduler
    fun ui(): Scheduler

}