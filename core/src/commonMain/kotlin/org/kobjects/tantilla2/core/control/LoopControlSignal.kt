package org.kobjects.tantilla2.core.control

class LoopControlSignal(val kind: Kind): TantillaControlSignal() {


    enum class Kind {
        BREAK, CONTINUE
    }

}