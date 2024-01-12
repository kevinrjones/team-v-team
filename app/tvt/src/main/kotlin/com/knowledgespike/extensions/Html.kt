package com.knowledgespike.extensions

import kotlinx.html.*

inline fun DIV.p(classes: String? = null, vararg attributes: String?, crossinline block: P.() -> Unit = {}): Unit {
    val classAttr = attributesMapOf("class", classes).toMutableMap()

    val otherAttrs = attributesMapOf(*attributes)

    classAttr.putAll(otherAttrs)


    P(classAttr, consumer).visit(block)
}


inline fun TR.td(classes: String? = null, vararg attributes: String?, crossinline block: TD.() -> Unit = {}): Unit {
    val classAttr = attributesMapOf("class", classes).toMutableMap()

    val otherAttrs = attributesMapOf(*attributes)

    classAttr.putAll(otherAttrs)


    TD(classAttr, consumer).visit(block)
}
inline fun TR.th(classes: String? = null, vararg attributes: String?, crossinline block: TD.() -> Unit = {}): Unit {
    val classAttr = attributesMapOf("class", classes).toMutableMap()

    val otherAttrs = attributesMapOf(*attributes)

    classAttr.putAll(otherAttrs)


    TD(classAttr, consumer).visit(block)
}




