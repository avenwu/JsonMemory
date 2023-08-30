package cn.hacktons.json

class Reflect(private val clz: Class<*>, private val target: Any) {

    companion object {
        fun <T : Any> from(target: T): Reflect {
            return Reflect(target.javaClass, target)
        }
    }

    fun <T> valueOf(name: String): T {
        val field = clz.getDeclaredField(name)
        field.isAccessible = true
        return field.get(target) as T
    }
}