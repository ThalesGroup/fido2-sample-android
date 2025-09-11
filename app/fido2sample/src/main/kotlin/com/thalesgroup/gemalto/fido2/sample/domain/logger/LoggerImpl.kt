/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.domain.logger

open class LoggerImpl : Logger {
    override val logs: MutableList<String?>

    init {
        this.logs = ArrayList<String?>()
    }

    override fun log(text: String?) {
        logs.add(text)
    }

    override fun clean() {
        logs.clear()
    }
}
