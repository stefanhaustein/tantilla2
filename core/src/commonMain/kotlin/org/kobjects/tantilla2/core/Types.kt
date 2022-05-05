import org.kobjects.greenspun.core.*
import org.kobjects.tantilla2.core.MetaType
import org.kobjects.tantilla2.core.Typed
import org.kobjects.tantilla2.core.classifier.ClassDefinition
import org.kobjects.tantilla2.core.classifier.ImplDefinition
import org.kobjects.tantilla2.core.classifier.TraitDefinition

val Type.tantillaName
    get() = when (this) {
        F64 -> "float"
        Str -> "str"
        is ClassDefinition -> name
        is TraitDefinition -> name
        is ImplDefinition -> name
        else -> toString()
    }

val Any?.type: Type
    get() = when (this) {
        null -> Void
        is Typed -> type
        is Double -> F64
        is Long -> I64
        is Type -> MetaType(this)
        is String -> Str
        else -> throw IllegalArgumentException("Can't determine type of $this")
    }
