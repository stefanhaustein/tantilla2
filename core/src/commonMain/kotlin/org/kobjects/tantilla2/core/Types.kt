import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Str
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.MetaType
import org.kobjects.tantilla2.core.Typed

val Type.tantillaName
    get() = when (this) {
        F64 -> "float"
        Str -> "str"
        else -> this.name
    }

val Any?.type: Type
    get() = when (this) {
        null -> Void
        is Typed -> type
        is Double -> F64
        is Type -> MetaType(this)
        is String -> Str
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
