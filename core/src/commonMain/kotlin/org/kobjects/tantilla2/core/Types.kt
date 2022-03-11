import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.Type
import org.kobjects.greenspun.core.Void
import org.kobjects.tantilla2.core.Lambda

fun typeToString(type: Any) =
    when (type) {
        Double::class -> "float"
        else -> type.toString()
    }

fun typeOf(value: Any?): Type =
    when (value) {
        null -> Void
        is Double -> F64
        is Lambda -> value.type
        else -> throw IllegalArgumentException()
    }
