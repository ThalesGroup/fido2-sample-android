/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.domain.logger

interface Logger {
    fun log(text: String?)
    val logs: MutableList<String?>?
    fun clean()
}
