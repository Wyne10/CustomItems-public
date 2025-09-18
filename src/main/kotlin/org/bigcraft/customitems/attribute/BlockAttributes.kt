package org.bigcraft.customitems.attribute

import org.bukkit.block.Block

interface BlockAttribute {
    fun apply(block: Block)
}