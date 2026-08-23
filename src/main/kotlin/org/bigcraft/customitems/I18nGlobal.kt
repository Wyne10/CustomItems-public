package org.bigcraft.customitems

import me.wyne.wutils.i18n.I18n

/** [I18n.global] is assigned by [InitializeI18n]; everything that reads it runs after that step. */
internal val i18n: I18n
    get() = I18n.global!!
